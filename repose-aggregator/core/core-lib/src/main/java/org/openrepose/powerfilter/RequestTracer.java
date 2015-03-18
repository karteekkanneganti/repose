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
package org.openrepose.powerfilter;

import org.openrepose.commons.utils.servlet.http.MutableHttpServletResponse;

public class RequestTracer {

    private final boolean trace;
    private final boolean addHeader;
    private long startTime;

    public RequestTracer(boolean trace, boolean addHeader) {
        this.trace = trace;
        this.addHeader = addHeader;
    }

    public long traceEnter() {
        if (!trace) {
            return 0;
        }

        startTime = System.currentTimeMillis();
        return startTime;
    }

    public long traceExit(MutableHttpServletResponse response, String filterName) {
        if (!trace) {
            return 0;
        }

        long totalRequestTime = System.currentTimeMillis() - startTime;

        if (addHeader) {
            response.addHeader("X-" + filterName + "-Time", totalRequestTime + "ms");
        }

        return totalRequestTime;
    }
}
