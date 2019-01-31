/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package config

import base.TestContext

import static groovyx.net.http.HttpBuilder.configure

class Config {

    public static
    final String BASE_URL = System.getProperty('baseUrl') != null ? System.getProperty('baseUrl') : 'http://localhost:8083/'

    // Configure the default TestContext. This is shared between all tests unless it is replaced by a testClass
    public static final TestContext testContext = new TestContext().setHttpBuilder(configure {
        request.uri = BASE_URL
    }).setAuthAdapter(new BasicAuthAdapter())

    // settings
    public static final String DEFAULT_USER = 'test-public-user-1'
    public static final String UNRESTRICTED_USER = 'test-public-user-2'
    public static final String ADMIN_USER = 'admin'

    // Keycloak specific settings
    public static final String AUTH_SERVER_URL = 'http://localhost:8080/'
    public static final String REALM = 'test'
    public static final String RESOURCE = 'transmart'

    public static final String PATH_QUERY = "/queries"
    public static final String PATH_NOTIFICATIONS = "/notifications/notify"
}
