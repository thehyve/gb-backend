/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package base

import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import groovyx.net.http.OkHttpEncoders
import com.fasterxml.jackson.databind.ObjectMapper

import static config.Config.DEFAULT_USER

class RestHelper {

    static delete(TestContext testContext, Map requestMap) {
        HttpBuilder http = testContext.getHttpBuilder()

        http.delete {
            request.uri.path = requestMap.path
            request.uri.query = requestMap.query

            testContext.getAuthAdapter().authenticate(getRequest(), (requestMap.user ?: DEFAULT_USER))

            response.success { FromServer fromServer, body ->
                assert fromServer.statusCode == (requestMap.statusCode ?: 200): "Unexpected status code. expected: " +
                        "${requestMap.statusCode ?: 200} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }

            response.failure { FromServer fromServer, body ->
                assert fromServer.statusCode == requestMap.statusCode: "Unexpected status code. expected: " +
                        "${requestMap.statusCode} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }
        }
    }

    static put(TestContext testContext, Map requestMap) {
        HttpBuilder http = testContext.getHttpBuilder()

        http.put {
            request.uri.path = requestMap.path
            request.uri.query = requestMap.query
            request.accept = requestMap.acceptType ?: ContentTypeFor.JSON
            request.contentType = requestMap.contentType ?: ContentTypeFor.JSON
            request.body = requestMap.body

            testContext.getAuthAdapter().authenticate(getRequest(), (requestMap.user ?: DEFAULT_USER))

            response.success { FromServer fromServer, body ->
                assert fromServer.statusCode == (requestMap.statusCode ?: 200): "Unexpected status code. expected: " +
                        "${requestMap.statusCode ?: 200} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }

            response.failure { FromServer fromServer, body ->
                assert fromServer.statusCode == requestMap.statusCode: "Unexpected status code. expected: " +
                        "${requestMap.statusCode} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }
        }
    }

    static post(TestContext testContext, Map requestMap) {
        HttpBuilder http = testContext.getHttpBuilder()

        http.post {
            request.uri.path = requestMap.path
            request.uri.query = requestMap.query
            request.accept = requestMap.acceptType ?: ContentTypeFor.JSON
            request.contentType = requestMap.contentType ?: ContentTypeFor.JSON
            request.body = requestMap.body

            if(requestMap.contentType == 'multipart/form-data') {
                request.encoder 'multipart/form-data', OkHttpEncoders.&multipart
            }

            testContext.getAuthAdapter().authenticate(getRequest(), (requestMap.user ?: DEFAULT_USER))

            response.success { FromServer fromServer, body ->
                assert fromServer.statusCode == (requestMap.statusCode ?: 200): "Unexpected status code. expected: " +
                        "${requestMap.statusCode ?: 200} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }

            response.failure { FromServer fromServer, body ->
                assert fromServer.statusCode == requestMap.statusCode: "Unexpected status code. expected: " +
                        "${requestMap.statusCode} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }
        }
    }

    static get(TestContext testContext, Map requestMap) {
        HttpBuilder http = testContext.getHttpBuilder()

        http.get {
            request.uri.path = requestMap.path
            request.uri.query = requestMap.query
            request.accept = requestMap.acceptType ?: ContentTypeFor.JSON

            testContext.getAuthAdapter().authenticate(getRequest(), (requestMap.user ?: DEFAULT_USER))

            response.success { FromServer fromServer, body ->
                assert fromServer.statusCode == (requestMap.statusCode ?: 200): "Unexpected status code. expected: " +
                        "${requestMap.statusCode ?: 200} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }

            response.failure { FromServer fromServer, body ->
                assert fromServer.statusCode == (requestMap.statusCode ?: 400): "Unexpected status code. expected: " +
                        "${requestMap.statusCode} but was ${fromServer.statusCode}. \n" +
                        printResponse(fromServer, body)
                body
            }
        }
    }

    static printResponse(FromServer fromServer, body) {
        return "from server: ${fromServer.uri} ${fromServer.statusCode}\n" +
                "${body}\n"
    }

    static <T> T toObject(Object response, Class<T> type) {
        def mapper = new ObjectMapper()
        def serialisedObject = mapper.writeValueAsString(response)
        mapper.readValue(serialisedObject, type)
    }
}
