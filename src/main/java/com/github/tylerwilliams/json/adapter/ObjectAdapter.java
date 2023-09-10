package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonBindException;
import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonToken;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.annotation.JsonIgnore;
import com.github.tylerwilliams.json.annotation.JsonProperty;
import com.github.tylerwilliams.json.annotation.JsonSerialization;
import com.github.tylerwilliams.json.objectbuilder.ObjectBuilder;
import com.github.tylerwilliams.json.util.JavaType;
import com.github.tylerwilliams.json.util.SingletonCache;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ObjectAdapter implements JsonAdapter<Object> {

    private static final JsonAdapter<Object> INSTANCE = new ObjectAdapter().nullSafe();

    public static JsonAdapter<Object> getInstance() {
        return INSTANCE;
    }

    private ObjectAdapter() {
    }

    @Override
    public void writeObject(JsonWriter writer, Object obj) throws IOException {
        writer.writeStartObject(obj);

        for (Class clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {

                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isTransient(field.getModifiers())
                        || field.isSynthetic()
                        || field.isAnnotationPresent(JsonIgnore.class)) {
                    continue;
                }

                Object fieldValue;
                try {
                    field.setAccessible(true);
                    fieldValue = field.get(obj);
                } catch (IllegalAccessException e) {
                    throw new JsonBindException(e);
                }

                if (fieldValue == null && !writer.getSerializationContext().isSerializeNulls()) {
                    continue;
                }

                String fieldName = field.isAnnotationPresent(JsonProperty.class) ? field.getDeclaredAnnotation(JsonProperty.class).value() : field.getName();
                writer.writeKey(fieldName);

                JsonSerialization jsonSerialization = field.getAnnotation(JsonSerialization.class);
                if (jsonSerialization != null) {
                    JsonAdapter jsonAdapter = SingletonCache.getInstance(jsonSerialization.value());
                    jsonAdapter.writeObject(writer, fieldValue);
                } else {
                    writer.writeValue(fieldValue, field.getGenericType());
                }
            }
        }

        writer.writeEndObject();
    }

    @Override
    public Object readObject(JsonReader reader, JavaType<?> type) throws IOException {
        if (type.getRawType() == Object.class) {
            JsonToken nextToken = reader.peek();

            return switch (nextToken) {
                case QUOTE -> reader.readString();
                case START_OBJECT -> MapAdapter.getInstance().readObject(reader, JavaType.from(Map.class));
                case START_ARRAY -> CollectionAdapter.getInstance().readObject(reader, JavaType.from(Collection.class));
                case TRUE, FALSE -> reader.readBoolean();
                case NUMBER -> NumberAdapter.getInstance().readObject(reader, JavaType.from(Number.class));
                case NULL -> {
                    reader.readNull();
                    yield null;
                }
                default -> throw reader.createMalformedJsonException("Cannot read a new element starting from token " + nextToken);
            };
        } else {
            ObjectBuilder objectBuilder = ObjectBuilder.newObjectBuilder(type.getRawType());
            reader.iterateNextObject(() -> objectBuilder.accumulateField(reader));
            return objectBuilder.buildObject();
        }
    }

}
