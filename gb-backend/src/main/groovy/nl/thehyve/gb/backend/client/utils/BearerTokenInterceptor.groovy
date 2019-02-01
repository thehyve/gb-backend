/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.client.utils

import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.client.KeycloakRestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

@CompileStatic
class BearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private static String tokenString

    BearerTokenInterceptor(String tokenString) {
        this.tokenString = tokenString
    }

    @Override
    ClientHttpResponse intercept(
            HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpHeaders headers = request.getHeaders()
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenString)
        }
        return execution.execute(request, body)
    }
}
