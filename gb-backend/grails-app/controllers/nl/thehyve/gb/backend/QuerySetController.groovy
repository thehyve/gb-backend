/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import nl.thehyve.gb.backend.representation.QuerySetChangesRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable

import static nl.thehyve.gb.backend.RequestUtils.checkForUnsupportedParams

class QuerySetController extends AbstractController {

    static responseFormats = ['json']

    @Autowired
    QuerySetService querySetService

    /**
     * Scans for changes in results of the stored queries and updates stored sets:
     * <code>/queries/sets/scan</code>
     *
     * This endpoint should be called after loading, deleting or updating the data.
     * Only available for administrators.
     *
     * @return number of sets that were updated, which is also a number of created querySets
     */
    def scan() {
        if (!authContext.user.admin) {
            response.status = 403
            respond error: "Only allowed for administrators."
            return
        }
        Integer result = querySetService.scan()
        response.status = 201
        respond([numberOfUpdatedSets: result])
    }

    /**
     * Gets a list of query result change entries by query id - history of data sets changes for specific query
     * <code>/queries/${queryId}/sets</code>
     *
     * @param maxNumberOfSets - max number of returned sets
     * @param queryId - id of the query
     * @return list of queryDiffs
     */
    def getSetChangesByQueryId(@PathVariable('queryId') Long queryId) {
        try {
            checkForUnsupportedParams(params, ['queryId', 'maxNumberOfSets'])
            def maxNumberOfSets = params.maxNumberOfSets as Integer

            List<QuerySetChangesRepresentation> querySets = querySetService.getQueryChangeHistory(queryId,
                    authContext.user, maxNumberOfSets)
            respond([querySets: querySets])
        } catch (InvalidArgumentsException e) {
            handleBadRequestResponse(e)
        } catch (Exception e) {
            response.status = 500
            respond e.message
        }
    }

}
