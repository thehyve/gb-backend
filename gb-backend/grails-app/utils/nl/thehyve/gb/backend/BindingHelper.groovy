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
import org.grails.web.converters.exceptions.ConverterException

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

    static <T> T getRepresentationFromInputStream(InputStream inputStream, Class<T> type) {
        try {
            def input = (T) read(inputStream, type)
            if (input == null) {
                throw new BindingException('Empty input.')
            }
            return input
        } catch (ConverterException c) {
            throw new BindingException('Cannot parse input parameter', c)
        }
    }

    static String writeAsString(Object o) {
        objectMapper.writeValueAsString(o)
    }

    static write(OutputStream out, Object value) {
        objectMapper.writeValue(out, value)
    }

    static <T> T read(InputStream content, Class<T> type) {
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

    static <T> T readFromString(String content, Class<T> type) {
        if (content == null || content.trim().empty) {
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
