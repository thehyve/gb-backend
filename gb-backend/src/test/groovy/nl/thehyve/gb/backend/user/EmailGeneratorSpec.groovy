/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.user

import nl.thehyve.gb.backend.EmailGenerator

import nl.thehyve.gb.backend.representation.QuerySetChangesRepresentation
import spock.lang.Specification

import java.text.SimpleDateFormat

class EmailGeneratorSpec extends Specification {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat('yyyy-MM-dd')
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat('yyyy-MM-dd H:mm')

    def 'subject of the query subscription updates'() {
        String clientAppName = 'ABC'
        Date reportDate = DATE_FORMAT.parse('2018-10-03')
        expect:
        EmailGenerator.getQuerySubscriptionUpdatesSubject(clientAppName, reportDate) == 'ABC - Query subscription updates - October 3 2018'
    }

    def 'body of the query subscription updates'() {
        String clientAppName = 'ABC'
        def querySetChanges = [
                new QuerySetChangesRepresentation(
                        queryId: 35,
                        queryName: 'first saved query',
                        createDate: DATE_TIME_FORMAT.parse('2017-05-03 13:30'),
                        objectsAdded: ['subj1', 'subj2', 'subj3'],
                        objectsRemoved: ['subj10'],

                ),
                new QuerySetChangesRepresentation(
                        queryId: 50,
                        queryName: 'test query',
                        createDate: DATE_TIME_FORMAT.parse('2017-08-16 8:45'),
                        objectsAdded: ['subj100', 'subj200', 'subj300', 'subj400', 'subj500', 'subj600'],
                        objectsRemoved: ['subj101', 'subj201'],

                ),
        ]
        Date reportDate = DATE_TIME_FORMAT.parse('2018-10-03 15:25')
        def expectedContent = 'Hello,<br /><br />' +
                'You have subscribed to be notified to data updates for one or more queries that you have saved in the "ABC" application.<br />' +
                'In this email you will find an overview of all data updates up until October 3 2018 15:25:' +
                '<p />' +
                '<table cellpadding="10">' +
                '<tr><th align="left">Your Query ID</th><th align="left">Your Query Name</th><th align="left">Added instances with ids</th><th align="left">Removed instances with ids</th><th align="left">Date of change</th></tr>' +
                '<tr><td>35</td><td>first saved query</td><td>subj1, subj2, subj3</td><td>subj10</td><td>May 3 2017 13:30</td></tr>' +
                '<tr><td>50</td><td>test query</td><td>subj100, subj200, subj300, subj400, subj500, subj600</td><td>subj101, subj201</td><td>August 16 2017 8:45</td></tr>' +
                '</table>' +
                '<p />' +
                'You can login to ABC to reload your queries and review the new data available.<br />' +
                'Regards,<br /><br />' +
                'ABC'
        when:
        def realContent = EmailGenerator.getQuerySubscriptionUpdatesBody(querySetChanges, clientAppName, reportDate)
        then:
        realContent == expectedContent
    }
}
