/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.client.utils.BearerTokenInterceptor
import nl.thehyve.gb.backend.client.utils.ImpersonationInterceptor
import nl.thehyve.gb.backend.client.utils.RestTemplateResponseErrorHandler
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.user.AuthContext
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

import java.security.cert.X509Certificate

@Component
@Slf4j
@CompileStatic
abstract class AbstractRestClient {

    @Autowired
    AuthContext authContext

    /**
     * Do not set this flag to true in production!
     */
    @Lazy
    private Boolean disableTrustManager = {
        Holders.config.getProperty('disable-trust-manager', Boolean, false)
    }()

    private static HttpHeaders getJsonHeaders() {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.setAccept([MediaType.APPLICATION_JSON])
        headers
    }

    protected <T> T postOnBehalfOf(String impersonatedUserName, URI uri, Map<String, Object> body, Class<T> type) {
        log.info "User impersonation! User ${authContext.user.username} " +
                "sending request on behalf of user $impersonatedUserName, requestURL: $uri"
        post(uri, body, type, getRestTemplateWithAuthorizationOnBehalfOf(impersonatedUserName))
    }

    protected <T> T postAsCurrentUser(URI uri, Map<String, Object> body, Class<T> type) {
        log.debug "Sending authorised post request from ${authContext.user.username} user."
        post(uri, body, type, getRestTemplateWithAuthorizationToken(authContext.tokenString))
    }

    protected static <T> T post(URI uri, Map<String, Object> body, Class<T> type, RestTemplate restTemplate) throws InvalidRequestException {

        def httpEntity = new HttpEntity(body, jsonHeaders)
        ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.POST, httpEntity, type)

        if (response.statusCode != HttpStatus.OK) {
            throw new InvalidRequestException(response.statusCode.toString())
        }

        return response.body
    }

    protected <T> T getAsCurrentUser(URI uri, Class<T> type) {
        log.debug "Sending authorised get request from ${authContext.user.username} user."
        get(uri, type, getRestTemplateWithAuthorizationToken(authContext.tokenString))
    }

    protected static <T> T get(URI uri, Class<T> type, RestTemplate restTemplate) throws InvalidRequestException {

        def httpEntity = new HttpEntity(jsonHeaders)
        ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.GET, httpEntity, type)

        if (response.statusCode != HttpStatus.OK) {
            throw new InvalidRequestException(response.statusCode.toString())
        }

        return response.body
    }

    protected RestTemplate getRestTemplateWithAuthorizationToken(String userToken) {
        def restTemplate = getRestTemplate()
        restTemplate.interceptors.add(new BearerTokenInterceptor(userToken))
        return restTemplate
    }

    protected RestTemplate getRestTemplateWithAuthorizationOnBehalfOf(String impersonatedUserName) {
        def restTemplate = getRestTemplate()
        restTemplate.interceptors.add(new ImpersonationInterceptor(impersonatedUserName))
        return restTemplate
    }

    protected RestTemplate getRestTemplate() {
        def requestFactory = new HttpComponentsClientHttpRequestFactory()
        if (disableTrustManager) {
            requestFactory.setHttpClient(httpClientWithoutCertificateChecking)
        }
        def restTemplate = new RestTemplate(requestFactory)
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler())
        return restTemplate
    }

    private static HttpClient getHttpClientWithoutCertificateChecking() {
        log.warn "SSL certificate checking is disabled!"
        def acceptingTrustStrategy = { X509Certificate[] chain, String authType -> true }
        def sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build()
        HttpClients.custom()
                .setSSLContext(sslContext)
                .build()
    }

}
