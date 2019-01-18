/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import grails.converters.JSON
import grails.web.mime.MimeType
import nl.thehyve.gb.backend.exception.AccessDeniedException
import nl.thehyve.gb.backend.exception.BindingException
import nl.thehyve.gb.backend.exception.InvalidArgumentsException
import nl.thehyve.gb.backend.exception.InvalidRequestException
import nl.thehyve.gb.backend.exception.NoSuchResourceException
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.representation.QueryUpdateRepresentation
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
        BindingHelper.write(response.outputStream, [queries: queries])
    }

    /**
     * GET /queries/{id}
     *
     * @param id the id of the saved query
     * @returns the saved query with the provided id, if it exists and is owned by the user,
     *      status 404 (Not Found) otherwise.
     */
    def get(@PathVariable('id') Long id) {
        try {
            def query = queryService.get(id, authContext.user)

            response.status = HttpStatus.OK.value()
            response.contentType = 'application/json'
            response.characterEncoding = 'utf-8'
            BindingHelper.write(response.outputStream, query)
        } catch (AccessDeniedException | NoSuchResourceException e){
            response.status = 404
            respond error: "Query with id ${id} not found for user."
            return
        }
    }

    /**
     * POST /queries
     * Saves the user query in the body, which is of type {@link QueryRepresentation}.
     *
     * @returns a representation of the saved query or 400 (Bad request) otherwise.
     */
    def save() {
        QueryRepresentation body = bindUserQuery()
        if (body == null) {
            return
        }
        try {
            def query = queryService.create(body, authContext.user)
            response.status = HttpStatus.CREATED.value()
            response.contentType = 'application/json'
            response.characterEncoding = 'utf-8'
            BindingHelper.write(response.outputStream, query)
        } catch (InvalidArgumentsException e) {
            handleBadRequestResponse(e)
        }
    }

    /**
     * PUT /queries/{id}
     * Saves changes to an existing user query.
     * Changes are specified in the body, which is of type {@link QueryRepresentation}.
     *
     * @param id the identifier of the user query to update.
     * @returns status 204 and the updated query object, if it exists and is owned by the current user;
     *      404 (Not Found) or 400 (Bad request) otherwise.
     */
    def update(@PathVariable('id') Long id) {
        QueryUpdateRepresentation body = bindUserQueryUpdate()
        if (body == null) {
            return
        }
        try {
            def query = queryService.update(id, body, authContext.user)
            response.status = HttpStatus.OK.value()
            response.contentType = 'application/json'
            response.characterEncoding = 'utf-8'
            BindingHelper.write(response.outputStream, query)
        } catch (InvalidArgumentsException e) {
            handleBadRequestResponse(e)
        } catch (AccessDeniedException | NoSuchResourceException e) {
            response.status = 404
            respond error: "Query with id ${id} not found for user."
            return
        }
    }

    /**
     * DELETE /queries/{id}
     * Deletes the user query with the provided id.
     *
     * @param id the database id of the user query.
     * @returns status 204, if the user query exists and is owned by the current user;
     *      404 (Not Found) otherwise.
     */
    def delete(@PathVariable('id') Long id) {
        try {
            queryService.delete(id, authContext.user)
            response.status = HttpStatus.NO_CONTENT.value()
        } catch (AccessDeniedException | NoSuchResourceException e) {
            response.status = 404
            respond error: "Query with id ${id} not found for user."
            return
        }
    }

    /**
     * Deserialises the request body to a user query representation object using Jackson.
     *
     * @returns the user query representation object if deserialisation was successful;
     * responds with code 400 and returns null otherwise.
     */
    protected QueryRepresentation bindUserQuery() {
        validateRequestContentType()

        try {
            return BindingHelper.getRepresentationFromInputStream(request.inputStream, QueryRepresentation.class)
        } catch (BindingException e) {
            return handleBadRequestResponse(e)
        }
    }

    /**
     * Deserialises the request body to a user query representation object using Jackson.
     *
     * @returns the user query representation object if deserialisation was successful;
     * responds with code 400 and returns null otherwise.
     */
    protected QueryUpdateRepresentation bindUserQueryUpdate() {
        validateRequestContentType()

        try {
            return BindingHelper.getRepresentationFromInputStream(request.inputStream, QueryUpdateRepresentation.class)
        } catch (BindingException e) {
            return handleBadRequestResponse(e)
        }
    }

    private void validateRequestContentType() {
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
