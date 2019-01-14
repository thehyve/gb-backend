/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend

import com.fasterxml.jackson.annotation.JsonCreator
import groovy.transform.CompileStatic

/**
 * The frequency of emails for user query subscription
 */
@CompileStatic
enum SubscriptionFrequency {

    DAILY,
    WEEKLY

    private static final Map<String, SubscriptionFrequency> mapping = new HashMap<>()
    static {
        for (SubscriptionFrequency type: values()) {
            mapping.put(type.name().toLowerCase(), type)
        }
    }

    @JsonCreator
    static SubscriptionFrequency forName(String name) {
        name = name.toLowerCase()
        if (mapping.containsKey(name)) {
            return mapping[name]
        } else {
            return null
        }
    }

}
