/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * Spring security based authentication.
 * Has to live in the request or session scope to prevent sharing state (potentially) between user.
 */
@Component
@Scope(value = 'request', proxyMode = ScopedProxyMode.TARGET_CLASS)
class SpringSecurityAuthContext implements AuthContext {

    @Autowired
    private UserService userService

    @Override
    User getUser() {
        userService.getUserFromPrincipal(getAuthentication())
    }

    @Override
    String toString() {
        "SpringSecurityAuthContext(${getAuthentication().name})"
    }

    protected Authentication getAuthentication() {
        SecurityContextHolder.context.authentication
    }

}
