/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.representation

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.SubscriptionFrequency

@Canonical
@CompileStatic
class QueryUpdateRepresentation {

    String name

    Boolean bookmarked

    Boolean subscribed

    SubscriptionFrequency subscriptionFreq

}
