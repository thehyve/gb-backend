/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.user

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.client.KeycloakRestClient
import nl.thehyve.gb.backend.exception.NoSuchResourceException
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount
import org.keycloak.representations.AccessToken
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

import java.security.Principal

@Slf4j
@CompileStatic
class UserService {

    public static final String ROLE_ADMIN = 'ROLE_ADMIN'

    @Autowired
    KeycloakRestClient keycloakRestClient

    User getUserFromPrincipal(Principal principal) throws IllegalArgumentException {
        assert principal instanceof Authentication
        if (!(principal as Authentication).authenticated) {
            throw new IllegalArgumentException("${principal.name} principal has authenticated flag set to false.")
        }

        final String username = principal.name
        List<String> authorities = (principal as Authentication).authorities.collect {
            GrantedAuthority ga -> ga.authority
        }
        final boolean admin = authorities.remove(ROLE_ADMIN)

        String realName = null
        String email = null
        if ((principal as Authentication).details instanceof SimpleKeycloakAccount) {
            def context = ((SimpleKeycloakAccount) (principal as Authentication).details).keycloakSecurityContext
            if (context?.token) {
                AccessToken token = context.token
                realName = token.name
                email = token.email
            } else {
                log.debug("No token in the security context. Giving up on getting email and name.")
            }
        } else {
            log.debug("The details field of unexpected type: ${(principal as Authentication).details.class}. " +
                    "Giving up on getting email and name.")
        }

        new User(username, realName, admin, email)
    }

    User getUserFromUsername(String username) throws NoSuchResourceException {
        User user = getUsers()?.find { it.username == username }
        if (!user) {
            throw new NoSuchResourceException("No user with '${username}' username found.")
        }
        user
    }

    List<User> getUsersWithEmailSpecified() {
        getUsers()?.findAll { it.email }
    }

    List<User> getUsers() {
        List<UserRepresentation> keycloakUsers = keycloakRestClient.getUsersByOfflineToken()
        if (keycloakUsers == null) {
            return null
        }
        keycloakUsers.collect { UserRepresentation keycloakUser ->
            Set<String> roles = keycloakRestClient.getRolesForUserByOfflineToken(keycloakUser.id)
            createUser(keycloakUser, roles)
        }
    }

    private static User createUser(UserRepresentation keycloakUser, Set<String> roles) {
        final boolean admin = roles.remove(ROLE_ADMIN)
        new User(keycloakUser.id,
                "$keycloakUser.firstName $keycloakUser.lastName",
                admin,
                keycloakUser.email
        )
    }

}
