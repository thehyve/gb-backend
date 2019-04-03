package nl.thehyve.gb.backend.representation

import com.fasterxml.jackson.annotation.JsonValue
import groovy.transform.CompileStatic

/**
 * Type of the dimension. The dimensions can represent either subjects (e.g., patients, locations, samples),
 * i.e., entities that the observations are about, or observation attributes (e.g., value, start date).
 *
 */
@CompileStatic
enum DimensionType {

    /**
     * Dimensions of this type represent a subject dimension, i.e., entities that observations are about.
     */
    SUBJECT,

    /**
     * Dimensions of this type represent observation attributes.
     */
    ATTRIBUTE

    @JsonValue
    String toJson() {
        name().toLowerCase()
    }

}
