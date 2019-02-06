/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import grails.test.mixin.integration.Integration
import nl.thehyve.gb.backend.client.TransmartRestClient
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import nl.thehyve.gb.backend.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

import static nl.thehyve.gb.backend.QueryService.toRepresentation

@Integration(applicationClass = Application)
@Rollback
class QuerySetServiceSpec extends Specification {

    @Autowired
    QuerySetService testee

    User regularUser
    User adminUser

    void setup() {
        clearDB()

        regularUser = new User('fake-user', 'Fake user', false, 'fake@email')
        adminUser = new User('admin', 'Administrator', true, 'admin@email')

        testee.queryService = Mock(QueryService)
        testee.transmartRestClient = Mock(TransmartRestClient)
    }

    @NotTransactional
    //To make sure transaction does not propagate on the scan (testee) method
    void 'test scanning for query set changes'() {
        def querySetsBeforeScan = 3
        def queries = getMockQueries()
        mockQueryService(queries)
        mockTransmartRestClient(queries)
        // prepare query sets for mock queries
        queries.forEach { testee.createQuerySetWithQueryInstances(toRepresentation(it)) }

        when: 'admin user triggers computing query diffs'
        def result = testee.scan()
        def querySetsNumber = getAllQuerySets().size()
        def querySetChanges = testee.getQueryChangeHistory(queries[1].id, regularUser, 999)
        def querySetInstances = getAllQuerySetInstances()
        def setDiffs = getAllQuerySetDiffs()

        then: 'Only two queries got new patients. The failing query did not stop the process.'
        result == 2
        querySetsNumber == querySetsBeforeScan + 2
        // check query history
        querySetChanges.size() == 2
        querySetInstances.size() == 3 + 4 // old instances + new instances

        setDiffs.size() == 4
        setDiffs.count { it.changeFlag == ChangeFlag.ADDED } == 3
        setDiffs.count { it.changeFlag == ChangeFlag.REMOVED } == 1

        when: 'checking querySet changes for an email with daily updates'
        def resultForDailySubscription = testee
                .getQueryChangeHistoryByUsernameAndFrequency(SubscriptionFrequency.DAILY, regularUser.username, 20)

        then: 'No elements found to be send in the daily email'
        resultForDailySubscription.size() == 1

        when: 'checking querySet changes for the email with weekly updates'
        def resultForWeeklySubscription = testee
                .getQueryChangeHistoryByUsernameAndFrequency(SubscriptionFrequency.WEEKLY, regularUser.username, 20)

        then: 'No elements found to be send in the weekly email'
        resultForWeeklySubscription.size() == 0

    }

    private void mockTransmartRestClient(List<Query> queries) {
        def queriesRepresentations = queries.collect{ toRepresentation(it) }

        testee.transmartRestClient.getDimensionElements(
                'patient', queriesRepresentations[0].queryConstraint as Map, queries[0].username) >>
                new InvalidRequestException("Transmart returned error status")
        testee.transmartRestClient.getDimensionElements(
                'diagnosis', queriesRepresentations[1].queryConstraint as Map, queries[1].username) >>
                new DimensionElementsRepresentation(
                        name: 'diagnosis',
                        elements: [
                                [
                                        id        : 20L,
                                        subjectIds: ["SUBJ_ID": "TEST:20"]
                                ],
                                [
                                        id        : 21L,
                                        subjectIds: ["SUBJ_ID": "TEST:21"]
                                ],
                                [
                                        id        : 22L,
                                        subjectIds: ["SUBJ_ID": "TEST:22"]
                                ]
                        ]
                )
        testee.transmartRestClient.getDimensionElements(
                'patient', queriesRepresentations[2].queryConstraint as Map, queries[2].username) >>
                new DimensionElementsRepresentation(
                        name: 'patient',
                        elements: [
                                [
                                        id        : 30L,
                                        subjectIds: ["SUBJ_ID": "TEST:30"]
                                ]
                        ]
                )
        testee.transmartRestClient.getDimensionElements(
                'not_requested_dimension', queriesRepresentations[2].queryConstraint as Map, queries[2].username) >>
                new DimensionElementsRepresentation(
                        name: 'not_requested_dimension'.toLowerCase(),
                        elements: [
                                [
                                        id        : 40L,
                                        subjectIds: ["SUBJ_ID": "TEST:30"]
                                ]
                        ]
                )
        testee.transmartRestClient.getDimensionElements(_, _) >>
                new DimensionElementsRepresentation(
                        name: 'some dimension',
                        elements: [
                                [
                                        id        : 21L,
                                        subjectIds: ["SUBJ_ID": "TEST:21"]
                                ]
                        ]
                )
    }

    private void mockQueryService(List<Query> queries) {
        def queriesRepresentations = queries.collect{ toRepresentation(it) }

        testee.queryService.getQueryById(queries[0].id) >> queries[0]
        testee.queryService.getQueryById(queries[1].id) >> queries[1]
        testee.queryService.getQueryById(queries[2].id) >> queries[2]

        testee.queryService.getQueriesSubscribedAndNotDeleted() >> queries

        testee.queryService.getQueryRepresentationByIdAndUsername(queries[0].id, regularUser) >> queriesRepresentations[0]
        testee.queryService.getQueryRepresentationByIdAndUsername(queries[1].id, regularUser) >> queriesRepresentations[1]
        testee.queryService.getQueryRepresentationByIdAndUsername(queries[2].id, adminUser) >> queriesRepresentations[2]
    }

    @Transactional
    private List<Query> getMockQueries() {
        def query1_invalid = new Query()
        query1_invalid.with {
            name = 'fail on scan query'
            type = 'patient'
            queryConstraint = '{"type": "concept", "conceptCode": "NON-EXISTENT"}'
            bookmarked = true
            subscribed = true
            subscriptionFreq = SubscriptionFrequency.DAILY
            username = regularUser.username
        }
        def query2 = new Query()
        query2.with {
            name = 'test query 1'
            type = 'diagnosis'
            queryConstraint = '{"type": "true"}'
            bookmarked = true
            subscribed = true
            subscriptionFreq = SubscriptionFrequency.DAILY
            username = regularUser.username
        }
        def query3 = new Query()
        query3.with {
            name = 'test query 2'
            type = 'patient'
            queryConstraint = '{"type": "negation", "arg": {"type": "true"}}'
            bookmarked = false
            subscribed = true
            subscriptionFreq = SubscriptionFrequency.WEEKLY
            username = adminUser.username
        }
        [query1_invalid, query2, query3]*.save(flush: true)
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

    @Transactional
    void clearDB() {
        QuerySetInstance.findAll()*.delete(flush: true)
        QuerySetDiff.findAll()*.delete(flush: true)
        QuerySet.findAll()*.delete(flush: true)
        Query.findAll()*.delete(flush: true)
    }

}



