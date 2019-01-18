/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.test.mixin.integration.Integration
import grails.util.Holders
import nl.thehyve.gb.backend.client.TransmartRestClient
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Ignore
import spock.lang.Specification

@Integration
class TransmartRestClientSpec extends Specification {

    TransmartRestClient testee
    String dimensionName

    def setup() {
        testee = new TransmartRestClient()
        testee.transmartServerUrl = 'https://test1.org'
//        testee.restTemplate = mockTemplate()

        dimensionName = 'dimension1'

        Holders.config['keycloak']['realm'] = 'testRealm'
        Holders.config['keycloak']['resource'] = 'testResource'
        Holders.config['keycloak']['auth-server-url'] = 'https://test2.org/auth'
        Holders.config['keycloakOffline']['offlineToken'] = 'testToken'
    }

    @Ignore // FIXME
    void 'test fetching dimension elements from transmart'() {
        def constraints = [type: 'true']
        def impersonatedUser = 'user1'

        when:
        def result = testee.getDimensionElements(dimensionName, constraints, impersonatedUser)
        then:
        result != null
    }

    private RestTemplate mockTemplate() {
        def transmartMockDimensionResponse = new DimensionElementsRepresentation()
        transmartMockDimensionResponse.name = dimensionName
        transmartMockDimensionResponse.elements = []
        transmartMockDimensionResponse.elements.add(["k1":"val11", "k2":"val12", "k13": "val13"])
        transmartMockDimensionResponse.elements.add(["k1":"val21", "k2":"val22"])

        ResponseEntity dimensionResponse = new ResponseEntity(transmartMockDimensionResponse, HttpStatus.OK)
        Mock( {
            exchange("${testee.transmartServerUrl}/v2/dimensions/${dimensionName}/elements", HttpMethod.POST, null, DimensionElementsRepresentation.class) >> dimensionResponse

        })
    }
}
