/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import nl.thehyve.gb.backend.user.AuthContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable

class QuerySetController {

    static responseFormats = ['json']

    @Autowired
    AuthContext authContext

    @Autowired
    QuerySetService querySetService

    // TODO remove this endpoint - only for testing
    def dimensionElements(@PathVariable('dimensionName') String dimensionName) {
        def constraints = request.JSON.constraint as Map<String, Object>
        def username = request.JSON.username.toString()
        def val = querySetService.getDimensionElements(dimensionName, constraints, username)

        response.status = HttpStatus.OK.value()
        response.contentType = 'application/json'
        response.characterEncoding = 'utf-8'
        BindingHelper.write(response.outputStream, val)
    }

}
