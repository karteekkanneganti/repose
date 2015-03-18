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
package org.openrepose.commons.utils.logging.apache.format.stock;

import org.openrepose.commons.utils.logging.apache.format.FormatterLogic;
import org.openrepose.commons.utils.servlet.http.MutableHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResponseBytesHandler implements FormatterLogic {

    @Override
    public String handle(HttpServletRequest request, HttpServletResponse response) {
        MutableHttpServletResponse mutableResponse = MutableHttpServletResponse.wrap(request, response);
        return String.valueOf(mutableResponse.getResponseSize());
    }
}
