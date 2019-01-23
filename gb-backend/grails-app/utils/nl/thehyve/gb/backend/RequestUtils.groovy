/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend

import groovy.transform.CompileStatic
import nl.thehyve.gb.backend.exception.InvalidArgumentsException

/**
 * Contains http request utilities.
 */
@CompileStatic
class RequestUtils {

    final static Set<String> GLOBAL_PARAMS = [
            "controller",
            "action",
            "format",
            "apiVersion"
    ] as Set

    /**
     * Checks if there are any parameters that are not in the set of default parameters
     * (format, action, controller, apiVersion) or the set of additional parameters for
     * the endpoint passed in <code>acceptedParameters</code>.
     *
     * @param parameters the request parameters
     * @param acceptedParameters the collection of supported non-default parameters.
     * @throws InvalidArgumentsException iff a parameter is used that is not supported.
     */
    static void checkForUnsupportedParams(Map parameters, Collection<String> acceptedParameters)
            throws InvalidArgumentsException{
        def acceptedParams = GLOBAL_PARAMS + acceptedParameters
        def unacceptableParams = parameters.keySet() - acceptedParams
        if (!unacceptableParams.empty) {
            if (unacceptableParams.size() == 1) {
                throw new InvalidArgumentsException("Parameter not supported: ${unacceptableParams.first()}.")
            } else {
                throw new InvalidArgumentsException("Parameters not supported: ${unacceptableParams.join(', ')}.")
            }
        }
    }

}
