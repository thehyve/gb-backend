/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client

import com.google.common.net.UrlEscapers
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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
        String escapedDimensionName = UrlEscapers.urlPathSegmentEscaper().escape(dimensionName)
        URI uri = URI.create("$transmartServerUrl/$transmartApiVersion/dimensions/${escapedDimensionName}/elements")

        Map<String, Object> body = new HashMap<>()
        body.put("dimensionName", dimensionName)
        body.put("constraint", constraint)

        if(impersonatedUserName?.trim()) {
            postOnBehalfOf(impersonatedUserName, uri, body, DimensionElementsRepresentation.class)
        } else {
            post(uri, body, DimensionElementsRepresentation.class)
        }
    }
}
