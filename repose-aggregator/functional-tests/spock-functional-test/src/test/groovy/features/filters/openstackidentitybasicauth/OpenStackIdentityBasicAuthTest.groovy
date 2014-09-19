package features.filters.openstackidentitybasicauth

import framework.ReposeValveTest
import framework.mocks.MockIdentityService
import org.apache.commons.codec.binary.Base64
import org.rackspace.deproxy.Deproxy
import org.rackspace.deproxy.Request
import org.rackspace.deproxy.Response

import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.HttpHeaders

class OpenStackIdentityBasicAuthTest extends ReposeValveTest {

    def static originEndpoint
    def static identityEndpoint
    def static ORIGIN_PASS_BODY = ":-)"
    def static ORIGIN_FAIL_BODY = "8^("

    def static MockIdentityService fakeIdentityService

    def setupSpec() {
        deproxy = new Deproxy()

        def params = properties.getDefaultTemplateParams()
        repose.configurationProvider.cleanConfigDirectory()
        repose.configurationProvider.applyConfigs("common", params);
        repose.configurationProvider.applyConfigs("features/filters/openstackidentitybasicauth/common", params);
        repose.configurationProvider.applyConfigs("features/filters/openstackidentitybasicauth/standalone", params);

        repose.start()

        //TODO: The port finding logic is not working!
        originEndpoint = deproxy.addEndpoint(properties.targetPort, 'origin service', null, { Request request -> return handleOriginRequest(request) })
        fakeIdentityService = new MockIdentityService(properties.identityPort, properties.targetPort)
        identityEndpoint = deproxy.addEndpoint(properties.identityPort, 'identity service', null, fakeIdentityService.handler)
    }

    def static Response handleOriginRequest(Request request) {
        if (request.getHeaders().getFirstValue(HttpHeaders.AUTHORIZATION).equals(
                'Basic ' + Base64.encodeBase64URLSafeString((fakeIdentityService.client_username + ":" + fakeIdentityService.client_apikey).bytes))) {
            return new Response(HttpServletResponse.SC_OK, null, null, ORIGIN_PASS_BODY)
        } else {
            return new Response(HttpServletResponse.SC_UNAUTHORIZED, null, null, ORIGIN_FAIL_BODY)
        }
    }


    def cleanupSpec() {
        if (deproxy) {
            deproxy.shutdown()
        }

        if (repose) {
            repose.stop()
        }
    }

    def "No HTTP Basic authentication header sent."() {
        when: "the request does not have an HTTP Basic authentication header"
        def messageChain = deproxy.makeRequest(url: reposeEndpoint, method: 'GET')

        then: "simply pass it on down the filter chain and this configuration will respond with a SC_UNAUTHORIZED (401) and add an HTTP Basic authentication header"
        messageChain.receivedResponse.code == HttpServletResponse.SC_UNAUTHORIZED.toString()
        messageChain.receivedResponse.body.equals(ORIGIN_FAIL_BODY)
        messageChain.receivedResponse.headers.findAll(HttpHeaders.WWW_AUTHENTICATE).contains("Basic realm=\"RAX-KEY\"")
        messageChain.getOrphanedHandlings().empty
    }

    def "Retrieve a token for an HTTP Basic authentication header with UserName/ApiKey"() {
        when: "the request does have an HTTP Basic authentication header with UserName/ApiKey"
        def messageChain = deproxy.makeRequest(url: reposeEndpoint, method: 'GET',
                headers: [(HttpHeaders.AUTHORIZATION): 'Basic ' + Base64.encodeBase64URLSafeString((fakeIdentityService.client_username + ":" + fakeIdentityService.client_apikey).bytes)])

        then: "then get a token for it"
        messageChain.receivedResponse.code == HttpServletResponse.SC_OK.toString()
        messageChain.receivedResponse.body.equals(ORIGIN_PASS_BODY)
        messageChain.handlings.get(messageChain.handlings.size() - 1).request.headers.getCountByName("X-Auth-Token") == 1
        messageChain.handlings.get(messageChain.handlings.size() - 1).request.headers.getFirstValue("X-Auth-Token").equals(fakeIdentityService.client_token)
        messageChain.orphanedHandlings.size() == 1
    }

    def "Ensure that subsequent calls within the cache timeout are retrieving the token from the cache"() {
        when: "multiple requests that have the same HTTP Basic authentication header"
        def messageChain0 = deproxy.makeRequest(url: reposeEndpoint, method: 'GET',
                headers: [(HttpHeaders.AUTHORIZATION): 'Basic ' + Base64.encodeBase64URLSafeString((fakeIdentityService.client_username + ":" + fakeIdentityService.client_apikey).bytes)])
        def messageChain = deproxy.makeRequest(url: reposeEndpoint, method: 'GET',
                headers: [(HttpHeaders.AUTHORIZATION): 'Basic ' + Base64.encodeBase64URLSafeString((fakeIdentityService.client_username + ":" + fakeIdentityService.client_apikey).bytes)])

        then: "get the token from the cache"
        messageChain.receivedResponse.code == HttpServletResponse.SC_OK.toString()
        messageChain.receivedResponse.body.equals(ORIGIN_PASS_BODY)
        messageChain.handlings.get(messageChain.handlings.size() - 1).request.headers.getCountByName("X-Auth-Token") == 1
        messageChain.handlings.get(messageChain.handlings.size() - 1).request.headers.getFirstValue("X-Auth-Token").equals(fakeIdentityService.client_token)
        messageChain.orphanedHandlings.size() == 0
    }

    def "Ensure that subsequent calls outside the cache timeout are retrieving a new token not from the cache"() {
        when: "multiple requests that have the same HTTP Basic authentication header, but are separated by more than the cache timeout"
        def messageChain0 = deproxy.makeRequest(url: reposeEndpoint, method: 'GET',
                headers: [(HttpHeaders.AUTHORIZATION): 'Basic ' + Base64.encodeBase64URLSafeString((fakeIdentityService.client_username + ":" + fakeIdentityService.client_apikey).bytes)])
        sleep(5000) // How do I get this programmatically from the config.
        def messageChain = deproxy.makeRequest(url: reposeEndpoint, method: 'GET',
                headers: [(HttpHeaders.AUTHORIZATION): 'Basic ' + Base64.encodeBase64URLSafeString((fakeIdentityService.client_username + ":" + fakeIdentityService.client_apikey).bytes)])

        then: "get the token from the Identity (Keystone) service"
        messageChain.receivedResponse.code == HttpServletResponse.SC_OK.toString()
        messageChain.receivedResponse.body.equals(ORIGIN_PASS_BODY)
        messageChain.handlings.get(messageChain.handlings.size() - 1).request.headers.getCountByName("X-Auth-Token") == 1
        messageChain.handlings.get(messageChain.handlings.size() - 1).request.headers.getFirstValue("X-Auth-Token").equals(fakeIdentityService.client_token)
        messageChain.orphanedHandlings.size() == 1
    }
}
