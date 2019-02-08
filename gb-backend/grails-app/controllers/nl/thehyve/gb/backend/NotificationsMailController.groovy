/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.mail.MailAuthenticationException
import org.springframework.web.bind.annotation.RequestParam

class NotificationsMailController extends AbstractController {

    @Autowired
    NotificationsMailService notificationsMailService

    @Value('${nl.thehyve.gb.backend.notifications.enabled}')
    boolean notificationsEnabled

    /**
     * GET /notifications/notify?frequency=DAILY|WEEKLY
     *
     *
     * Sends emails to users who subscribed for a user query updates.
     *
     * @param frequency specifies whether the mail should be send to users
     *        who subscribed for DAILY or WEEKLY subscription.
     * @return {@link org.springframework.http.HttpStatus#OK};
     *      or {@link org.springframework.http.HttpStatus#FORBIDDEN} when the user does not have an ADMIN role,
     *      see request matcher configuration of the current application.
     */
    def notificationsNotify(@RequestParam('frequency')String frequency) {

        if (!notificationsEnabled) {
            response.status = 404
            respond error: "This endpoint is not enabled."
        } else if (!authContext.user.admin) {
            response.status = 403
            respond error: "Only allowed for administrators."
        } else {
            SubscriptionFrequency subscriptionFrequency = frequency?.trim() ? SubscriptionFrequency.forName(frequency) : null
            if (!subscriptionFrequency) {
                handleBadRequestResponse(new InvalidArgumentsException("Invalid frequency parameter: $frequency"))
            }
            try {
                notificationsMailService.run(subscriptionFrequency)
                response.status = HttpStatus.OK.value()
            } catch (MailAuthenticationException e) {
                response.status = 401
                respond error: e.message
            }
        }
    }

}
