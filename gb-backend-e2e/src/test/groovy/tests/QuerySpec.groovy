/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package tests

import base.ContentTypeFor
import base.ErrorResponse
import base.RESTSpec
import base.RestHelper
import config.Config


/**
 *  CRUD endpoint for user queries.
 */
class QuerySpec extends RESTSpec {

    def "list queries"() {
        createQuery()

        when:
        def getResponseData = getQueriesForUser()

        then:
        !getResponseData.queries.empty
    }

    def "get query"() {
        Long id = createQuery(Config.DEFAULT_USER).id

        when:
        def responseData = getQuery(id)

        then:
        responseData.id == id

        when: 'trying to access the query by different user'
        def responseData2 = getQuery(id, 404, Config.UNRESTRICTED_USER)

        then:
        responseData2.error == "Query with id ${id} not found for user."
    }

    def "save query"() {
        when:
        def responseData = createQuery()

        then:
        !('username' in responseData)
        !('deleted' in responseData)
        responseData.id != null
        responseData.name == 'test query'
        responseData.queryConstraint.type == 'true'
        responseData.apiVersion != null
        responseData.bookmarked == true
        responseData.subscribed == false

        responseData.createDate.endsWith('Z')
        responseData.updateDate.endsWith('Z')
    }

    def "save query without constraints"() {
        when:
        def responseData = RestHelper.toObject post([
                path      : Config.PATH_QUERY,
                acceptType: ContentTypeFor.JSON,
                user      : Config.DEFAULT_USER,
                statusCode: 400,
                body      : [
                        name             : 'test query',
                        bookmarked       : true,
                        subscribed       : false,
                ],
        ]), ErrorResponse

        then:
        responseData.message == '1 error(s): queryConstraint: may not be null'
    }

    def "update query"() {
        Long id = createQuery(Config.DEFAULT_USER).id

        when:
        def updatedQuery = put([
                path      : "${Config.PATH_QUERY}/${id}",
                acceptType: ContentTypeFor.JSON,
                user      : Config.DEFAULT_USER,
                statusCode: 200,
                body      : [
                        name             : 'test query 2',
                        bookmarked       : false,
                        subscribed       : false,
                ],
        ])

        then:
        updatedQuery.name == 'test query 2'
        updatedQuery.bookmarked == false
        updatedQuery.subscribed == false

        when: 'try to update query by a different user'
        def updateResponseData1 = RestHelper.toObject put([
                path      : "${Config.PATH_QUERY}/${id}",
                acceptType: ContentTypeFor.JSON,
                user      : Config.UNRESTRICTED_USER,
                statusCode: 404,
                body      : [
                        bookmarked: true
                ],
        ]), ErrorResponse

        then:
        updateResponseData1.error == "Query with id ${id} not found for user."
    }

    def "delete query"() {
        Long id = createQuery(Config.DEFAULT_USER).id

        when:
        def forbidDeleteResponseData = delete([
                path      : "${Config.PATH_QUERY}/${id}",
                acceptType: ContentTypeFor.JSON,
                user      : Config.UNRESTRICTED_USER,
                statusCode: 404,
        ])
        then:
        forbidDeleteResponseData.error == "Query with id ${id} not found for user."

        when:
        def deleteResponseData = delete([
                path      : "${Config.PATH_QUERY}/${id}",
                acceptType: ContentTypeFor.JSON,
                user      : Config.DEFAULT_USER,
                statusCode: 204,
        ])

        then:
        deleteResponseData == null
        getQuery(id, 404).error == "Query with id ${id} not found for user."
        !getQueriesForUser().queries.any { it.id == id }
    }

    def createQuery(String user = Config.DEFAULT_USER) {
        post([
                path      : Config.PATH_QUERY,
                acceptType: ContentTypeFor.JSON,
                user      : user,
                statusCode: 201,
                body      : [
                        name             : 'test query',
                        queryConstraint  : [type: 'true'],// TODO replace with TrueConstraint representation from tm-core-api
                        bookmarked       : true,
                        subscribed       : false,
                        queryBlob        : [
                                patientsQueryFull: [
                                        constraint: [type: 'true']
                                ]
                        ]
                ],
        ])
    }

    def getQuery(Long id, Integer statusCode = 200, String user = Config.DEFAULT_USER) {
        get([
                path      : "${Config.PATH_QUERY}/${id}",
                acceptType: ContentTypeFor.JSON,
                user      : user,
                statusCode: statusCode,
        ])
    }

    def getQueriesForUser() {
        get([
                path      : Config.PATH_QUERY,
                acceptType: ContentTypeFor.JSON,
                user      : Config.DEFAULT_USER,
                statusCode: 200,
        ])
    }
}
