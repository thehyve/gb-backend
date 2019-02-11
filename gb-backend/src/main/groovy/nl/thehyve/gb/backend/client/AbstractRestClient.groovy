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
import nl.thehyve.gb.backend.user.AuthContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
@Slf4j
@CompileStatic
abstract class AbstractRestClient {

    @Autowired
    AuthContext authContext

    private static HttpHeaders getJsonHeaders() {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.setAccept([MediaType.APPLICATION_JSON])
        headers
    }

    protected <T> T postOnBehalfOf(String impersonatedUserName, URI uri, Map<String, Object> body, Class<T> type) {
        log.info "User impersonation! User ${authContext.user.username} " +
                "sending request on behalf of user $impersonatedUserName, requestURL: $uri"
        RestTemplate restTemplate = new RestTemplate()
        restTemplate.interceptors.add(new ImpersonationInterceptor(impersonatedUserName))
        post(uri, body, type)
    }

    protected <T> T post(URI uri, Map<String, Object> body, Class<T> type,
                         RestTemplate restTemplate = new RestTemplate()) throws InvalidRequestException {
        if (restTemplate.interceptors.size() < 1) {
            restTemplate.interceptors.add(new BearerTokenInterceptor(authContext.tokenString))
        }
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler())

        def httpEntity = new HttpEntity(body, jsonHeaders)
        ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.POST, httpEntity, type)

        if (response.statusCode != HttpStatus.OK) {
            throw new InvalidRequestException(response.statusCode.toString())
        }

        return response.body
    }

}
