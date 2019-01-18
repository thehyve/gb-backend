/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.user

import org.keycloak.adapters.RefreshableKeycloakSecurityContext
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.keycloak.adapters.tomcat.SimplePrincipal
import org.keycloak.representations.AccessToken
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification

class UserServiceSpec extends Specification {

    UserService testee

    def setup() {
        testee = new UserService()
    }

    void "test keycloak principal parsing"() {
        List<GrantedAuthority> authorities = [
                new SimpleGrantedAuthority('ROLE_1')
        ]

        def principal = new TestingAuthenticationToken('test-sub', 'test-password', authorities)
        principal.authenticated = true

        when:
        User user = testee.getUserFromPrincipal(principal)

        then:
        user.username == 'test-sub'
        user.realName == null

    }

    void 'work with authenticated principals only'() {
        def principal = new TestingAuthenticationToken('test', 'test-psw')
        principal.authenticated = false

        when:
        testee.getUserFromPrincipal(principal)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'test principal has authenticated flag set to false.'
    }

    void 'full name has parsed correctly from the keycloak token'() {
        def context = new RefreshableKeycloakSecurityContext()
        context.token = new AccessToken(name: 'Test User')
        def token = new KeycloakAuthenticationToken(new SimpleKeycloakAccount(
                new SimplePrincipal('test-principal'), [] as Set, context), true, [])

        when:
        User user = testee.getUserFromPrincipal(token)

        then:
        user.realName == 'Test User'
    }

}
