/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.exception.BindingException
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import java.text.SimpleDateFormat

@CompileStatic
class BindingHelper {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator()

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

    static ObjectMapper getObjectMapper() {
        new ObjectMapper().setDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT))
    }

    static String write(Object o) {
        objectMapper.writeValueAsString(o)
    }

    static writeValue(OutputStream out, Object value) {
        objectMapper.writeValue(out, value)
    }

    static <T> T read(String content, Class<T> type) {
        if (content == null) {
            return null
        }
        try {
            def object = (T) objectMapper.readValue(content, type)
            validate(object)
            object
        } catch (JsonProcessingException e) {
            throw new BindingException("Cannot parse parameters: ${e.message}", e)
        }
    }

    static <T> void validate(T object) {
        if (object == null) {
            return
        }
        Set<ConstraintViolation<T>> errors = validator.validate(object)
        if (errors) {
            String sErrors = errors.collect { "${it.propertyPath.toString()}: ${it.message}" }.join('; ')
            throw new BindingException("${errors.size()} error(s): ${sErrors}", errors)
        }
    }
}
