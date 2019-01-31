/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.client.utils.BearerTokenInterceptor
import nl.thehyve.gb.backend.client.utils.ImpersonationInterceptor
import nl.thehyve.gb.backend.client.utils.RestTemplateResponseErrorHandler
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import nl.thehyve.gb.backend.representation.PatientRepresentation
import nl.thehyve.gb.backend.user.AuthContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Slf4j
@CompileStatic
class TransmartRestClient extends AbstractRestClient {

    @Value('${transmart.server-url}')
    private String transmartServerUrl

    @Value('${transmart.api-version}')
    private String transmartApiVersion

    /**
     * Request for all elements from a dimension of given name that satisfy the constraint if given.
     * POST /v2/dimensions/${dimensionName}/elements
     *
     * @param dimensionName
     * @param constraint - json that specifies the constraint
     * @param impersonatedUserName - optional, only for administrators!
     *                               name of the user that on whose behalf the request is executed.
     *
     * @return a list of all dimension elements that user (or impersonated user) has access to.
     */
    DimensionElementsRepresentation getDimensionElements(String dimensionName, Map constraint, String impersonatedUserName = '') {
        URI uri = createURI("dimensions/${dimensionName}/elements")

        Map<String, Object> body = new HashMap<>()
        body.put("dimensionName", dimensionName)
        body.put("constraint", constraint)

        if(impersonatedUserName?.trim()) {
            postOnBehalfOf(impersonatedUserName, uri, body, DimensionElementsRepresentation.class)
        } else {
            post(uri, body, DimensionElementsRepresentation.class)
        }
    }

    private URI createURI(String urlParts) {
        URI.create("$transmartServerUrl/$transmartApiVersion/$urlParts")
    }
}
