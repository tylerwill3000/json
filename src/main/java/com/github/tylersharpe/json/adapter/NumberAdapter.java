package com.github.tylersharpe.json.adapter;

import com.github.tylersharpe.json.*;
import com.github.tylersharpe.json.util.JavaType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberAdapter implements JsonAdapter<Number> {

  private static final JsonAdapter<Number> INSTANCE = new NumberAdapter().nullSafe();

  private NumberAdapter() {}

  public static JsonAdapter<Number> getInstance() {
    return INSTANCE;
  }

  @Override
  public void writeObject(JsonWriter jsonWriter, Number obj) throws IOException {
    jsonWriter.writeNumber(obj);
  }

  @Override
  public Number readObject(JsonReader reader, JavaType<? extends Number> javaType) throws IOException {
    Class<? extends Number> numberType = javaType.getRawType();

    if (numberType == BigDecimal.class) {
      return reader.readBigDecimal();

    } else if (numberType == BigInteger.class) {
      return reader.readBigInteger();

    } else if (numberType == Byte.class || numberType == byte.class) {
      return reader.readByte();

    } else if (numberType == Short.class || numberType == short.class) {
      return reader.readShort();

    } else if (numberType == Integer.class || numberType == int.class) {
      return reader.readInt();

    } else if (numberType == Long.class || numberType == long.class) {
      return reader.readLong();

    } else if (numberType == Float.class || numberType == float.class) {
      return reader.readFloat();

    } else if (numberType == Double.class || numberType == double.class) {
      return reader.readDouble();

    } else if (numberType == AtomicInteger.class) {
      return new AtomicInteger(reader.readInt());

    } else if (numberType == AtomicLong.class) {
      return new AtomicLong(reader.readLong());

    } else {
      return reader.readNumber();
    }
  }

}
