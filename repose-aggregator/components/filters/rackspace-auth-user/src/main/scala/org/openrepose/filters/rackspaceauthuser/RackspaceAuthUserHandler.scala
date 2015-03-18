/*
 *  Copyright (c) 2015 Rackspace US, Inc.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openrepose.filters.rackspaceauthuser

import java.io.InputStream
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.openrepose.commons.utils.http.PowerApiHeader
import org.openrepose.commons.utils.io.stream.LimitedReadInputStream
import org.openrepose.commons.utils.servlet.http.ReadableHttpServletResponse
import org.openrepose.core.filter.logic.common.AbstractFilterLogicHandler
import org.openrepose.core.filter.logic.impl.FilterDirectorImpl
import org.openrepose.core.filter.logic.{FilterAction, FilterDirector}
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.io.Source
import scala.xml.XML

class RackspaceAuthUserHandler(filterConfig: RackspaceAuthUserConfig) extends AbstractFilterLogicHandler with LazyLogging {

  type UsernameParsingFunction = InputStream => Option[String]

  override def handleRequest(request: HttpServletRequest, response: ReadableHttpServletResponse): FilterDirector = {
    val director = new FilterDirectorImpl()
    //By default, if nothing happens we're going to pass
    director.setFilterAction(FilterAction.PASS)

    //Only operate on things if it's a POST, else there's no body to manipulate.
    if (request.getMethod == "POST") {
      val headerManager = director.requestHeaderManager()
      val contentType = request.getContentType

      //This logic is exactly the same regardless of the configuration, so lets reuse it
      val updateHeaders: (IdentityGroupConfig, String) => Unit = { (config, username) =>
        headerManager.appendHeader(PowerApiHeader.USER.toString, username, config.getQuality.toDouble)
        headerManager.appendHeader(PowerApiHeader.GROUPS.toString, config.getGroup, config.getQuality.toDouble)
        director.setFilterAction(FilterAction.PASS) //We don't want to muck with the response, just the headers
      }

      val inputStream = request.getInputStream()

      //If the config for v11 is set, do the work
      Option(filterConfig.getV11).map { config =>
        parseUsername(config, inputStream, contentType, username1_1JSON, username1_1XML)(updateHeaders)
      }
      //If the config for v20 is set, do the work it's not likely that both will be set, or that both will succeed
      Option(filterConfig.getV20).map { config =>
        parseUsername(config, inputStream, contentType, username2_0JSON, username2_0XML)(updateHeaders)
      }
    }

    director
  }

  /**
   * Build a function that takes our config, the request itself, functions to transform if given json, and if given XML
   * and then a resultant function that can take that config and the username to do the work with.
   */
  def parseUsername(config: IdentityGroupConfig, inputStream: InputStream, contentType: String, json: UsernameParsingFunction, xml: UsernameParsingFunction)(usernameFunction: (IdentityGroupConfig, String) => Unit) = {
    val limit = BigInt(config.getContentBodyReadLimit).toLong
    //Copied this limited read stuff from the other content-identity filter...
    val limitedInputStream = new LimitedReadInputStream(limit, inputStream) //Allows me to reset?
    limitedInputStream.mark(limit.toInt)
    try {
      val usernameOpt = if (contentType.contains("xml")) {
        //It's probably xml, lets try to xpath it
        xml(limitedInputStream)
      } else {
        //Try to run it through the JSON pather
        json(limitedInputStream)
      }

      usernameOpt.map { username =>
        usernameFunction(config, username)
      }
    } catch {
      case e: Exception =>
        val identityRequestVersion = if (config.isInstanceOf[IdentityV11]) {
          "v 1.1"
        } else {
          "v 2.0"
        }
        logger.warn(s"Unable to parse username from identity $identityRequestVersion request", e)
    } finally {
      limitedInputStream.reset()
    }
  }

  val username1_1XML: UsernameParsingFunction = { is =>
    val xml = XML.load(is)
    val username = (xml \\ "credentials" \ "@username").text
    if (username.nonEmpty) {
      Some(username)
    } else {
      None
    }
  }

  // https://www.playframework.com/documentation/2.3.x/ScalaJson
  //Using play json here because I don't have to build entire objects
  val username1_1JSON: UsernameParsingFunction = { is =>
    val json = Json.parse(Source.fromInputStream(is).getLines() mkString)
    val username = (json \ "credentials" \ "username").validate[String]
    username match {
      case s: JsSuccess[String] =>
        Some(s.get)
      case f: JsError =>
        logger.debug(s"1.1 JSON parsing failure: ${
          JsError.toFlatJson(f)
        }")
        None
    }
  }

  /**
   * Many payloads to parse here, should be fun
   */
  val username2_0XML: UsernameParsingFunction = { is =>
    val xml = XML.load(is)
    val auth = xml \\ "auth"
    val possibleUsernames = List(
      (auth \ "apiKeyCredentials" \ "@username").text,
      (auth \ "passwordCredentials" \ "@username").text,
      (auth \ "@tenantId").text,
      (auth \ "@tenantName").text
    )

    possibleUsernames.filterNot(_.isEmpty).foldLeft[Option[String]](Option.empty[String]) {
      (opt, it) =>
        Some(it)
    }
  }

  val username2_0JSON: UsernameParsingFunction = { is =>
    val json = Json.parse(Source.fromInputStream(is).getLines() mkString)
    val possibleUsernames = List(
      (json \ "auth" \ "passwordCredentials" \ "username").validate[String],
      (json \ "auth" \ "RAX-KSKEY:apiKeyCredentials" \ "username").validate[String],
      (json \ "auth" \ "tenantId").validate[String],
      (json \ "auth" \ "tenantName").validate[String]
    )

    val usernames = possibleUsernames.map {
      case s: JsSuccess[String] => Some(s.get)
      case f: JsError =>
        logger.debug(s"2.0 JSON Parsing failure: ${JsError.toFlatJson(f)}")
        None
    }.filterNot(_.isEmpty)

    //At this point we have a prioritized list of the username parsing, where the head of the list is more
    // important to return than the tail. If we are empty, we didn't find anything,
    // If we've got at least one item, return just the first
    if (usernames.isEmpty) {
      None
    } else {
      usernames.head
    }
  }

  case class JSONParseException(message: String, reason: Throwable = null) extends Exception(message, reason)

  //Response will always pass through in this one
}
