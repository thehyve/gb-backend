/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.representation

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.Canonical
import groovy.transform.CompileStatic

@Canonical
@CompileStatic
class DimensionPropertiesRepresentation {

    /**
     * Dimension name
     */
    String name

    /**
     * If true, this dimension will be inlined in the cell. Only present if true.
     */
    Boolean inline

    DimensionType dimensionType

    Integer sortIndex

    String valueType

    /**
     * Fields is required only if the type is Object
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<Field> fields

}
