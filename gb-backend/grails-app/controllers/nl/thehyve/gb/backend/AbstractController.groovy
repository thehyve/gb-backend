/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.artefact.Controller
import grails.converters.JSON
import grails.web.mime.MimeType
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.user.AuthContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

abstract class AbstractController implements Controller {

    @Autowired
    AuthContext authContext

    /**
     * Validates if the request has the proper content type
     *
     * @throws nl.thehyve.gb.backend.exception.InvalidRequestException if content type different than JSON
     */
    protected void validateRequestContentType() throws InvalidRequestException {
        if (!request.contentType) {
            throw new InvalidRequestException('No content type provided')
        }
        MimeType mimeType = new MimeType(request.contentType)
        if (mimeType != MimeType.JSON) {
            throw new InvalidRequestException("Content type should be ${MimeType.JSON.name}; got ${mimeType}.")
        }
    }

    /**
     * Creates well-formatted error response body with HttpStatus.BAD_REQUEST status
     * @param exception that occurred
     * @return code 400
     */
    protected handleBadRequestResponse(Exception e) {
        def error = [
                httpStatus: HttpStatus.BAD_REQUEST.value(),
                message   : e.message,
                type      : e.class.simpleName,
        ] as Map<String, Object>

        if (e instanceof BindingHelper  && e.errors) {
            error.errors = e.errors
                    .collect { [propertyPath: it.propertyPath.toString(), message: it.message] }
        }

        response.status = HttpStatus.BAD_REQUEST.value()
        render error as JSON
        return null
    }
}
