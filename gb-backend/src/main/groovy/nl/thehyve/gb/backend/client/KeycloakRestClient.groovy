/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.keycloak.representations.AccessTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

import java.security.cert.X509Certificate

@Component
@Slf4j
@CompileStatic
class KeycloakRestClient {

    private static String realm = Holders.config['keycloak']['realm']
    private static String clientId = Holders.config['keycloak']['resource']
    private static String keycloakServerUrl = Holders.config['keycloak']['auth-server-url']
    private static String offlineToken = Holders.config['keycloakOffline']['offlineToken']

    /**
     * Do not set this flag to true in production!
     */
    @Lazy
    private Boolean keycloakDisableTrustManager = {
        Holders.config.getProperty('keycloak.disable-trust-manager', Boolean, false)
    }()


    String getImpersonatedTokenByOfflineTokenForUser(String impersonatedUserName) {
        String offlineAccessToken = getAccessTokenByOfflineTokenAndClientId()
        exchangeToken(offlineAccessToken, impersonatedUserName)
    }

    private static HttpClient getHttpClientWithoutCertificateChecking() {
        log.warn "SSL certificate checking for Keycloak is disabled!"
        def acceptingTrustStrategy = { X509Certificate[] chain, String authType -> true }
        def sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build()
        HttpClients.custom()
                .setSSLContext(sslContext)
                .build()
    }

    private RestTemplate getRestTemplate() {
        def requestFactory = new HttpComponentsClientHttpRequestFactory()
        if (keycloakDisableTrustManager) {
            requestFactory.setHttpClient(httpClientWithoutCertificateChecking)
        }
        new RestTemplate(requestFactory)
    }

    /**
     * Exchange current session user’s token for another (impersonated) user’s token
     * @param token current user’s token
     * @param user name of the impersonated user
     * @return access token
     */
    private String exchangeToken(String token, String user) {

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        headers.setAccept([MediaType.APPLICATION_JSON])

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>()
        body.add("client_id", clientId)
        body.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
        body.add("requested_subject", user)
        body.add("subject_token", token)

        def url = URI.create("$keycloakServerUrl/realms/$realm/protocol/openid-connect/token")
        def httpEntity = new HttpEntity(body, headers)
        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(url,
                HttpMethod.POST, httpEntity, AccessTokenResponse.class)

        response.body.token
    }

    /**
     * Get access token from Keycloak based on the client_id and the offline token, which is stored in the config.
     * Offline token is a type of a classic Refresh token, but it never expires.
     * @return access token
     */
    private String getAccessTokenByOfflineTokenAndClientId(){
        HttpHeaders headers = new HttpHeaders()
        headers.setAccept([MediaType.APPLICATION_JSON])

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>()
        body.add("grant_type", "refresh_token")
        body.add("client_id", clientId)
        body.add("scope", "offline_access")
        body.add("refresh_token", offlineToken)

        def url = URI.create("$keycloakServerUrl/realms/$realm/protocol/openid-connect/token")
        def httpEntity = new HttpEntity(body, headers)
        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(url,
                HttpMethod.POST, httpEntity, AccessTokenResponse.class)

        response.body.token
    }

}
