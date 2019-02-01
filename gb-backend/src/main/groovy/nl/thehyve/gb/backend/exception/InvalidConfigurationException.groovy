/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.exception

import groovy.transform.InheritConstructors


/**
 * An exception type to designate the invalid configuration setting.
 */
@InheritConstructors
class InvalidConfigurationException extends RuntimeException { }
