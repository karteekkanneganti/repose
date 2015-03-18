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
package org.openrepose.filters.translation.httpx.node;

import org.openrepose.core.httpx.Body;
import org.openrepose.core.httpx.Response;
import org.openrepose.filters.translation.httpx.ObjectFactoryUser;

import javax.servlet.http.HttpServletResponse;

/**
 * @author fran
 */
public class ResponseBodyNode extends ObjectFactoryUser implements Node {
    private final HttpServletResponse response;
    private final Response messageResponse;
    private final boolean jsonProcessing;

    public ResponseBodyNode(HttpServletResponse response, Response messageResponse, boolean jsonProcessing) {
        this.response = response;
        this.messageResponse = messageResponse;
        this.jsonProcessing = jsonProcessing;
    }

    @Override
    public void build() {
        Body body = getObjectFactory().createBody();

        // TODO: Need to determine how we want to handle the response data and if it needs any processing
        
        messageResponse.setBody(body);
    }
    
    public HttpServletResponse getResponse() {
        return response;
    }
    
    public boolean getJsonProcessing() {
        return jsonProcessing;
    }
        
}
