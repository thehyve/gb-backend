/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

class QuerySetInstance {

    Long id
    String objectId
    QuerySet querySet

    static belongsTo = QuerySet

    static mapping = {
        querySet fetch: 'join'
        version false
    }

    static constraints = {
        id generator: 'sequence', params: [sequence: 'query_set_instance_id_seq']
        querySet column: 'query_set_id'
    }

}
