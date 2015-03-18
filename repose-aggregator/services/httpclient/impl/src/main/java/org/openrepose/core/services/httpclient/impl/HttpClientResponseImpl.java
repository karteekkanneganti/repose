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
package org.openrepose.core.services.httpclient.impl;

import org.openrepose.core.services.httpclient.HttpClientResponse;
import org.apache.http.client.HttpClient;

/**
 *  An HttpClientResponse that generates a unique UUID
 */
public class HttpClientResponseImpl implements HttpClientResponse {

    private HttpClient httpClient;
    private String clientId;
    private String clientInstanceId;
    private String userId;

    public HttpClientResponseImpl(HttpClient httpClient, String clientId, String clientInstanceId, String userId) {
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.clientInstanceId = clientInstanceId;
        this.userId = userId;
    }

    @Override
    public HttpClient getHttpClient() {
        return httpClient;
    }

    public String getClientInstanceId() {
        return clientInstanceId;
    }

    public String getUserId() {
        return userId;
    }

}
