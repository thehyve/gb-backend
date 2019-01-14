/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.user

import groovy.transform.Immutable

@Immutable
class User {
    String username
    String realName
}
