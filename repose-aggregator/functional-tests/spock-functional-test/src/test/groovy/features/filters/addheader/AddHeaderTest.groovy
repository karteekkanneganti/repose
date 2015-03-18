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
package features.filters.addheader

import framework.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.MessageChain

/**
 * Created by jennyvo on 12/11/14.
 */
class AddHeaderTest extends ReposeValveTest {

    def setupSpec() {
        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort)

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/filters/addheader", params)
        repose.start()
    }

    def cleanupSpec() {
        deproxy.shutdown()
        repose.stop()
    }

    def "When using add-header filter the expect header(s) in config is added to request/response" () {
        given:
        def Map headers = ["x-rax-user": "test-user", "x-rax-groups": "reposegroup1"]

        when: "Request contains value(s) of the target header"
        def mc = deproxy.makeRequest([url: reposeEndpoint, headers: headers])
        def sentRequest = ((MessageChain) mc).getHandlings()[0]

        then: "The request/response should contain additional header from add-header config"
        sentRequest.request.headers.contains("x-rax-user")
        sentRequest.request.headers.getFirstValue("x-rax-user") == "test-user"
        sentRequest.request.headers.contains("x-rax-groups")
        sentRequest.request.headers.getFirstValue("x-rax-groups") == "reposegroup1"
        sentRequest.request.headers.contains("repose-test")
        sentRequest.request.headers.getFirstValue("repose-test") == "this-is-a-test"
        mc.getReceivedResponse().headers.contains("response-header")
        mc.getReceivedResponse().headers.getFirstValue("response-header") == "foooo;q=0.9"
    }
}
