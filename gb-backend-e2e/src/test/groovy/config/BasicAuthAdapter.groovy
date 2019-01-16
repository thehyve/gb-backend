/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package config

import base.AuthAdapter
import groovyx.net.http.HttpConfig

import static groovyx.net.http.HttpBuilder.configure

class BasicAuthAdapter implements AuthAdapter {

    private static HashMap<String, User> users = [:]

    BasicAuthAdapter() {
        users.put('test-public-user-1', new User('test-public-user-1', 'test-public-user-1'))
        users.put('test-public-user-2', new User('test-public-user-2', 'test-public-user-2'))
        users.put('admin', new User('admin', 'admin'))
    }

    @Override
    void authenticate(HttpConfig.Request request, String userID) {
        request.headers.'Authorization' = 'Bearer ' + getToken(userID)
    }

    static String requestToken(User user) {
        configure {
            request.uri = Config.AUTH_SERVER_URL
        }.post() {
            request.uri.path = "/auth/realms/$Config.REALM/protocol/openid-connect/token"
            request.contentType = "application/x-www-form-urlencoded"
            request.body = ['grant_type': 'password', 'client_id': Config.RESOURCE, 'username': user.username, 'password': user.password]
        }.access_token
    }

    static String getToken(String userID) {
        def user = getUser(userID)
        if (!user.token) {
            user.token = requestToken(user)
        }
        user.token
    }

    static User getUser(String userID) {
        if (!users.get(userID)) {
            throw new MissingResourceException("the user with id ${userID} is not definded in OauthAdapter.users", 'User', userID)
        }
        users.get(userID)
    }

    class User {
        String username
        String password
        String token

        User(String username, String password) {
            this.username = username
            this.password = password
            this.token = null
        }
    }
}
