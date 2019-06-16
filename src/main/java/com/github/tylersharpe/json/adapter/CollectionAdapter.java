package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonBindException;
import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

public class CollectionAdapter implements JsonAdapter<Collection> {

  private static final JsonAdapter<Collection> INSTANCE = new CollectionAdapter().nullSafe();

  private CollectionAdapter() {}

  public static JsonAdapter<Collection> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Collection collection) throws IOException {
    jsonWriter.writeStartArray(collection);
    for (var item : collection) {
      jsonWriter.writeValue(item);
    }
    jsonWriter.writeEndArray();
  }

  @Override
  @SuppressWarnings({"unchecked", "SimplifyOptionalCallChains"})
  public Collection readObject(JsonReader reader, JavaType<? extends Collection> collectionType) throws IOException {
    Type itemType = collectionType.getGenericType(0)
            .map(type -> type instanceof WildcardType ? Object.class : type)
            .orElse(Object.class);

    var collection = instantiateCollection(collectionType.getRawType());
    reader.iterateNextArray(() -> collection.add(reader.readType(itemType)));
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
    } else {
      return new ArrayList();
    }
  }

}
