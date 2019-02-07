/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.representation

import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * Representation of changes made in the query_set - added and removed objects,
 * in comparison to the previous query_set related to the same query
 */
@Canonical
@CompileStatic
class QuerySetChangesRepresentation {

    Long id

    Long setSize

    Date createDate

    String queryName

    Long queryId

    List<String> objectsAdded

    List<String> objectsRemoved
}
