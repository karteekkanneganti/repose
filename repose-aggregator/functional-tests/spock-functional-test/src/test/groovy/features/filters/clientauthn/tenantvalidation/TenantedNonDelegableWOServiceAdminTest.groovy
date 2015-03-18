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
package features.filters.clientauthn.tenantvalidation

import framework.ReposeValveTest
import framework.mocks.MockIdentityService
import org.joda.time.DateTime
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.MessageChain
import org.rackspace.deproxy.Response
import spock.lang.Unroll

class TenantedNonDelegableWOServiceAdminTest extends ReposeValveTest {

    def static originEndpoint
    def static identityEndpoint

    def static MockIdentityService fakeIdentityService

    def setupSpec() {

        deproxy = new Deproxy()

        def params = properties.defaultTemplateParams
        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/filters/clientauthn/removetenant", params)
        repose.configurationProvider.applyConfigs("features/filters/clientauthn/removetenant/noserviceroles", params)
        repose.start()

        originEndpoint = deproxy.addEndpoint(properties.targetPort, 'origin service')
        fakeIdentityService = new MockIdentityService(properties.identityPort, properties.targetPort)
        identityEndpoint = deproxy.addEndpoint(properties.identityPort,
                'identity service', null, fakeIdentityService.handler)


    }

    def cleanupSpec() {
        deproxy.shutdown()

        repose.stop()
    }

    def setup(){
        fakeIdentityService.resetHandlers()
    }

    @Unroll("tenant: #requestTenant, with return from identity with HTTP code (#authResponseCode), group HTTP code (#groupResponseCode) and response tenant: #responseTenant")
    def "when authenticating user in tenanted and non delegable mode and without service-admin - fail"() {

        given:
        fakeIdentityService.with {
            client_token = UUID.randomUUID().toString()
            tokenExpiresAt = DateTime.now().plusDays(1)
            client_tenant = responseTenant
            client_userid = requestTenant
            service_admin_role = "not-admin"
        }

        if(authResponseCode != 200){
            fakeIdentityService.validateTokenHandler = {
                tokenId, request,xml ->
                    new Response(authResponseCode)
            }
        }

        if(groupResponseCode != 200){
            fakeIdentityService.getGroupsHandler = {
                userId, request,xml ->
                    new Response(groupResponseCode)
            }
        }

        when: "User passes a request through repose"
        MessageChain mc = deproxy.makeRequest(
                url: "$reposeEndpoint/servers/$requestTenant/",
                method: 'GET',
                headers: [
                        'content-type': 'application/json',
                        'X-Auth-Token': fakeIdentityService.client_token
                ]
        )

        then: "Request body sent from repose to the origin service should contain"
        mc.receivedResponse.code == responseCode
        mc.handlings.size() == 0

        where:
        requestTenant | responseTenant  | authResponseCode | responseCode | groupResponseCode | x_www_auth
        813           | 813             | 500              | "500"        | 200               | false
        814           | 814             | 404              | "401"        | 200               | true
        815           | 815             | 200              | "500"        | 404               | false
        816           | 816             | 200              | "500"        | 500               | false
        811           | 812             | 200              | "401"        | 200               | true


    }

    def "when authenticating user in tenanted and non delegable mode and without service-admin - pass"() {
        given:
        fakeIdentityService.with {
            client_token = UUID.randomUUID().toString()
            tokenExpiresAt = DateTime.now().plusDays(1)
            client_tenant = 999
            client_userid = 999
            service_admin_role = "non-admin"
        }

        when: "User passes a request through repose"
        MessageChain mc = deproxy.makeRequest(
                url:"$reposeEndpoint/servers/999/",
                method:'GET',
                headers:['content-type': 'application/json', 'X-Auth-Token': fakeIdentityService.client_token])

        then: "Request body sent from repose to the origin service should contain"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1
        mc.handlings[0].endpoint == originEndpoint
        def request2 = mc.handlings[0].request
        request2.headers.getFirstValue("X-Default-Region") == "the-default-region"
        request2.headers.contains("x-auth-token")
        !request2.headers.contains("x-identity-status")
        request2.headers.contains("x-authorization")
        request2.headers.getFirstValue("x-authorization") == "Proxy 999"
    }

}
