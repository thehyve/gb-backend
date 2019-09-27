/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.plugins.mail.MailService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nl.thehyve.gb.backend.representation.QuerySetChangesRepresentation
import nl.thehyve.gb.backend.user.User
import nl.thehyve.gb.backend.user.UserService
import org.springframework.beans.factory.annotation.Autowired

/**
 * Generates and sends a daily or weekly subscription email for each user
 * that subscribed for a query data result updates.
 *
 * The DSL for sending emails is provided by the Grails mail plugin.
 * See Grails Mail Plugin documentation for more information:
 * http://gpc.github.io/grails-mail/
 *
 */
@Slf4j
@CompileStatic
class NotificationsMailService {

    @Autowired
    MailService mailService

    @Autowired
    UserService userService

    @Autowired
    QuerySetService querySetService

    String clientApplicationName
    String clientApplicationUrl
    Integer maxNumberOfSets

    /**
     * Creates and sends a daily or weekly email for each subscribed user having an email specified.
     *
     * The email is sent only when there are changes for one or more of the results of queries the user subscribed for.
     * A query result is changed when there is an object ID in the result that wasn't there the last time it was run,
     * or when an object ID isn't there which was there the last time it was run.
     *
     */
    def run(SubscriptionFrequency frequency) {
        assert querySetService
        assert maxNumberOfSets

        List<User> users = userService.getUsersWithEmailSpecified()
        if (users == null) {
            return
        }
        Date reportDate = new Date()
        for (user in users) {
            Map<String, List<QuerySetChangesRepresentation>> queryTypeToQuerySetsChanges =
                    getQueryTypeToQuerySetChangesRepresentations(frequency, user.username)

            if (queryTypeToQuerySetsChanges.size() > 0) {
                String emailSubject = EmailGenerator.getQuerySubscriptionUpdatesSubject(clientApplicationName, reportDate)
                String emailBodyHtml = EmailGenerator.getQuerySubscriptionUpdatesBody(queryTypeToQuerySetsChanges,
                        clientApplicationName,
                        clientApplicationUrl,
                        reportDate)
                sendEmail(user.email, emailSubject, emailBodyHtml)
            }
        }
    }

    /**
     * Fetches a list of sets for all queries user subscribed to with specific frequency
     * for which there were instances added or removed comparing to a previous query set
     * and groups it by query type (subject dimension).
     *
     * @param frequency
     * @param username
     * @return A map of query subject dimension to list of sets with changes
     */
    private Map<String, List<QuerySetChangesRepresentation>> getQueryTypeToQuerySetChangesRepresentations(
            SubscriptionFrequency frequency,
            String username) {
        List<QuerySetChangesRepresentation> querySetsChanges =
                querySetService.getQueriesChangeHistoriesByUsernameAndFrequency(username,  frequency, maxNumberOfSets)
        return querySetsChanges.sort { it.queryId }.groupBy { it.queryType }
    }

    /**
     * Sends an email to a specific recipient from an email account that is specified in the application config file,
     * using Grails Mail Plugin
     *
     * @param address - a receiver address
     * @param title - an email subject
     * @param emailBodyHtml - html to send as an email body
     */
    private void sendEmail(String address, String title, String emailBodyHtml) {

        mailService.sendMail {
            to address
            html emailBodyHtml
            subject(title)
        }
    }
}
