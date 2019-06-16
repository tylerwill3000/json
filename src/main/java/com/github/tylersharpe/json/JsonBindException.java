package com.github.tylersharpe.json;

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

}
