package com.github.tylersharpe.json.objectbuilder;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.annotation.JsonIgnoreExtraFields;
import com.github.tylersharpe.json.annotation.JsonProperty;
import com.github.tylersharpe.json.annotation.JsonSerialization;
import com.github.tylersharpe.json.util.JavaType;
import com.github.tylersharpe.json.util.SingletonCache;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Builds objects field-by-field as entries are visited in a JSON object or array
 */
public interface ObjectBuilder<T> {

  /**
   * Accumulate a field value from the next object / array entry
   */
  void accumulateField(JsonReader reader) throws IOException;

  /**
   * @return The built object after having visited all entries in the upcoming array / object
   */
  T buildObject();

  @SuppressWarnings("unchecked")
  static Object readFieldValue(JsonReader reader, Field field) throws IOException {
    if (field.isAnnotationPresent(JsonSerialization.class)) {
      var adapterClass = field.getAnnotation(JsonSerialization.class).value();
      JsonAdapter jsonAdapter = SingletonCache.getInstance(adapterClass);
      return jsonAdapter.readObject(reader, JavaType.from(field.getGenericType()));
    } else {
      return reader.readType(field.getGenericType());
    }
  }

  static Field findFieldForJsonProperty(Class clazz, String jsonProperty) {
    try {
      return clazz.getDeclaredField(jsonProperty);
    } catch (NoSuchFieldException ignore) {
      return Stream.of(clazz.getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(JsonProperty.class) &&
                               field.getDeclaredAnnotation(JsonProperty.class).value().equals(jsonProperty))
              .findFirst()
              .orElseGet(() -> {
                if (clazz.isAnnotationPresent(JsonIgnoreExtraFields.class)) {
                  return null;
                }
                throw new JsonBindException("Cannot find field in " + clazz + " to bind json property '" + jsonProperty + "' to");
              });
    }
  }

}
