/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

class UrlMappings {

    static mappings = {
        "/queries"(method: 'GET', controller: 'query', action: 'index')

        "/queries/$id"(method: 'GET', controller: 'query', action: 'get')

        "/queries"(method: 'POST', controller: 'query', action: 'save')

        "/queries/$id"(method: 'PUT', controller: 'query', action: 'update')

        "/queries/$id"(method: 'DELETE', controller: 'query', action: 'delete')

        "/dimensions/$dimensionName/elements"(method: 'POST', controller: 'querySet', action: 'dimensionElements')

    }
}
