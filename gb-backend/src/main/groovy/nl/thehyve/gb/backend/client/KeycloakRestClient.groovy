/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.exception.ServiceNotAvailableException
import org.keycloak.representations.AccessTokenResponse
import org.keycloak.representations.idm.ClientMappingsRepresentation
import org.keycloak.representations.idm.MappingsRepresentation
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

@Component
@Slf4j
@CompileStatic
class KeycloakRestClient extends AbstractRestClient {

    private static String realm = Holders.config['keycloak']['realm']
    private static String clientId = Holders.config['keycloak']['resource']
    private static String keycloakServerUrl = Holders.config['keycloak']['auth-server-url']
    private static String offlineToken = Holders.config['keycloakOffline']['offlineToken']

    Set<String> getRolesForUserByOfflineToken(String userId) {
        String accessToken = getAccessTokenByOfflineTokenAndClientId()
        getRoles(userId, accessToken)
    }

    List<UserRepresentation> getUsersByOfflineToken() {
        String accessToken = getAccessTokenByOfflineTokenAndClientId()
        getUsers(accessToken)
    }

    String getImpersonatedTokenByOfflineTokenForUser(String impersonatedUserName) {
        String offlineAccessToken = getAccessTokenByOfflineTokenAndClientId()
        exchangeToken(offlineAccessToken, impersonatedUserName)
    }

    static ParameterizedTypeReference<List<UserRepresentation>> userListRef =
            new ParameterizedTypeReference<List<UserRepresentation>>() {}

    private List<UserRepresentation> getUsers(String accessToken) {
        RestTemplate template = getRestTemplateWithAuthorizationToken(accessToken)
        ResponseEntity<List<UserRepresentation>> response = template
                .exchange("${keycloakServerUrl}/admin/realms/${realm}/users".toString(), HttpMethod.GET, null, userListRef)
        if (response.statusCode == HttpStatus.OK) {
            return response.body
        }
        throw new ServiceNotAvailableException("Could not fetch list of users. Status: ${response.statusCode}")
    }

    private Set<String> getRoles(String userId, String accessToken) {
        RestTemplate template = getRestTemplateWithAuthorizationToken(accessToken)
        ResponseEntity<MappingsRepresentation> response = template.getForEntity(
                "$keycloakServerUrl/admin/realms/$realm/users/$userId/role-mappings".toString(),
                MappingsRepresentation.class)

        Map<String, ClientMappingsRepresentation> rolesPerClient = response.body.clientMappings
        Set<String> roles = []
        ClientMappingsRepresentation clientMappings = rolesPerClient.get(clientId)
        if (clientMappings) {
            for (RoleRepresentation roleRepresentation: clientMappings.mappings) {
                roles.add(roleRepresentation.name)
            }
        } else {
            log.debug("No client role mappings for $clientId client were found.")
        }
        log.debug("${userId} user has following roles on ${clientId} client: ${roles}.")

        roles
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
