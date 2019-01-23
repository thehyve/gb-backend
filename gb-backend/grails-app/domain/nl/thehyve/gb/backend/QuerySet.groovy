/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

class QuerySet {

    Long id
    SetType setType
    Long setSize
    Date createDate = new Date()
    Query query

    static belongsTo = Query
    static hasMany = [
            querySetInstances: QuerySetInstance,
            querySetDiffs: QuerySetDiff
    ]

    static mapping = {
        version false
    }

    static constraints = {
        id generator: 'sequence', params: [sequence: 'query_set_id_seq']
        query column: 'query_id'
        querySetInstances batchSize: 1000
        querySetDiffs batchSize: 1000
    }

}
