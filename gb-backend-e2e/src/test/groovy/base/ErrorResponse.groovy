package base

import groovy.transform.EqualsAndHashCode

// TODO TMT-690 - replace this class with transmart-core-api representation
// https://github.com/thehyve/transmart-core/blob/dev/transmart-core-api/src/main/groovy/org/transmartproject/core/multidimquery/ErrorResponse.groovy
@EqualsAndHashCode
class ErrorResponse {
    Integer httpStatus
    String type
    String error
    String message
    String path
    Date timestamp
}
