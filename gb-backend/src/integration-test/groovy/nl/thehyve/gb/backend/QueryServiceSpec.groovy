/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.gorm.transactions.Transactional
import grails.testing.mixin.integration.Integration
import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import nl.thehyve.gb.backend.client.TransmartRestClient
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import nl.thehyve.gb.backend.representation.DimensionPropertiesRepresentation
import nl.thehyve.gb.backend.representation.DimensionType
import nl.thehyve.gb.backend.representation.DimensionsRepresentation
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@Integration(applicationClass = Application)
@Rollback
class QueryServiceSpec extends Specification {

    @Autowired
    QueryService queryService

    @Autowired
    QuerySetService querySetService

    User regularUser
    User adminUser

    QueryRepresentation query1Representation
    QueryRepresentation query2Representation
    QueryRepresentation query3Representation

    void setup() {
        clearDatabase()
        regularUser = new User('fake-user', 'Fake user', false, 'fake@email')
        adminUser = new User('admin', 'Administrator', true, 'fake@email')

        querySetService.transmartRestClient = Mock(TransmartRestClient)
        queryService.transmartRestClient = Mock(TransmartRestClient)

        // for some reason without this line querySetService uses mocked queryService
        querySetService.queryService = queryService

        mockTransmartRestClient()
    }

    @Transactional
    void 'test creating query sets with query set instances when subscribed'() {
        when: 'three queries are saved with subscription'
        queryService.create(query1Representation, regularUser)
        queryService.create(query2Representation, regularUser)
        queryService.create(query3Representation, adminUser)

        then: 'two query set instances have been created'
        def querySets = getAllQuerySets()
        def querySetInstances = getAllQuerySetInstances()
        querySets.size() == 2
        querySetInstances.size() == 4

        assert querySets[0].setSize == 1
        assert querySets[1].setSize == 3

        assert querySetInstances.findAll { it.querySet == querySets[0] }.objectId == ["TEST:21"]
        assert querySetInstances.findAll { it.querySet == querySets[1] }.objectId == ["s1", "s2", "s3"]
    }

    @Transactional
    void 'test throwing exception when using invalid subject dimension for a query'() {
        when: 'a query is created with an invalid dimension'
        def queryWithUnsupportedDimension = query1Representation
        queryWithUnsupportedDimension.subjectDimension = 'unsupported dimension'

        queryService.create(queryWithUnsupportedDimension, regularUser)

        then: 'the invalid argument exception is returned'
        InvalidArgumentsException ex = thrown()
        ex.message == "Subject dimension '${queryWithUnsupportedDimension.subjectDimension}' not supported."
    }

    @Transactional
    List<QuerySetDiff> getAllQuerySetDiffs() {
        return QuerySetDiff.list()
    }

    @Transactional
    List<QuerySetInstance> getAllQuerySetInstances() {
        return QuerySetInstance.list()
    }

    @Transactional
    List<QuerySet> getAllQuerySets() {
        return QuerySet.list()
    }

    private void mockTransmartRestClient() {
        query1Representation = new QueryRepresentation()
        query1Representation.with {
            name = 'test query 1'
            subjectDimension = 'patient'
            queryConstraint = [type: 'true']
            bookmarked = true
            subscribed = true
            subscriptionFreq = SubscriptionFrequency.DAILY
            username = regularUser.username
        }
        query2Representation = new QueryRepresentation()
        query2Representation.with {
            name = 'test query 2'
            subjectDimension = 'diagnosis'
            queryConstraint = [type: 'true']
            bookmarked = true
            subscribed = false
            username = regularUser.username
        }
        query3Representation = new QueryRepresentation()
        query3Representation.with {
            name = 'test query 3'
            subjectDimension = 'sample1'
            queryConstraint = [type: 'negation', arg: [type: 'true']]
            bookmarked = false
            subscribed = true
            subscriptionFreq = SubscriptionFrequency.WEEKLY
            username = adminUser.username
        }
        def dimensionName = 'patient'

        queryService.querySetService.transmartRestClient.getDimensionElements(
                'patient', query1Representation.queryConstraint as Map) >>
                new DimensionElementsRepresentation(
                        name: dimensionName.toLowerCase(),
                        elements: [
                                [
                                        id        : 21L,
                                        subjectIds: ["SUBJ_ID": "TEST:21", "Invalid": "TEST:0"]
                                ]
                        ]
                )
        queryService.querySetService.transmartRestClient.getDimensionElements(
                'sample1', query3Representation.queryConstraint as Map) >>
                new DimensionElementsRepresentation(
                        name: dimensionName.toLowerCase(),
                        elements: [
                                "s1",
                                "s2",
                                "s3"
                        ]
                )

        queryService.transmartRestClient.getDimensions() >>
                new DimensionsRepresentation(
                        dimensions: [
                                new DimensionPropertiesRepresentation(
                                        name: 'patient',
                                        dimensionType: DimensionType.SUBJECT
                                ),
                                new DimensionPropertiesRepresentation(
                                        name: 'sample1',
                                        dimensionType: DimensionType.SUBJECT
                                ),
                                new DimensionPropertiesRepresentation(
                                        name: 'diagnosis',
                                        dimensionType: DimensionType.SUBJECT
                                )
                        ]
                )
    }

    @Transactional
    void clearDatabase() {
        QuerySetInstance.findAll()*.delete(flush: true)
        QuerySetDiff.findAll()*.delete(flush: true)
        QuerySet.findAll()*.delete(flush: true)
        Query.findAll()*.delete(flush: true)
    }

}
