/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.user

import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class User {
    String username
    String realName
}
