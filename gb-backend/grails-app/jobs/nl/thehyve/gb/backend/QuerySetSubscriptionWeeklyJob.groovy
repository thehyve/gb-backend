/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.util.Holders
import groovy.util.logging.Slf4j

/**
 * Quartz plugin job
 * See Grails Quartz Plugin documentation for more information:
 * http://grails-plugins.github.io/grails-quartz/latest/guide/
 */
@Slf4j
class QuerySetSubscriptionWeeklyJob {

    static jobEnabled =  Holders.config.nl.thehyve.gb.backend.notifications.enabled

    NotificationsMailService notificationsMailService
    /**
     * Specifies a cron expression for job trigger
     cronExpression: "s m h D M W Y"
         Second, 0-59;
         Minute, 0-59;
         Hour, 0-23;
         Day of Month, 1-31, ?;
         Month, 1-12 or JAN-DEC;
         Day of Week, 1-7 or SUN-SAT, ?;
         Year [optional]
     Either Day-of-Week or Day-of-Month must be "?".
     Can't specify both fields, nor leave both as the all values wildcard "*"
     **/
    static triggers = {
        cron name: 'weeklySubscriptionTrigger', cronExpression: "0 0 1 ? * MON"
    }

    /**
     * A short description of the job
     */
    def description = "Weekly job to check for user query data updates."

    /**
     * Runs generating emails
     */
    void execute() {
        notificationsMailService.run(SubscriptionFrequency.WEEKLY)
        log.info "Weekly subscription job executed."
    }
}
