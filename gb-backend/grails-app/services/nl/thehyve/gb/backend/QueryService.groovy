/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.exception.AccessDeniedException
import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import nl.thehyve.gb.backend.exception.NoSuchResourceException
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.representation.QueryUpdateRepresentation
import nl.thehyve.gb.backend.user.User
import org.springframework.beans.factory.annotation.Autowired

import java.util.stream.Collectors

@Transactional
@CompileStatic
class QueryService {

    @Autowired
    QuerySetService querySetService

    static QueryRepresentation toRepresentation(Query query) {
        query.with {
            new QueryRepresentation(
                    id,
                    type,
                    username,
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

    Query getQueryById(Long id) throws NoSuchResourceException {
        def query = Query.createCriteria().get {
            idEq id
        } as Query
        if (!query) {
            throw new NoSuchResourceException("Query with id ${id} not found.")
        }
        query
    }

    QueryRepresentation getQueryRepresentationByIdAndUsername(Long id, User currentUser) {
        toRepresentation(getQueryByIdAndUsername(id, currentUser))
    }

    Query getQueryByIdAndUsername(Long id, User currentUser) throws NoSuchResourceException {
        def query = Query.createCriteria().get {
            eq 'id', id
            eq 'username', currentUser.username
            eq 'deleted', false
        } as Query
        if (!query) {
            throw new NoSuchResourceException("Query with id ${id} not found for user ${currentUser.username}.")
        }
        query
    }

    List<Query> getQueriesSubscribedAndNotDeleted() {
        Query.createCriteria().list {
            eq 'deleted', false
            eq 'subscribed', true
        } as List<Query>
    }

    QueryRepresentation create(QueryRepresentation representation, User currentUser) throws InvalidArgumentsException {
        def query = new Query(username: currentUser.username)
        validateSubscriptionEnabled(representation.subscribed, representation.subscriptionFreq)
        if (representation.subscribed) {
            if (!representation.queryConstraint) {
                throw new InvalidArgumentsException("Cannot subscribe to a query with empty constraints.")
            }
        }
        query.with {
            name = representation.name
            type = representation.type // TODO TMT-741 - validate query type, currently it can be any string
            queryConstraint = BindingHelper.writeAsString(representation.queryConstraint)
            bookmarked = representation.bookmarked ?: false
            subscribed = representation.subscribed ?: false
            subscriptionFreq = representation.subscriptionFreq
            queryBlob = BindingHelper.writeAsString(representation.queryBlob)
        }
        query = save(query, currentUser)

        def result = toRepresentation(query)

        if (query.subscribed) {
            querySetService.createQuerySetWithQueryInstances(result)
        }

        result
    }

    QueryRepresentation update(Long id, QueryUpdateRepresentation representation, User currentUser)
            throws InvalidArgumentsException{
        validateSubscriptionEnabled(representation.subscribed, representation.subscriptionFreq)

        Query query = getQueryByIdAndUsername(id, currentUser)

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
                if (!query.subscribed) {
                    // This is a new subscription, an initial set needs to be generated
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
            // Create initial set when subscription is being enabled
            querySetService.createQuerySetWithQueryInstances(result)
        }

        result
    }

    void delete(Long id, User currentUser) {
        def query = getQueryByIdAndUsername(id, currentUser)
        assert query instanceof Query
        query.deleted = true
        save(query, currentUser)
    }

    protected static Query save(Query query, User currentUser) throws InvalidArgumentsException {
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

    private static void validateSubscriptionEnabled(Boolean subscribed, SubscriptionFrequency subscriptionFreq)
            throws InvalidArgumentsException {
        boolean subscriptionFreqSpecified = subscriptionFreq != null
        boolean subscriptionEnabled = Holders.config.getProperty('nl.thehyve.gb.backend.subscription.enabled', Boolean)
        if (!subscriptionEnabled && (subscribed || subscriptionFreqSpecified)) {
            throw new InvalidArgumentsException(
                    "Subscription functionality is not enabled. Saving subscription data not supported.")
        }
    }

}
