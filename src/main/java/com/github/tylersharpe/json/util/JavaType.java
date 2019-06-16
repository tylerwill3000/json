package com.github.tylersharpe.json.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class JavaType<C> {

  private static final Map<Type, JavaType<?>> CACHE = new HashMap<>();

  private Class<C> rawType;
  private List<Type> genericTypes;

  @SuppressWarnings("unchecked")
  private JavaType(Type type) {
    if (type instanceof Class) {
      rawType = (Class<C>) type;
      genericTypes = List.of();
    } else if (type instanceof ParameterizedType) {
      var paramType = ((ParameterizedType) type);
      rawType = (Class<C>) paramType.getRawType();
      genericTypes = Stream.of(paramType.getActualTypeArguments()).collect(toList());
    } else {
      throw new IllegalArgumentException("Cannot determine raw class from " + type);
    }
  }

  @SuppressWarnings("unchecked")
  public static <C> JavaType<C> from(Type type) {
    return (JavaType<C>) CACHE.computeIfAbsent(type, JavaType::new);
  }

  public Class<C> getRawType() {
    return rawType;
  }

  public Optional<Type> getGenericType(int index) {
    if (index >= genericTypes.size()) {
      return Optional.empty();
    }
    return Optional.of(genericTypes.get(index));
  }

}
