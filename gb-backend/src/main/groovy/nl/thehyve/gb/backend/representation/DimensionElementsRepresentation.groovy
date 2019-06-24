/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.representation

import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class DimensionElementsRepresentation {

    /**
     * Dimension name
     */
    String name

    /**
     * List of dimension elements with properties specific to a given dimension.
     */
    List elements

}
