/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.representation.QuerySetChangesRepresentation

import java.text.SimpleDateFormat

@CompileStatic
class EmailGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat('MMMM d Y')
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat('MMMM d Y H:mm')
    public static final String SUBJECT_PARTS_SEPARATOR = ' - '
    private static final String BR = '<br />'
    private static final String P = '<p />'

    static String getQuerySubscriptionUpdatesSubject(String clientAppName, Date reportDate) {
        [
                clientAppName,
                'Query subscription updates',
                DATE_FORMAT.format(reportDate)
        ].join(SUBJECT_PARTS_SEPARATOR)
    }

    /**
     * Generates an email for specific user with data updates for a query the user is subscribed for.
     *
     * The email contains a list of each query with changed results with:
     * - a name of the query,
     * - list of added and removed ids of objects that the query relates to
     * - over what period the change was
     *
     * @return The body of the email
     *
     */
    static String getQuerySubscriptionUpdatesBody(List<QuerySetChangesRepresentation> querySetsChanges, String clientAppName, Date reportDate) {
        String header = [
                'Hello,',
                '',
                "You have subscribed to be notified to data updates for one or more queries that you have saved in the \"${clientAppName}\" application.",
                "In this email you will find an overview of all data updates up until ${DATE_TIME_FORMAT.format(reportDate)}:",
        ].join(BR)
        String table = updatesHtmlTable(querySetsChanges)
        String footer = [
                "You can login to ${clientAppName} to reload your queries and review the new data available.",
                'Regards,',
                '',
                clientAppName,
        ].join(BR)
        return header + P + table + P + footer
    }

    protected static String updatesHtmlTable(List<QuerySetChangesRepresentation> querySetsChanges) {
        String queryIdHeader = 'Your Query ID'
        String queryNameHeader = 'Your Query Name'
        String addedSubjectHeader = 'Added instances with ids'
        String removedSubjectHeader = 'Removed instances with ids'
        String dateOfChangeHeader = 'Date of change'
        List<String> tableRows = querySetsChanges.collect { QuerySetChangesRepresentation change ->
            "<tr><td>${change.queryId}</td><td>${change.queryName}</td><td>${change.objectsAdded.join(', ')}</td><td>${change.objectsRemoved.join(', ')}</td><td>${DATE_TIME_FORMAT.format(change.createDate)}</td></tr>".toString()
        }
        '<table cellpadding="10">' +
                '<tr>' + th(queryIdHeader) + th(queryNameHeader) + th(addedSubjectHeader) + th(removedSubjectHeader) + th(dateOfChangeHeader) + '</tr>' +
                tableRows.join('') +
                '</table>'
    }

    protected static String th(String content) {
        '<th align="left">' + content + '</th>'
    }
}
