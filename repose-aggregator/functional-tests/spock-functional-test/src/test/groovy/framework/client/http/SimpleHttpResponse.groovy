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
package framework.client.http

import org.apache.http.Header

class SimpleHttpResponse {

    def String responseBody
    def statusCode
    def Header[] responseHeaders

    def getHeader(String name) {
        def String foundValue

        responseHeaders.each { header ->
            if (header.name.equals(name)) {
                foundValue = header.value
            }
        }
        return foundValue
    }

    @Override
    String toString() {
        return "response body: " + responseBody + " statusCode: " + statusCode + " responseHeaders: " + responseHeaders.toString()
    }
}