/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import com.fasterxml.jackson.core.JsonProcessingException
import grails.converters.JSON
import grails.web.mime.MimeType
import nl.thehyve.gb.backend.exception.BindingException
import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.user.AuthContext
import org.grails.web.converters.exceptions.ConverterException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable

class QueryController {

    static responseFormats = ['json']

    @Autowired
    AuthContext authContext

    @Autowired
    QueryService queryService


    /**
     * GET /queries
     *
     * @returns the list of all queries saved by the user.
     */
    def index() {
        def queries = queryService.list(authContext.user)

        response.status = HttpStatus.OK.value()
        response.contentType = 'application/json'
        response.characterEncoding = 'utf-8'
        BindingHelper.writeValue(response.outputStream, [queries: queries])
    }

    /**
     * GET /queries/{id}
     *
     * @param id the id of the saved query
     * @returns the saved query with the provided id, if it exists and is owned by the user,
     *      status 404 (Not Found) or 403 (Forbidden) otherwise.
     */
    def get(@PathVariable('id') Long id) {
        def query = queryService.get(id, authContext.user)

        response.status = HttpStatus.OK.value()
        response.contentType = 'application/json'
        response.characterEncoding = 'utf-8'
        BindingHelper.writeValue(response.outputStream, query)
    }

    protected static QueryRepresentation getUserQueryFromString(String src) {
        if (src == null || src.trim().empty) {
            throw new InvalidArgumentsException('Empty user query.')
        }
        try {
            try {
                QueryRepresentation userQuery = BindingHelper.read(src, QueryRepresentation.class)
                userQuery
            } catch (JsonProcessingException e) {
                throw new InvalidArgumentsException("Cannot parse query parameter: ${e.message}", e)
            }
        } catch (ConverterException c) {
            throw new InvalidArgumentsException('Cannot parse query parameter', c)
        }
    }

    /**
     * Deserialises the request body to a user query representation object using Jackson.
     *
     * @returns the user query representation object is deserialisation was successful;
     * responds with code 400 and returns null otherwise.
     */
    protected QueryRepresentation bindUserQuery() {
        if (!request.contentType) {
            throw new InvalidRequestException('No content type provided')
        }
        MimeType mimeType = new MimeType(request.contentType)
        if (mimeType != MimeType.JSON) {
            throw new InvalidRequestException("Content type should be ${MimeType.JSON.name}; got ${mimeType}.")
        }

        try {
            def src = BindingHelper.write(request.JSON)
            return getUserQueryFromString(src)
        } catch (BindingException e) {
            def error = [
                    httpStatus: HttpStatus.BAD_REQUEST.value(),
                    message   : e.message,
                    type      : e.class.simpleName,
            ] as Map<String, Object>

            if (e.errors) {
                error.errors = e.errors
                        .collect { [propertyPath: it.propertyPath.toString(), message: it.message] }
            }

            response.status = HttpStatus.BAD_REQUEST.value()
            render error as JSON
            return null
        }
    }

    /**
     * POST /queries
     * Saves the user query in the body, which is of type {@link QueryRepresentation}.
     *
     * @returns a representation of the saved query.
     */
    def save() {
        QueryRepresentation body = bindUserQuery()
        if (body == null) {
            return
        }
        def query = queryService.create(body, authContext.user)

        response.status = HttpStatus.CREATED.value()
        response.contentType = 'application/json'
        response.characterEncoding = 'utf-8'
        BindingHelper.writeValue(response.outputStream, query)
    }

    /**
     * PUT /queries/{id}
     * Saves changes to an existing user query.
     * Changes are specified in the body, which is of type {@link QueryRepresentation}.
     *
     * @param id the identifier of the user query to update.
     * @returns status 204 and the updated query object, if it exists and is owned by the current user;
     *      404 (Not Found) or 403 (Forbidden) otherwise.
     */
    def update(@PathVariable('id') Long id) {
        QueryRepresentation body = bindUserQuery()
        if (body == null) {
            return
        }
        def query = queryService.update(id, body, authContext.user)

        response.status = HttpStatus.OK.value()
        response.contentType = 'application/json'
        response.characterEncoding = 'utf-8'
        BindingHelper.writeValue(response.outputStream, query)
    }

    /**
     * DELETE /queries/{id}
     * Deletes the user query with the provided id.
     *
     * @param id the database id of the user query.
     * @returns status 204, if the user query exists and is owned by the current user;
     *      404 (Not Found) or 403 (Forbidden) otherwise.
     */
    def delete(@PathVariable('id') Long id) {
        queryService.delete(id, authContext.user)
        response.status = HttpStatus.NO_CONTENT.value()
    }

}
