package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonBindException;
import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MapAdapter implements JsonAdapter<Map> {

    private static final JsonAdapter<Map> INSTANCE = new MapAdapter().nullSafe();

    private MapAdapter() {
    }

    public static JsonAdapter<Map> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Map map) throws IOException {
        writer.writeStartObject(map);

        map.forEach((key, value) -> {
            try {
                writer.writeKey(key);
                writer.writeValue(value);
            } catch (IOException e) {
                throw new JsonBindException(e);
            }
        });

        writer.writeEndObject();
    }

    @Override
    public Map<?, ?> readObject(JsonReader reader, JavaType<? extends Map> mapType) throws IOException {
        Type keyType = mapType.getGenericType(0)
                .map(type -> {
                    if (type instanceof WildcardType) {
                        Type boundType = JavaType.parseBoundType((WildcardType) type);
                        return boundType == Object.class ? String.class : boundType;
                    }
                    return type;
                })
                .orElse(String.class);

        Type valueType = mapType.getGenericType(1)
                .map(type -> type instanceof WildcardType ? JavaType.parseBoundType((WildcardType) type) : type)
                .orElse(Object.class);

        Map map = instantiateMap(mapType.getRawType());

        reader.iterateNextObject(() -> {
            Object key = reader.readKey(keyType);
            Object value = reader.readType(valueType);
            map.put(key, value);
        });

        return map;
    }

    private Map instantiateMap(Class<? extends Map> mapType) {
        try {
            if (!mapType.isInterface()) {
                return mapType.getConstructor().newInstance();
            }
            return new LinkedHashMap();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new JsonBindException(e);
        }
    }

}
