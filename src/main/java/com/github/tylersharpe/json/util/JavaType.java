package com.github.tylersharpe.json.util;

import java.lang.reflect.GenericArrayType;
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

  private JavaType(Class<C> rawType, List<Type> genericTypes) {
    this.rawType = rawType;
    this.genericTypes = genericTypes;
  }

  @SuppressWarnings("unchecked")
  public static <C> JavaType<C> from(Type type) {
    return (JavaType<C>) CACHE.computeIfAbsent(type, JavaType::create);
  }

  @SuppressWarnings("unchecked")
  private static JavaType create(Type type) {
    if (type instanceof Class) {
      return new JavaType((Class) type, List.of());

    } else if (type instanceof ParameterizedType) {
      var paramType = ((ParameterizedType) type);
      var rawType = (Class) paramType.getRawType();
      var genericTypes = Stream.of(paramType.getActualTypeArguments()).collect(toList());
      return new JavaType(rawType, genericTypes);

    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return create(componentType);

    } else {
      throw new IllegalArgumentException("Cannot determine raw class from " + type);
    }
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
