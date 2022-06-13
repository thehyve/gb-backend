/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.user

import nl.thehyve.gb.backend.NotificationsMailService
import nl.thehyve.gb.backend.QuerySetService
import nl.thehyve.gb.backend.SubscriptionFrequency
import nl.thehyve.gb.backend.representation.QuerySetChangesRepresentation
import spock.lang.Specification

import java.text.SimpleDateFormat

class NotificationsMailServiceSpec extends Specification {

    NotificationsMailService testee

    def setup() {
        testee = new NotificationsMailService()
        testee.querySetService = Mock(QuerySetService)
        mockQuerySetService()
    }

    void "test grouping of queries change representations"() {

        when:
        Map<String, List<QuerySetChangesRepresentation>> result = testee.getQueryTypeToQuerySetChangesRepresentations(SubscriptionFrequency.DAILY, 'user1')

        then:
        result.size() == 3
        result.keySet().containsAll(['diagnosis', 'custom sample', 'patient'])
        result['diagnosis'].size() == 3
        result['custom sample'].size() == 1
        result['patient'].queryId == [36, 41]

        // test queries order
        result['diagnosis'].queryId == [37, 38, 40]
        result['custom sample'].queryId == [39]
        result['patient'].size() == 2
    }

    private void mockQuerySetService() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        testee.querySetService.getQueriesChangeHistoriesByUsernameAndFrequency( _ as String, _ as SubscriptionFrequency, _) >> [
                new QuerySetChangesRepresentation(
                        queryId: 37,
                        queryName: 'test query1',
                        createDate: format.parse('2015-12-17 06:33'),
                        queryType: 'diagnosis',
                        objectsAdded: ['d1', 'd2', 'd3'],
                        objectsRemoved: []
                ),
                new QuerySetChangesRepresentation(
                        queryId: 41,
                        queryName: 'test query2',
                        createDate: format.parse( '2019-02-07 17:59'),
                        queryType: 'patient',
                        objectsAdded: ['p1', 'p2'],
                        objectsRemoved: ['p3','p4', 'p5', 'p6']
                ),
                new QuerySetChangesRepresentation(
                        queryId: 38,
                        queryName: 'test query3',
                        createDate: format.parse( '2019-01-07 12:47'),
                        queryType: 'diagnosis',
                        objectsAdded: [],
                        objectsRemoved: ['d8','d4', 'd5', 'd6']
                ),
                new QuerySetChangesRepresentation(
                        queryId: 40,
                        queryName: 'test query4',
                        createDate: format.parse(  '2018-04-06 00:14'),
                        queryType: 'diagnosis',
                        objectsAdded: ['d1', 'd2'],
                        objectsRemoved: ['d4', 'd6']
                ),
                new QuerySetChangesRepresentation(
                        queryId: 39,
                        queryName: 'test query5',
                        createDate: format.parse( '2011-10-14 11:44'),
                        queryType: 'custom sample',
                        objectsAdded: ['x', 'y', 'z'],
                        objectsRemoved: ['a']
                ),
                new QuerySetChangesRepresentation(
                        queryId: 36,
                        queryName: 'test query6',
                        createDate: format.parse('2019-01-01 10:47'),
                        queryType: 'patient',
                        objectsAdded: ['p1'],
                        objectsRemoved: ['p3']
                )
        ]
    }
}
