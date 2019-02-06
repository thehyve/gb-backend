/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.client.TransmartRestClient
import nl.thehyve.gb.backend.exception.UnexpectedResultException
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import nl.thehyve.gb.backend.representation.PatientRepresentation
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.representation.QuerySetChangesRepresentation
import nl.thehyve.gb.backend.user.User
import nl.thehyve.gb.backend.user.UserService
import org.hibernate.Criteria
import org.hibernate.SessionFactory
import org.hibernate.criterion.DetachedCriteria
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation

import java.util.stream.Collectors

import static nl.thehyve.gb.backend.QueryService.toRepresentation

@Transactional
@CompileStatic
class QuerySetService {

    @Autowired
    UserService userService

    @Autowired
    QueryService queryService

    @Autowired
    TransmartRestClient transmartRestClient

    SessionFactory sessionFactory

    static final String PATIENT_DIMENSION_NAME = 'patient'

    Integer scan() {
        log.info 'Scanning for subscribed user queries updates ...'
        int numberOfResults = 0
        List<Query> queries = queryService.getQueriesSubscribedAndNotDeleted()
        log.info "${queries.size()} subscribed user queries are found."

        for (Query query : queries) {
            try {
                if (createQuerySetWithQueryDiffs(query)) {
                    numberOfResults++
                }
            } catch (Exception e) {
                log.error "Could not compute updates for user query ${query.id}", e
            }
        }
        log.info "${numberOfResults} subscribed user queries got updated."
        return numberOfResults
    }

    List<QuerySetChangesRepresentation> getQueryChangeHistory(Long queryId, User currentUser, Integer maxNumberOfSets) {
        def querySets = getQuerySets(queryId, currentUser, maxNumberOfSets)
        querySets.stream()
                .map({ QuerySet userQuerySet -> mapToSetChangesRepresentation(userQuerySet) })
                .collect(Collectors.toList())
    }

    List<QuerySetChangesRepresentation> getQueryChangeHistoryByUsernameAndFrequency(SubscriptionFrequency frequency,
                                                                                    String username,
                                                                                    Integer maxNumberOfSets) {
        def querySets = getQuerySetsWithDiffsByUsernameAndFrequency(frequency, username, maxNumberOfSets)
        querySets.stream()
                .map({ QuerySet querySet -> mapToSetChangesRepresentation(querySet) })
                .collect(Collectors.toList())
    }

