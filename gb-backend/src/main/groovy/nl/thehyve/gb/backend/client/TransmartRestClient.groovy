/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.client.utils.ImpersonationInterceptor
import nl.thehyve.gb.backend.client.utils.RestTemplateResponseErrorHandler
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Slf4j
@CompileStatic
class TransmartRestClient {

    @Value('${transmart.server-url}')
    private String transmartServerUrl

    @Autowired
    RestTemplate restTemplate

    DimensionElementsRepresentation getDimensionElements(String dimensionName, Map constraint, String impersonatedUserName) {
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.setAccept([MediaType.APPLICATION_JSON])

        Map<String, Object> body = new HashMap<>()
        body.put("dimensionName", dimensionName)
        body.put("constraint", constraint)

        def url = URI.create("$transmartServerUrl/v2/dimensions/${dimensionName}/elements")
        def httpEntity = new HttpEntity(body, headers)
        restTemplate.interceptors.add(new ImpersonationInterceptor(impersonatedUserName))
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler())
        ResponseEntity<DimensionElementsRepresentation> response = restTemplate.exchange(url,
                HttpMethod.POST, httpEntity, DimensionElementsRepresentation.class)

        if (response.statusCode != HttpStatus.OK) {
            throw new InvalidRequestException(response.statusCode.toString())
        }

        return response.body
    }

}
