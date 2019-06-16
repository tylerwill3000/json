package com.github.tylersharpe.json;

public class MalformedJson extends RuntimeException {

  public MalformedJson() {}

  MalformedJson(String message) {
    super(message);
  }

}