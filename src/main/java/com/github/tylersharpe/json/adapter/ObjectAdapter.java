package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonToken;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.annotation.JsonIgnore;
import com.github.tylersharpe.json.annotation.JsonProperty;
import com.github.tylersharpe.json.annotation.JsonSerialization;
import com.github.tylersharpe.json.objectbuilder.ObjectBuilder;
import com.github.tylersharpe.json.util.JavaType;
import com.github.tylersharpe.json.util.SingletonCache;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

public class ObjectAdapter implements JsonAdapter<Object> {

  private static final JsonAdapter<Object> INSTANCE = new ObjectAdapter().nullSafe();

  public static JsonAdapter<Object> getInstance() {
    return INSTANCE;
  }

  private ObjectAdapter() {}

  @SuppressWarnings("unchecked")
  @Override
  public void writeObject(JsonWriter jsonWriter, Object obj) throws IOException {
    jsonWriter.writeStartObject(obj);

    for (Class clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
      for (Field field : clazz.getDeclaredFields()) {

        if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.isSynthetic() || field.isAnnotationPresent(JsonIgnore.class)) {
          continue;
        }

        Object fieldValue;
        try {
          field.setAccessible(true);
          fieldValue = field.get(obj);
        } catch (IllegalAccessException e) {
          throw new JsonBindException(e);
        }

        if (fieldValue == null && !jsonWriter.getSerializationContext().isSerializeNulls()) {
          continue;
        }

        String fieldName = field.isAnnotationPresent(JsonProperty.class) ? field.getDeclaredAnnotation(JsonProperty.class).value() : field.getName();
        jsonWriter.writeKey(fieldName);

        JsonSerialization jsonSerialization = field.getAnnotation(JsonSerialization.class);
        if (jsonSerialization != null) {
          JsonAdapter jsonAdapter = SingletonCache.getInstance(jsonSerialization.value());
          jsonAdapter.writeObject(jsonWriter, fieldValue);
        } else {
          jsonWriter.writeValue(fieldValue, field.getGenericType());
        }
      }
    }

    jsonWriter.writeEndObject();
  }

  @Override
  public Object readObject(JsonReader reader, JavaType<?> type) throws IOException {
    if (type.getRawType() == Object.class) {

      JsonToken nextToken = reader.peek();
      switch (nextToken) {
        case QUOTE:
          return reader.readString();
        case START_OBJECT:
          return MapAdapter.getInstance().readObject(reader, JavaType.from(Map.class));
        case START_ARRAY:
          return CollectionAdapter.getInstance().readObject(reader, JavaType.from(Collection.class));
        case NULL:
          reader.readNull();
          return null;
        case TRUE:
        case FALSE:
          return reader.readBoolean();
        case NUMBER:
          return NumberAdapter.getInstance().readObject(reader, JavaType.from(Number.class));
        default:
          throw reader.createMalformedJsonException("Cannot read a new element starting from token " + nextToken);
      }
    } else {
      ObjectBuilder objectBuilder = ObjectBuilder.newObjectBuilder(type.getRawType());
      reader.iterateNextObject(() -> objectBuilder.accumulateField(reader));
      return objectBuilder.buildObject();
    }
  }

}
