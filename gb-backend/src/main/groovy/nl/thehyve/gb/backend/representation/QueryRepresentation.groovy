/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.representation

import com.fasterxml.jackson.annotation.JsonFormat
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.SubscriptionFrequency

import javax.validation.constraints.Size
import javax.validation.constraints.NotNull

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING

@Canonical
@CompileStatic
class QueryRepresentation {

    Long id

    @Size(min = 1)
    String type

    @Size(min = 1)
    String username

    @Size(min = 1)
    String name

    @NotNull
    Object queryConstraint

    Boolean bookmarked

    Boolean subscribed

    SubscriptionFrequency subscriptionFreq

    Object queryBlob

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Date createDate

    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    Date updateDate

}
