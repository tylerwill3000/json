package com.github.tylersharpe.json;

/**
 * Thrown when a cyclic reference loop is detected in an object graph during serialization
 */
public class CyclicReferenceException extends JsonBindException {

    CyclicReferenceException(String message) {
        super(message);
    }

}
