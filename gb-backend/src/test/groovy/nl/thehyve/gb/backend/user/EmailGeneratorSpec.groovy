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
        EmailGenerator.getQuerySubscriptionUpdatesSubject(clientAppName, reportDate) == 'ABC - Cohort subscription updates - October 3 2018'
    }

    def 'body of the query subscription updates'() {
        String clientAppUrl = 'https://abc.example.com'
        String clientAppName = 'ABC'
        Map<String, List<QuerySetChangesRepresentation>> querySetChanges = [
                Diagnosis: [
                        new QuerySetChangesRepresentation(
                                queryId: 36,
                                queryName: 'test diagnosis query',
                                createDate: DATE_TIME_FORMAT.parse('2019-02-07 09:59'),
                                queryType: 'Diagnosis',
                                objectsAdded: ['d1', 'd2', 'd3'],
                                objectsRemoved: []
                        )
                ],
                Patient  : [
                        new QuerySetChangesRepresentation(
                                queryId: 35,
                                queryName: 'first saved query',
                                createDate: DATE_TIME_FORMAT.parse('2017-05-03 13:30'),
                                queryType: 'Patient',
                                objectsAdded: ['subj1', 'subj2', 'subj3'],
                                objectsRemoved: ['subj10'],

                        ),
                        new QuerySetChangesRepresentation(
                                queryId: 50,
                                queryName: 'test query',
                                createDate: DATE_TIME_FORMAT.parse('2017-08-16 8:45'),
                                queryType: 'Diagnosis',
                                objectsAdded: ['subj100', 'subj200', 'subj300', 'subj400', 'subj500', 'subj600'],
                                objectsRemoved: ['subj101', 'subj201'],
                        ),
                ]
        ]
        Date reportDate = DATE_TIME_FORMAT.parse('2018-10-03 15:25')
        def expectedContent = 'Hello,<br /><br />' +
                'You have subscribed to be notified to data updates for one or more cohorts that you have saved in the "ABC" application.' +
                '<br />In this email you will find an overview of all data updates up until October 3 2018 15:25:' +
                '<p />Cohort changes (type <b>Diagnosis</b>):' +
                '<br /><table cellpadding="10"><tr>' +
                '<th align="left">Your Cohort Name</th><th align="left">Your Cohort ID</th><th align="left">Number of added instances</th><th align="left">Number of removed instances</th><th align="left">Date of change</th></tr>' +
                '<tr><td>test diagnosis query</td><td>36</td><td>3</td><td>0</td><td>February 7 2019 9:59</td></tr>' +
                '</table>' +
                '<br />Cohort changes (type <b>Patient</b>):' +
                '<br /><table cellpadding="10">' +
                '<tr><th align="left">Your Cohort Name</th><th align="left">Your Cohort ID</th><th align="left">Number of added instances</th><th align="left">Number of removed instances</th><th align="left">Date of change</th></tr>' +
                '<tr><td>first saved query</td><td>35</td><td>3</td><td>1</td><td>May 3 2017 13:30</td></tr>' +
                '<tr><td>test query</td><td>50</td><td>6</td><td>2</td><td>August 16 2017 8:45</td></tr>' +
                '</table>' +
                '<p />You can login to <a href="https://abc.example.com">ABC</a> to reload your cohorts and review the new data available.' +
                '<br />The list of cohorts is in the left panel. Click on the cohort and select \'Show subscription records\' to see the identifiers of the records that were added or removed.' +
                '<br />Regards,<br /><br />ABC'
        when:
        def realContent = EmailGenerator.getQuerySubscriptionUpdatesBody(querySetChanges, clientAppName, clientAppUrl, reportDate)
        then:
        realContent == expectedContent
    }
}
