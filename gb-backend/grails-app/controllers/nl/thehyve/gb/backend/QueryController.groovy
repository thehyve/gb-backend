/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import nl.thehyve.gb.backend.exception.*
import nl.thehyve.gb.backend.representation.QueryRepresentation
import nl.thehyve.gb.backend.representation.QueryUpdateRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.client.ResourceAccessException

import static nl.thehyve.gb.backend.RequestUtils.checkForUnsupportedParams

class QueryController extends AbstractController {

    static responseFormats = ['json']

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
            checkForUnsupportedParams(params, ['id'])
            def query = queryService.getQueryRepresentationByIdAndUsername(id, authContext.user)

            response.status = HttpStatus.OK.value()
            response.contentType = 'application/json'
            response.characterEncoding = 'utf-8'
            BindingHelper.write(response.outputStream, query)
        } catch (AccessDeniedException | NoSuchResourceException e){
            response.status = 404
            respond error: "Query with id ${id} not found for user."
        } catch (InvalidArgumentsException e) {
            handleBadRequestResponse(e)
        }
    }

    /**
     * POST /queries
     * Saves the user query in the body, which is of type {@link QueryRepresentation}.
     *
     * @returns a representation of the saved query
     *          400 (Bad request) or 503 (Service unavailable) otherwise.
     */
    def save() {
        def body = bindQuery(QueryRepresentation.class)
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
        } catch (ResourceAccessException e) {
            response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
            respond error: e.message
        }
    }

    /**
     * PUT /queries/{id}
     * Saves changes to an existing user query.
     * Changes are specified in the body, which is of type {@link QueryRepresentation}.
     *
     * @param id the identifier of the user query to update.
     * @returns status 204 and the updated query object, if it exists and is owned by the current user;
     *      404 (Not Found), 400 (Bad request) or 503 (Service unavailable) otherwise.
     */
    def update(@PathVariable('id') Long id) {
        def body = bindQuery(QueryUpdateRepresentation.class)
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
        } catch (ResourceAccessException e) {
            response.status = HttpStatus.SERVICE_UNAVAILABLE.value()
            respond error: e.message
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
        }
    }

    /**
     * Deserialises the request body to a user query representation object using Jackson.
     *
     * @returns the user query representation object if deserialisation was successful;
     * responds with code 400 and returns null otherwise.
     */
    protected <T> T bindQuery(Class<T> type) {
        validateRequestContentType()

        try {
            return BindingHelper.getRepresentationFromInputStream(request.inputStream, type)
        } catch (BindingException e) {
            return handleBadRequestResponse(e)
        }
    }



}