    void createQuerySetWithQueryInstances(QueryRepresentation queryRepresentation) {
        log.info "Create set of type ${queryRepresentation.type} for query ${queryRepresentation.id}"
        List<String> setInstances = getSetInstancesForQuery(queryRepresentation)
        Query query = queryService.getQueryById(queryRepresentation.id)
        QuerySet querySet = new QuerySet(
                query: query,
                setSize: setInstances.size(),
        )
        querySet.save(flush: true, failOnError: true)

        List<QuerySetInstance> instances = []
        if (setInstances.size() > 0) {
            for (instanceId in setInstances) {
                instances.add(new QuerySetInstance(
                        querySet: querySet,
                        objectId: instanceId
                ))
            }
            instances*.save(flush: true, failOnError: true)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected boolean createQuerySetWithQueryDiffs(Query query) {
        Long queryId = query.id
        List<QuerySetInstance> previousQuerySetInstances = getQueryInstancesForLatestQuerySet(queryId)
        List<String> previousInstanceIds = previousQuerySetInstances.collect { it.objectId }

        List<String> newSetInstanceIds = getSetInstancesWithImpersonation(toRepresentation(query))
        List<String> addedSubIds = newSetInstanceIds - previousInstanceIds
        List<String> removedSubIds = previousInstanceIds - newSetInstanceIds

        if (addedSubIds.size() > 0 || removedSubIds.size() > 0) {
            QuerySet querySet = new QuerySet()
            querySet.query = query
            querySet.setSize = (Long) newSetInstanceIds.size()

            List<QuerySetInstance> querySetInstances = []
            querySetInstances.addAll(newSetInstanceIds.collect {
                QuerySetInstance setInstance = new QuerySetInstance()
                setInstance.querySet = querySet
                setInstance.objectId = it
                setInstance
            })

            List<QuerySetDiff> querySetDiffs = []
            querySetDiffs.addAll(addedSubIds.collect {
                QuerySetDiff setDiff = new QuerySetDiff()
                setDiff.querySet = querySet
                setDiff.objectId = it
                setDiff.changeFlag = ChangeFlag.ADDED
                setDiff
            })
            querySetDiffs.addAll(removedSubIds.collect {
                QuerySetDiff setDiff = new QuerySetDiff()
                setDiff.querySet = querySet
                setDiff.objectId = it
                setDiff.changeFlag = ChangeFlag.REMOVED
                setDiff
            })

            querySet.save(flush: true, failOnError: true)
            querySetInstances*.save(flush: true, failOnError: true)
            querySetDiffs*.save(flush: true, failOnError: true)

            return true
        } else {
            return false
        }
    }

    private DimensionElementsRepresentation getDimensionElements(String dimensionName, Map constraint) {
        def dimensionElements = transmartRestClient.getDimensionElements(dimensionName, constraint)
        dimensionElements.name = dimensionName
        dimensionElements
    }

    private DimensionElementsRepresentation getDimensionElementsForUser(String dimensionName, Map constraint,
                                                                String impersonatedUserName) {
        def dimensionElements = transmartRestClient.getDimensionElements(dimensionName, constraint, impersonatedUserName)
        dimensionElements.name = dimensionName
        dimensionElements
    }

    private List<String> getSetInstancesForQuery(QueryRepresentation query) {
        def newDimensionElements =
                getDimensionElements(query.type, query.queryConstraint as Map)?.elements
        listSetInstanceIds(query.type, newDimensionElements)
    }

    private List<String> getSetInstancesWithImpersonation(QueryRepresentation query) {
        def newDimensionElements =
                getDimensionElementsForUser(query.type, query.queryConstraint as Map, query.username)?.elements
        listSetInstanceIds(query.type, newDimensionElements)
    }

    private List<QuerySet> getQuerySets(Long queryId, User currentUser, Integer maxNumberOfSets) {
        // Check access to the query
        QueryRepresentation query = queryService.getQueryRepresentationByIdAndUsername(queryId, currentUser)

        Criteria criteria = sessionFactory.currentSession.createCriteria(QuerySet, 'querySet')
                .createAlias('querySet.query', 'query', JoinType.INNER_JOIN)
                .add(Restrictions.eq('query.id', query.id))
                .add(Restrictions.eq('query.deleted', false))
                .addOrder(Order.desc('querySet.createDate'))
                .setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
        if (maxNumberOfSets) {
            criteria.setMaxResults(maxNumberOfSets)
        }
        def result = criteria.list() as List<Map>
        result.stream()
                .map({ Map data -> (QuerySet)data.querySet })
                .collect(Collectors.toList())
    }

    private List<QuerySet> getQuerySetsWithDiffsByUsernameAndFrequency(SubscriptionFrequency frequency,
                                                                           String username, Integer maxNumberOfSets) {
        Calendar calendar = Calendar.getInstance()
        if (frequency == SubscriptionFrequency.DAILY) {
            calendar.add(Calendar.DATE, -1)
        } else {
            calendar.add(Calendar.DATE, -7)
        }
        def session = sessionFactory.currentSession
        Criteria criteria = session.createCriteria(QuerySet, "querySet")
                .createAlias("querySet.query", "query", JoinType.INNER_JOIN)
                .add(Restrictions.eq('query.username', username))
                .add(Restrictions.eq('query.deleted', false))
                .add(Restrictions.eq('query.subscribed', true))
                .add(Restrictions.eq('query.subscriptionFreq', frequency))
                .add(Restrictions.isNotEmpty('querySet.querySetDiffs'))
                .add(Restrictions.ge("querySet.createDate", calendar.getTime()))
                .addOrder(Order.desc('querySet.createDate'))
                .setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
        if (maxNumberOfSets) {
            criteria.setMaxResults(maxNumberOfSets)
        }
        def result = criteria.list() as List<Map>
        result.stream()
                .map({ Map data -> (QuerySet)data.querySet })
                .collect(Collectors.toList())
    }

    private List<QuerySetInstance> getQueryInstancesForLatestQuerySet(Long id) throws UnexpectedResultException {
        DetachedCriteria criteria = DetachedCriteria.forClass(QuerySet)
                .add(Restrictions.eq('query.id', id))
                .addOrder(Order.desc('createDate'))
        def recent = criteria.getExecutableCriteria(sessionFactory.currentSession)
                .setMaxResults(1)
                .list() as List<QuerySet>
        if (recent.size() != 1) {
            throw new UnexpectedResultException("One query set expected, got ${recent.size()}")
        }

        sessionFactory.currentSession.createCriteria(QuerySetInstance)
                .add(Restrictions.eq("querySet", recent[0]))
                .list() as List<QuerySetInstance>
    }

    private static List<String> listSetInstanceIds(String dimensionName, List<Map<String, Object>> newDimensionElements) {
        if (dimensionName == PATIENT_DIMENSION_NAME) {
            // it is a special case, where instead of using patient dimension id, a subject identifier is used
            listPatientSetInstanceIds(newDimensionElements)
        } else {
            listSetInstanceIds(newDimensionElements)
        }
    }

    private static List<String> listSetInstanceIds(List<Map<String, Object>> newDimensionElements) {
        newDimensionElements.collect { Map<String, Object> element ->
                    element['id'].toString()
        }
    }

    private static List<String> listPatientSetInstanceIds(List<Map<String, Object>> newDimensionElements) {
        mapToPatientDimensionRepresentation(newDimensionElements).collect { patientRepresentation ->
            patientRepresentation.patientSubId
        }
    }

    private static List<PatientRepresentation> mapToPatientDimensionRepresentation(
            List<Map<String, Object>> dimensionElements) {
        dimensionElements.collect { Map<String, Object> element ->
            new PatientRepresentation(
                    (Long) element['id'],
                    (Map) element['subjectIds']
            )
        }
    }

    private static QuerySetChangesRepresentation mapToSetChangesRepresentation(QuerySet set){
        List<String> objectsAdded = []
        List<String> objectsRemoved = []
        if (set.querySetDiffs) {
            for (diff in set.querySetDiffs) {
                if (diff.changeFlag == ChangeFlag.ADDED) {
                    objectsAdded.add(diff.objectId)
                } else {
                    objectsRemoved.add(diff.objectId)
                }
            }
        }
        new QuerySetChangesRepresentation(
                set.id,
                set.setSize,
                set.createDate,
                set.query.name,
                set.query.id,
                objectsAdded,
                objectsRemoved
        )
    }

}
