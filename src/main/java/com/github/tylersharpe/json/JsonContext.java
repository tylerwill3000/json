package com.github.tylersharpe.json;

class JsonContext {
  enum Type { OBJECT, ARRAY }

  final Type type;
  boolean hasEntries;
  final Object reference;

  JsonContext(Type type, boolean hasEntries, Object reference) {
    this.type = type;
    this.hasEntries = hasEntries;
    this.reference = reference;
  }
}
