/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.transaction.Transactional
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.client.TransmartRestClient
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.representation.DimensionElementsRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.ResourceAccessException

@Transactional
@CompileStatic
class QuerySetService {

    @Autowired
    TransmartRestClient transmartRestClient

    DimensionElementsRepresentation getDimensionElements(String dimensionName, Map constraint, String username) {
        try {
            def dimensionElements = transmartRestClient.getDimensionElements(dimensionName, constraint, username)
            dimensionElements.name = dimensionName
            dimensionElements
        } catch (InvalidRequestException e) {
            //TODO
        } catch (ResourceAccessException e) {
            //TODO
        }
    }
}
