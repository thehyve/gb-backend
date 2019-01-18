/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.transaction.Transactional
import grails.util.Holders
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.exception.AccessDeniedException
import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import nl.thehyve.gb.backend.exception.NoSuchResourceException
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.representation.QueryUpdateRepresentation
import nl.thehyve.gb.backend.user.User

import java.util.stream.Collectors

@Transactional
@CompileStatic
class QueryService {

    private static void validateSubscriptionEnabled(Boolean subscribed, SubscriptionFrequency subscriptionFreq) {
        boolean subscriptionFreqSpecified = subscriptionFreq != null
        boolean subscriptionEnabled = Holders.config.getProperty('nl.thehyve.gb.backend.notifications.enabled', Boolean)
        if (!subscriptionEnabled && (subscribed || subscriptionFreqSpecified)) {
            throw new InvalidArgumentsException(
                    "Subscription functionality is not enabled. Saving subscription data not supported.")
        }
    }

    static QueryRepresentation toRepresentation(Query query) {
        query.with {
            new QueryRepresentation(
                    id,
                    name,
                    queryConstraint ? BindingHelper.readFromString(queryConstraint, Object) : null,
                    bookmarked,
                    subscribed,
                    subscriptionFreq,
                    queryBlob ? BindingHelper.readFromString(queryBlob, Object) : null,
                    createDate,
                    updateDate,
            )
        }
    }

    List<QueryRepresentation> list(User currentUser) {
        def result = Query.createCriteria().list {
            eq 'username', currentUser.username
            eq 'deleted', false
        } as List<Query>
        result.stream()
                .map({ Query query -> toRepresentation(query)})
                .collect(Collectors.toList())
    }

    protected Query fetch(Long id, User currentUser) {
        def query = Query.createCriteria().get {
            eq 'id', id
            eq 'username', currentUser.username
            eq 'deleted', false
        } as Query
        if (!query) {
            throw new NoSuchResourceException("Query with id ${id} not found for user.")
        }
        query
    }

    QueryRepresentation get(Long id, User currentUser) {
        toRepresentation(fetch(id, currentUser))
    }

    QueryRepresentation create(QueryRepresentation representation, User currentUser) {
        def query = new Query(username: currentUser.username)
        validateSubscriptionEnabled(representation.subscribed, representation.subscriptionFreq)
        if (representation.subscribed) {
            if (!representation.queryConstraint) {
                throw new InvalidArgumentsException("Cannot subscribe to a query with empty constraints.")
            }
            // Check query access when subscription is enabled
            // TODO TMT-686
            // checkConstraintAccess(representation.queryConstraint, currentUser)
        }

        query.with {
            name = representation.name
            queryConstraint = BindingHelper.writeAsString(representation.queryConstraint)
            bookmarked = representation.bookmarked ?: false
            subscribed = representation.subscribed ?: false
            subscriptionFreq = representation.subscriptionFreq
            queryBlob = BindingHelper.writeAsString(representation.queryBlob)
        }

        query = save(query, currentUser)

        def result = toRepresentation(query)

        if (query.subscribed) {
            // Create initial patient set when subscription is enabled
            //TODO TMT-686
            //querySetService.createSetWithInstances(result, currentUser)
        }

        result
    }

    QueryRepresentation update(Long id, QueryUpdateRepresentation representation, User currentUser) {
        validateSubscriptionEnabled(representation.subscribed, representation.subscriptionFreq)

        Query query = fetch(id, currentUser)

        if (representation.name != null) {
            query.name = representation.name
        }
        if (representation.bookmarked != null) {
            query.bookmarked = representation.bookmarked
        }
        boolean newSubscription = false
        if (representation.subscribed != null) {
            if (representation.subscribed) {
                if (!query.queryConstraint) {
                    throw new InvalidArgumentsException("Cannot subscribe to a query with empty constraints.")
                }
                // Check query access when subscription is enabled
                // TODO TMT-686
                // checkConstraintAccess(ConstraintFactory.readFromString(query.patientsQuery), currentUser)
                if (!query.subscribed) {
                    // This is a new subscription, an initial patient set needs to be generated
                    newSubscription = true
                }
            }
            query.subscribed = representation.subscribed
        }
        if (representation.subscriptionFreq != null) {
            query.subscriptionFreq = representation.subscriptionFreq
        }

        query = save(query, currentUser)

        def result = toRepresentation(query)

        if (newSubscription) {
            // Create initial patient set when subscription is being enabled
            // TODO TMT-686
            // querySetService.createSetWithInstances(result, currentUser)
        }

        result
    }

    protected Query save(Query query, User currentUser) {
        assert query instanceof Query
        if (currentUser.username != query.username) {
            throw new AccessDeniedException("Query does not belong to the current user.")
        }
        if (!query.validate()) {
            def message = query.errors.allErrors*.defaultMessage.join('.')
            throw new InvalidArgumentsException(message)
        }
        query.updateUpdateDate()
        query.save(flush: true, failOnError: true)
        query
    }

    void delete(Long id, User currentUser) {
        def query = fetch(id, currentUser)
        assert query instanceof Query
        query.deleted = true
        save(query, currentUser)
    }

}
