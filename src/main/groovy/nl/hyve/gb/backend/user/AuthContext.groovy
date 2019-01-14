/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.hyve.gb.backend.user

/**
 * Holds an authenticated user.
 * Makes it easy to fetch the logged in user.
 */
interface AuthContext {
    User getUser()
}
