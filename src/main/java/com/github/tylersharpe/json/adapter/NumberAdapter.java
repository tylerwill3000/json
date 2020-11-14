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

    private NumberAdapter() {
    }

    public static JsonAdapter<Number> getInstance() {
        return INSTANCE;
    }

    @Override
    public void writeObject(JsonWriter jsonWriter, Number obj) throws IOException {
        jsonWriter.writeNumber(obj);
    }

    @Override
    public Number readObject(JsonReader jsonReader, JavaType<? extends Number> javaType) throws IOException {
        Class<? extends Number> numberType = javaType.getRawType();

        if (numberType == BigDecimal.class) {
            return jsonReader.readBigDecimal();

        } else if (numberType == BigInteger.class) {
            return jsonReader.readBigInteger();

        } else if (numberType == Byte.class || numberType == byte.class) {
            return jsonReader.readByte();

        } else if (numberType == Short.class || numberType == short.class) {
            return jsonReader.readShort();

        } else if (numberType == Integer.class || numberType == int.class) {
            return jsonReader.readInt();

        } else if (numberType == Long.class || numberType == long.class) {
            return jsonReader.readLong();

        } else if (numberType == Float.class || numberType == float.class) {
            return jsonReader.readFloat();

        } else if (numberType == Double.class || numberType == double.class) {
            return jsonReader.readDouble();

        } else if (numberType == AtomicInteger.class) {
            return new AtomicInteger(jsonReader.readInt());

        } else if (numberType == AtomicLong.class) {
            return new AtomicLong(jsonReader.readLong());

        } else {
            Number genericNumberValue = jsonReader.readNumber();
            if (!numberType.isInstance(genericNumberValue)) {
                throw new JsonBindException(
                    "Parsed generic number '" + genericNumberValue + "' (of type: " + genericNumberValue.getClass().getName() + "), " +
                    "but cannot assign to required type " + numberType.getName()
                );
            }
            return genericNumberValue;
        }
    }

}
