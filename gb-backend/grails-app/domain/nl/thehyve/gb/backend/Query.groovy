/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

class Query {

    String name
    String subjectDimension
    String username
    String queryConstraint
    Boolean bookmarked = false
    Boolean deleted = false
    Boolean subscribed = false
    SubscriptionFrequency subscriptionFreq
    Date createDate = new Date()
    Date updateDate = new Date()
    String queryBlob

    void updateUpdateDate() {
        updateDate = new Date()
    }

    static mapping = {
        version false
        id generator: 'sequence', params: [sequence: 'query_id_seq']
    }

    static constraints = {
        name maxSize: 1000
        subjectDimension maxSize: 255, nullable: false
        username maxSize: 50
        queryConstraint type: 'text', nullable: false
        bookmarked nullable: true
        subscribed nullable: true
        subscriptionFreq nullable: true
        deleted nullable: true
        createDate nullable: true
        updateDate nullable: true
        queryBlob type: 'text', nullable: true
    }

}
