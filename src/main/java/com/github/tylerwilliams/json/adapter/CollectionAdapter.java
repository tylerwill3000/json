package com.github.tylerwilliams.json.adapter;

import com.github.tylerwilliams.json.JsonBindException;
import com.github.tylerwilliams.json.JsonReader;
import com.github.tylerwilliams.json.JsonWriter;
import com.github.tylerwilliams.json.util.JavaType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

@SuppressWarnings("rawtypes")
public class CollectionAdapter implements JsonAdapter<Collection> {

    private static final JsonAdapter<Collection> INSTANCE = new CollectionAdapter().nullSafe();

    private CollectionAdapter() {
    }

    public static JsonAdapter<Collection> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter writer, Collection collection) throws IOException {
        writer.writeStartArray(collection);
        for (var item : collection) {
            writer.writeValue(item);
        }
        writer.writeEndArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection readObject(JsonReader reader, JavaType<? extends Collection> collectionType) throws IOException {
        Type itemType = collectionType.getGenericType(0)
                .map(type -> type instanceof WildcardType ? JavaType.parseBoundType((WildcardType) type) : type)
                .orElse(Object.class);

        var collection = instantiateCollection(collectionType.getRawType());
        reader.iterateNextArray(() -> {
            var deserializedItem = reader.readType(itemType);
            collection.add(deserializedItem);
        });
        return collection;
    }

    private Collection instantiateCollection(Class<? extends Collection> clazz) {
        if (!clazz.isInterface()) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new JsonBindException(e);
            }
        }

        if (SortedSet.class.isAssignableFrom(clazz)) {
            return new TreeSet();
        } else if (Set.class.isAssignableFrom(clazz)) {
            return new HashSet();
        } else if (Queue.class.isAssignableFrom(clazz)) {
            return new ArrayDeque();
        } else if (ArrayList.class.isAssignableFrom(clazz)) {
            return new ArrayList();
        } else {
            throw new JsonBindException("Unknown collection type: " + clazz);
        }
    }

}
