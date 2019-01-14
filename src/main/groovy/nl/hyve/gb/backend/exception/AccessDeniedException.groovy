/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.exception

import groovy.transform.InheritConstructors

/**
 * Exception to be thrown whenever the user is denied access to some resource.
 */
@InheritConstructors
class AccessDeniedException extends RuntimeException { }
