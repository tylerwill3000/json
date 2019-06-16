package com.github.tylersharpe.json.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Used to capture generic type info when passing a type to a parsing method
 */
@SuppressWarnings("unused")
public class TypeToken<T> {

  public Type getType() {
    return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

}
