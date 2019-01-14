/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.exception

import groovy.transform.InheritConstructors
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
@InheritConstructors
class ServiceNotAvailableException extends RuntimeException {
}
