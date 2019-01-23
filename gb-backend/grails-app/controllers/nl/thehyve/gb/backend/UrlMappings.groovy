/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

class UrlMappings {

    static mappings = {
        // query management:
        "/queries"(method: 'GET', controller: 'query', action: 'index')
        "/queries/$id"(method: 'GET', controller: 'query', action: 'get')
        "/queries"(method: 'POST', controller: 'query', action: 'save')
        "/queries/$id"(method: 'PUT', controller: 'query', action: 'update')
        "/queries/$id"(method: 'DELETE', controller: 'query', action: 'delete')

        // query subscription:
        "/queries/sets/scan"(method: 'POST', controller: 'querySet', action: 'scan')
        "/queries/$queryId/sets"(method: 'GET', controller: 'querySet', action: 'getSetChangesByQueryId')

    }
}
