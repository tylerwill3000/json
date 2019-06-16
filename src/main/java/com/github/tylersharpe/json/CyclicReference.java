package com.github.tylersharpe.json;

/**
 * Thrown when a cyclic reference loop is detected in an object graph during serialization
 */
public class CyclicReference extends RuntimeException {

  CyclicReference(String message) {
    super(message);
  }

}
