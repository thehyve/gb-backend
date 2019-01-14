/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.exception

import groovy.transform.InheritConstructors

/**
 * An exception type to designate the submission of invalid data to a resource
 * method.
 */
@InheritConstructors
class InvalidRequestException extends RuntimeException { }
