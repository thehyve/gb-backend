/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.user

import groovy.transform.CompileStatic
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount
import org.keycloak.representations.AccessToken
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication

import java.security.Principal

@CompileStatic
class UserService {

    @Value('${keycloak.realm}')
    String realm

    @Value('${keycloak.resource}')
    String clientId

    @Value('${keycloak.auth-server-url}')
    String keycloakServerUrl

    User getUserFromPrincipal(Principal principal) {
        assert principal instanceof Authentication
        if (!(principal as Authentication).authenticated) {
            throw new IllegalArgumentException("${principal.name} principal has authenticated flag set to false.")
        }

        final String username = principal.name

        String realName = null
        if ((principal as Authentication).details instanceof SimpleKeycloakAccount) {
            def context = ((SimpleKeycloakAccount) (principal as Authentication).details).keycloakSecurityContext
            if (context?.token) {
                AccessToken token = context.token
                realName = token.name
            } else {
                log.debug("No token in the security context. Giving up on getting email and name.")
            }
        } else {
            log.debug("The details field of unexpected type: ${(principal as Authentication).details.class}. " +
                    "Giving up on getting email and name.")
        }

        new User(username, realName)
    }

}
