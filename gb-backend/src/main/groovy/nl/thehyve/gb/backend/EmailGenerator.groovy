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
                'Cohort subscription updates',
                DATE_FORMAT.format(reportDate)
        ].join(SUBJECT_PARTS_SEPARATOR)
    }

    /**
     * Generates an email for specific user with data updates for queries the user is subscribed for, grouped by query type.
     *
     * The email contains a list of each query (cohort), grouped by query type, with changed results, containing:
     * - a name of the query,
     * - number of added and removed objects that the query relates to
     * - over what period the change was
     *
     * @return The body of the email
     *
     */
    static String getQuerySubscriptionUpdatesBody(Map<String, List<QuerySetChangesRepresentation>> queryTypeToQuerySetsChanges,
            String clientAppName,
            String clientAppUrl,
            Date reportDate) {
        String clientAppLink = clientAppUrl && !clientAppUrl.empty ? "<a href=\"${clientAppUrl}\">${clientAppName}</a>" : clientAppName
        String header = [
                'Hello,',
                '',
                "You have subscribed to be notified to data updates for one or more cohorts that you have saved in the \"${clientAppName}\" application.",
                "In this email you will find an overview of all data updates up until ${DATE_TIME_FORMAT.format(reportDate)}:",
        ].join(BR)
        String updateInfo = htmlTablesGroupedByType(queryTypeToQuerySetsChanges)
        String footer = [
                "You can login to ${clientAppLink} to reload your cohorts and review the new data available.",
                "The list of cohorts is in the left panel. Click on the cohort and select 'Show subscription records' to see the identifiers of the records that were added or removed.",
                '',
                'Regards,',
                '',
                clientAppName,
        ].join(BR)
        return header + P + updateInfo + P + footer
    }

    protected static String htmlTablesGroupedByType(Map<String, List<QuerySetChangesRepresentation>> queryTypeToQuerySetsChanges) {
        queryTypeToQuerySetsChanges.collect { type, querySetsChanges ->
            String table = updatesHtmlTablePerType(querySetsChanges)
            [
                    "Cohort changes (type <b>${type}</b>):",
                    table
            ].join(BR)
        }.join(BR)
    }

    protected static String updatesHtmlTablePerType(List<QuerySetChangesRepresentation> querySetsChanges) {
        String queryNameHeader = 'Your Cohort Name'
        String queryIdHeader = 'Your Cohort ID'
        String addedSubjectHeader = 'Number of added instances'
        String removedSubjectHeader = 'Number of removed instances'
        String dateOfChangeHeader = 'Date of change'

        List<String> tableRows = querySetsChanges.collect { QuerySetChangesRepresentation change ->
            "<tr><td>${change.queryName}</td><td>${change.queryId}</td><td>${change.objectsAdded.size()}</td><td>${change.objectsRemoved.size()}</td><td>${DATE_TIME_FORMAT.format(change.createDate)}</td></tr>".toString()
        }

        '<table cellpadding="10">' +
                '<tr>' + th(queryNameHeader) + th(queryIdHeader) + th(addedSubjectHeader) + th(removedSubjectHeader) + th(dateOfChangeHeader) + '</tr>' +
                tableRows.join('') +
                '</table>'
    }

    protected static String th(String content) {
        '<th align="left">' + content + '</th>'
    }
}
