package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.JsonReader;
import com.github.tylersharpe.json.JsonWriter;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayAdapter implements JsonAdapter<Object> {

  private static final JsonAdapter<Object> INSTANCE = new ArrayAdapter().nullSafe();

  private ArrayAdapter() {}

  public static JsonAdapter<Object> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Object array) throws IOException {
    jsonWriter.writeStartArray();

    int length = Array.getLength(array);
    for (int i = 0; i < length; i++) {
      Object arrayItem = Array.get(array, i);
      jsonWriter.writeValue(arrayItem);
    }

    jsonWriter.writeEndArray();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object readObject(JsonReader reader, JavaType<?> type) throws IOException {
    Class arrayItemType = type.getRawType().getComponentType();

    List list = new ArrayList();
    reader.iterateNextArray(() -> list.add(reader.readClass(arrayItemType)));

    var array = Array.newInstance(arrayItemType, list.size());
    if (arrayItemType.isPrimitive()) {
      fillPrimitiveArray(list, array);
      return array;
    } else {
      return list.toArray((Object[]) array);
    }
  }

  private void fillPrimitiveArray(List list, Object primitiveArray) {
    for (int i = 0; i < list.size(); i++) {
      Array.set(primitiveArray, i, list.get(i));
    }
  }

}
