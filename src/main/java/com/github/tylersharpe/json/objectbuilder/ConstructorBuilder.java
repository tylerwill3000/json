package com.github.tylersharpe.json.objectbuilder;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.annotation.JsonConstructor;
import com.github.tylersharpe.json.annotation.JsonProperty;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Constructs a java class instance by accumulating object entries as they are visited and
 * then calling a {@link JsonConstructor} annotated constructor to create the instance
 */
public class ConstructorBuilder<T> implements ObjectBuilder<T> {

  private Constructor<T> constructor;
  private List<String> constructorJsonProperties;
  private Map<String, Object> jsonProperties = new HashMap<>();

  public ConstructorBuilder(final Constructor<T> constructor) {
    this.constructor = constructor;
    this.constructorJsonProperties = Stream.of(constructor.getParameters()).map(constructorParam -> {
      if (!constructorParam.isAnnotationPresent(JsonProperty.class)) {
        throw new IllegalStateException(
          "@" + JsonProperty.class.getSimpleName() + " annotation is required on all constructor parameters. " +
          "The annotation is missing on " + constructorParam + " in " + constructor.getDeclaringClass()
        );
      }
      return constructorParam.getDeclaredAnnotation(JsonProperty.class).value();
    }).collect(toList());
  }

  @Override
  public void accumulateField(JsonReader reader) throws IOException {
    String jsonProperty = reader.readKey();
    Field field = ObjectBuilder.findFieldForJsonProperty(constructor.getDeclaringClass(), jsonProperty);

    if (field == null) {
      reader.readClass(Object.class);
      return;
    }

    Object fieldValue = ObjectBuilder.readFieldValue(reader, field);
    jsonProperties.put(jsonProperty, fieldValue);
  }

  @Override
  public T buildObject() {
    List<Object> constructorArgs = constructorJsonProperties.stream().map(property -> {
      if (!jsonProperties.containsKey(property)) {
        throw new JsonBindException("Constructor property '" + property + "' not found in JSON object: " + jsonProperties);
      }
      return jsonProperties.get(property);
    }).collect(toList());

    try {
      return constructor.newInstance(constructorArgs.toArray());
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new JsonBindException(e);
    }
  }

}
