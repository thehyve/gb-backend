/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.representation

import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * A patient (or subject) is a individual for which there are clinical
 * observations or other data.
 */
@CompileStatic
@Canonical
class PatientRepresentation {

    static final String SUBJ_ID_SOURCE = 'SUBJ_ID'

    /**
     * A unique identifier for the patient. Cannot be null.
     */
    Long id

    /**
     * A map of subject identifiers. The key is the source of the identifier,
     * the value is the subject identifier.
     */
    Map<String, String> subjectIds

    /**
     * Subject identifier associated with a patient.
     */
    String getPatientSubId() {
        subjectIds[SUBJ_ID_SOURCE]
    }

}
