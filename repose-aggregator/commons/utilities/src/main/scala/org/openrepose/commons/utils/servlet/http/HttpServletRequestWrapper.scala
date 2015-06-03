/*
 * _=_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=
 * Repose
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Copyright (C) 2010 - 2015 Rackspace US, Inc.
 * _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_=_
 */
package org.openrepose.commons.utils.servlet.http

import java.util
import javax.servlet.http.HttpServletRequest

import org.openrepose.commons.utils.http.header.HeaderName

import scala.collection.JavaConverters._

/**
 * Created with IntelliJ IDEA.
 * User: adrian
 * Date: 5/27/15
 * Time: 10:25 AM
 */
class HttpServletRequestWrapper(originalRequest: HttpServletRequest)
  extends javax.servlet.http.HttpServletRequestWrapper(originalRequest)
  with HeaderInteractor {

  private var headerMap :Map[HeaderName, List[String]] = Map[HeaderName, List[String]]()
  private var removedHeaders :Set[HeaderName] = Set[HeaderName]()

  def getHeaderNamesSet: Set[String] = {
    (super.getHeaderNames.asScala.toSet.map(HeaderName.wrap).filterNot(removedHeaders.contains) ++ headerMap.keySet).map(_.getName)
  }

  override def getHeaderNames: util.Enumeration[String] = {
    getHeaderNamesSet.toIterator.asJavaEnumeration
  }

  override def getHeaderNamesList: util.List[String] = {
    getHeaderNamesSet.toList.asJava
  }

  override def getIntHeader(headerName: String): Int = {
    val wrappedHeaderName : HeaderName = HeaderName.wrap(headerName)
    val header : String = getHeader(headerName)
    if(header == null || removedHeaders.contains(wrappedHeaderName)) {
      -1
    }
    else {
      header.toInt
    }
  }

  override def getHeaders(s: String): util.Enumeration[String] = super.getHeaders(s)

  override def getDateHeader(s: String): Long = super.getDateHeader(s)

  override def getHeader(s: String): String = super.getHeader(s)

  def getHeadersScalaList(headerName: String) :List[String] = {
    val wrappedHeaderName : HeaderName = HeaderName.wrap(headerName)
    if (removedHeaders.contains(wrappedHeaderName)) {
      List[String]()
    }
    else {
      headerMap.getOrElse(wrappedHeaderName, super.getHeaders(headerName).asScala.toList)
    }
  }

  override def getHeadersList(headerName: String): util.List[String] = {
    getHeadersScalaList(headerName).asJava
  }

  override def addHeader(headerName: String, headerValue: String): Unit = {
    val wrappedHeaderName :HeaderName = HeaderName.wrap(headerName)
    val existingHeaders: List[String] = getHeadersScalaList(headerName) //this has to be done before we remove from the list,
                                                                        // because getting this list is partially based on the contents of the removed list
    if (removedHeaders.contains(wrappedHeaderName)) {
      removedHeaders = removedHeaders.filterNot(_.equals(wrappedHeaderName))
    }
    headerMap = headerMap + (wrappedHeaderName ->  (existingHeaders :+ headerValue))
  }

  override def addHeader(headerName: String, headerValue: String, quality: Double): Unit = ???

  override def getPreferredSplittableHeader(headerName: String): String = ???

  override def appendHeader(headerName: String, headerValue: String): Unit = ???

  override def appendHeader(headerName: String, headerValue: String, quality: Double): Unit = ???

  override def removeHeader(headerName: String): Unit = {
    val wrappedHeaderName: HeaderName = HeaderName.wrap(headerName)
    removedHeaders = removedHeaders + wrappedHeaderName
    headerMap = headerMap.filterKeys(!_.equals(wrappedHeaderName))
  }

  override def getPreferredHeader(headerName: String): String = ???

  override def replaceHeader(headerName: String, headerValue: String): Unit = ???

  override def replaceHeader(headerName: String, headerValue: String, quality: Double): Unit = ???

  override def getSplittableHeader(headerName: String): util.List[String] = ???
}
