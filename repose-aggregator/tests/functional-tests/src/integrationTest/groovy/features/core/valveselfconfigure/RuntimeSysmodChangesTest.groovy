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
package features.core.valveselfconfigure

import org.junit.experimental.categories.Category
import org.openrepose.framework.test.PortFinder
import org.openrepose.framework.test.ReposeValveTest
import org.rackspace.deproxy.Deproxy
import scaffold.category.Core
import spock.lang.Shared

import java.util.concurrent.TimeUnit

@Category(Core)
class RuntimeSysmodChangesTest extends ReposeValveTest {

    @Shared
    int port1
    @Shared
    int port2
    @Shared
    int port3

    def setupSpec() {

        deproxy = new Deproxy()
        deproxy.addEndpoint(properties.targetPort)

        port1 = properties.reposePort
        port2 = PortFinder.instance.getNextOpenPort()
        port3 = PortFinder.instance.getNextOpenPort()

        def params = properties.defaultTemplateParams
        params += [
                'port1'     : port1,
                'port2'     : port2,
                'port3'     : port3,

                'proto'     : 'http',
                'sysmodPort': port1,

        ]
        repose.configurationProvider.cleanConfigDirectory()

        repose.configurationProvider.applyConfigs("common", params)
        repose.configurationProvider.applyConfigs("features/core/valveselfconfigure/container-no-port", params)
        repose.configurationProvider.applyConfigs("features/core/valveselfconfigure/single-node-with-proto", params)
        repose.start(killOthersBeforeStarting: false,
                waitOnJmxAfterStarting: false)
        repose.waitForNon500FromUrl("http://localhost:${port1}")
    }

    def "when making runtime changes to the system model, available nodes/ports/etc should change accordingly"() {

        def mc

        when: "Repose first starts up"
        mc = deproxy.makeRequest(url: "http://localhost:${port1}")
        then: "the first node should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "Repose first starts up"
        deproxy.makeRequest(url: "http://localhost:${port2}")
        then: "port 2 should not connect"
        thrown(ConnectException)

        when: "Repose first starts up"
        deproxy.makeRequest(url: "http://localhost:${port3}")
        then: "port 3 should not connect"
        thrown(ConnectException)



        when: "change the configs while it's running - two nodes"
        def params = properties.getDefaultTemplateParams()
        params += [
                'node1host' : 'localhost',
                'node2host' : 'localhost',
                'node1port' : port1,
                'node2port' : port2,
        ]
        reposeLogSearch.cleanLog()
        repose.configurationProvider.applyConfigs('features/core/valveselfconfigure/two-nodes', params)
        println("Change config to two-nodes")
        reposeLogSearch.awaitByString("Configuration Updated: SystemModel", 1, 35, TimeUnit.SECONDS)
        repose.waitForNon500FromUrl("http://localhost:${port1}")
        repose.waitForNon500FromUrl("http://localhost:${port2}")
        then:
        1 == 1 //WAT


        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port1}")
        then: "the first node should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port2}")
        then: "node 2 should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "configs have changed"
        deproxy.makeRequest(url: "http://localhost:${port3}")
        then: "port 3 should not connect"
        thrown(ConnectException)



        when: "change the configs while it's running - one node on port 2"
        params = properties.getDefaultTemplateParams()
        params += [
                'proto'     : 'http',
                'sysmodPort': port2,
        ]
        reposeLogSearch.cleanLog()
        repose.configurationProvider.applyConfigs('features/core/valveselfconfigure/single-node-with-proto', params)
        println("changed configs to single-node-with-proto")
        reposeLogSearch.awaitByString("Configuration Updated: SystemModel", 1, 35, TimeUnit.SECONDS)
        repose.waitForNon500FromUrl("http://localhost:${port2}")
        then:
        1 == 1



        when: "configs have changed"
        deproxy.makeRequest(url: "http://localhost:${port1}")
        then: "port 1 should not connect"
        thrown(ConnectException)

        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port2}")
        then: "node 2 should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "configs have changed"
        deproxy.makeRequest(url: "http://localhost:${port3}")
        then: "port 3 should not connect"
        thrown(ConnectException)



        when: "change the configs while it's running - two of three nodes"
        params = properties.getDefaultTemplateParams()
        params += [
                'node1host' : 'localhost',
                'node2host' : 'localhost',
                'node3host' : 'example.com',
                'node1port' : port1,
                'node2port' : port2,
                'node3port' : port3,
        ]
        reposeLogSearch.cleanLog()
        repose.configurationProvider.applyConfigs('features/core/valveselfconfigure/three-nodes', params)
        println("changed to three-nodes config")
        reposeLogSearch.awaitByString("Configuration Updated: SystemModel", 1, 35, TimeUnit.SECONDS)
        repose.waitForNon500FromUrl("http://localhost:${port1}")
        repose.waitForNon500FromUrl("http://localhost:${port2}")
        then:
        1 == 1


        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port1}")
        then: "the first node should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port2}")
        then: "node 2 should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "configs have changed"
        deproxy.makeRequest(url: "http://localhost:${port3}")
        then: "port 3 should not connect"
        thrown(ConnectException)



        when: "change the configs while it's running - two of three nodes again, but different hostnames"
        params = properties.getDefaultTemplateParams()
        params += [
                'node1host' : 'example.com',
                'node2host' : 'localhost',
                'node3host' : 'localhost',
                'node1port' : port1,
                'node2port' : port2,
                'node3port' : port3,
        ]
        reposeLogSearch.cleanLog()
        repose.configurationProvider.applyConfigs('features/core/valveselfconfigure/three-nodes', params)
        println("changed to three-nodes config again, but a different hostname")
        reposeLogSearch.awaitByString("Configuration Updated: SystemModel", 1, 35, TimeUnit.SECONDS)
        repose.waitForNon500FromUrl("http://localhost:${port2}")
        repose.waitForNon500FromUrl("http://localhost:${port3}")
        then:
        1 == 1



        when: "configs have changed"
        deproxy.makeRequest(url: "http://localhost:${port1}")
        then: "port 1 should not connect"
        thrown(ConnectException)

        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port2}")
        then: "node 2 should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1

        when: "configs have changed"
        mc = deproxy.makeRequest(url: "http://localhost:${port3}")
        then: "node 3 should be available"
        mc.receivedResponse.code == "200"
        mc.handlings.size() == 1
    }
}
