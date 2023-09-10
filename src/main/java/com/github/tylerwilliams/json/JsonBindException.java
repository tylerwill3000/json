package com.github.tylerwilliams.json;

/**
 * Exception thrown when there is an issue binding JSON to a java class type
 */
public class JsonBindException extends RuntimeException {

    public JsonBindException(String message) {
        super(message);
    }

    public JsonBindException(Throwable e) {
        super(e);
    }

    public JsonBindException(String message, Throwable e) {
        super(message, e);
    }

}
