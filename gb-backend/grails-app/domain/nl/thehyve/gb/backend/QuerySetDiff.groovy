/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

class QuerySetDiff {

    Long id
    String objectId
    QuerySet querySet
    ChangeFlag changeFlag

    static belongsTo = QuerySet

    static mapping = {
        querySet fetch: 'join'
        version false
    }

    static constraints = {
        id generator: 'sequence', params: [sequence: 'query_set_diff_id_seq']
        querySet column: 'query_set_id'
    }

}
