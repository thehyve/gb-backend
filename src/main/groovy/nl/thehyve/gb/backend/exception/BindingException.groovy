/*
 * Copyright (c) 2019  The Hyve B.V.
 *  This file is distributed under the GNU Affero General Public License
 *  (see accompanying file LICENSE).
 */

package nl.thehyve.gb.backend.exception

import groovy.transform.InheritConstructors

import javax.validation.ConstraintViolation

@InheritConstructors
class BindingException<T> extends InvalidArgumentsException {
    final Set<ConstraintViolation<T>> errors

    BindingException(String message, Set<ConstraintViolation<T>> errors) {
        super(message)
        this.errors = errors
    }
}
