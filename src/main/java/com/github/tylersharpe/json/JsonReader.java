package com.github.tylersharpe.json;

import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.util.CharReader;
import com.github.tylersharpe.json.util.JavaType;
import com.github.tylersharpe.json.util.ThrowingRunnable;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import static com.github.tylersharpe.json.JsonToken.*;

public class JsonReader implements Closeable {

  private static final Set<Character> LITERAL_STOPS = Set.of(',', ']', '}');
  private static final Set<Character> QUOTE_STOP = Collections.singleton('"');

  private CharReader reader;
  private JsonToken lastTokenRead;
  private SerializationContext serializationContext;

  JsonReader(InputStream inputStream, SerializationContext serializationContext) {
    this.reader = new CharReader(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    this.serializationContext = serializationContext;
  }

  public SerializationContext getSerializationContext() {
    return serializationContext;
  }

  public Object readKey(Type bindType) throws IOException {
    Object key = readType(bindType);
    assertNextToken(COLON);
    return key;
  }

  public String readKey() throws IOException {
    String key = readString();
    assertNextToken(COLON);
    return key;
  }

  @SuppressWarnings("unchecked")
  public <T> T readClass(Class<T> bindClass) throws IOException {
    Object result = readType(bindClass);
    return bindClass.isPrimitive() ? (T) result : bindClass.cast(result);
  }

  @SuppressWarnings("unchecked")
  public Object readType(Type bindType) throws IOException {
    readCommaIfNeeded();

    JavaType javaType = JavaType.from(bindType);

    JsonAdapter adapter = serializationContext.getAdapter(javaType.getRawType());
    if (adapter == null) {
      throw new JsonBindException("No adapter found for " + bindType);
    }

    return adapter.readObject(this, javaType);
  }

  public byte readByte() throws IOException {
    return parseNumber(Byte::parseByte);
  }

  public short readShort() throws IOException {
    return parseNumber(Short::parseShort);
  }

  public int readInt() throws IOException {
    return parseNumber(Integer::parseInt);
  }

  public long readLong() throws IOException {
    return parseNumber(Long::parseLong);
  }

  public float readFloat() throws IOException {
    return parseNumber(Float::parseFloat);
  }

  public double readDouble() throws IOException {
    return parseNumber(Double::parseDouble);
  }

  public BigInteger readBigInteger() throws IOException {
    return parseNumber(BigInteger::new);
  }

  public BigDecimal readBigDecimal() throws IOException {
    return parseNumber(BigDecimal::new);
  }

  private <T extends Number> T parseNumber(Function<String, T> parser) throws IOException {
    String literal = readLiteral();
    try {
      var number = parser.apply(literal);
      lastTokenRead = NUMBER;
      return number;
    } catch (NumberFormatException e) {
      throw createMalformedJsonError("Cannot parse number from sequence '" + literal + "'");
    }
  }

  public Number readNumber() throws IOException {
    String literal = readLiteral();
    char firstChar = literal.charAt(0);

    if (Character.isDigit(firstChar) || firstChar == '.' || firstChar == '-') {
      Number number = literal.contains(".") ? new BigDecimal(literal) : new BigInteger(literal);
      lastTokenRead = NUMBER;
      return number;
    } else {
      throw createJsonBindError("Cannot read '" + literal + "' as a number");
    }
  }

  public boolean readBoolean() throws IOException {
    String literal = readLiteral();
    switch (literal) {
      case "true":
        lastTokenRead = TRUE;
        return true;
      case "false":
        lastTokenRead = FALSE;
        return false;
      default:
        throw createMalformedJsonError("Expected boolean but instead found sequence '" + literal + "'");
    }
  }

  public String readString() throws IOException {
    readCommaIfNeeded();
    assertNextToken(QUOTE);
    String readString = reader.readUntil(QUOTE_STOP, true);
    lastTokenRead = QUOTE;
    return readString;
  }

  public void readNull() throws IOException {
    String literal = readLiteral();
    if (!literal.equals("null")) {
      throw createMalformedJsonError("Expected 'null' but instead found sequence '" + literal + "'");
    }
    lastTokenRead = NULL;
  }

  public void iterateNextObject(ThrowingRunnable<IOException> action) throws IOException {
    readStartObject();
    while (peek() != END_OBJECT) {
      action.run();
    }
    readEndObject();
  }

  public void iterateNextArray(ThrowingRunnable<IOException> action) throws IOException {
    readStartArray();
    while (peek() != END_ARRAY) {
      action.run();
    }
    readEndArray();
  }

  public void readStartArray() throws IOException {
    readCommaIfNeeded();
    assertNextToken(START_ARRAY);
  }

  public void readEndArray() throws IOException {
    assertNextToken(END_ARRAY);
  }

  @SuppressWarnings("WeakerAccess")
  public void readStartObject() throws IOException {
    readCommaIfNeeded();
    assertNextToken(START_OBJECT);
  }

  @SuppressWarnings("WeakerAccess")
  public void readEndObject() throws IOException {
    assertNextToken(END_OBJECT);
  }

  private String readLiteral() throws IOException {
    readCommaIfNeeded();
    return reader.readUntil(LITERAL_STOPS, false).trim();
  }

  private void readCommaIfNeeded() throws IOException {
    if (lastTokenRead == END_ARRAY || lastTokenRead == END_OBJECT || lastTokenRead == NUMBER ||
        lastTokenRead == QUOTE || lastTokenRead == NULL || lastTokenRead == TRUE || lastTokenRead == FALSE)
    {
      assertNextToken(COMMA);
    }
  }

  private void assertNextToken(JsonToken token) throws IOException {
    reader.munchWhitespace();

    char next = reader.nextCharacter();
    if (next != token.character) {
      throw createMalformedJsonError("Expected '" + token.character + "' character but instead found '" + next + "'");
    }

    lastTokenRead = token;
  }

  public JsonToken peek() throws IOException {
    reader.munchWhitespace();

    char peekChar = reader.peek();
    JsonToken nextToken = JsonToken.fromCharacter(peekChar);
    if (nextToken == null) {
      throw createMalformedJsonError("Cannot determine next JSON token from character '" + peekChar + "'");
    }

    return nextToken;
  }

  public MalformedJson createMalformedJsonError(String message) {
    return new MalformedJson(message + " (near index: " + reader.getLastIndexRead() + ")");
  }

  public JsonBindException createJsonBindError(String message) {
    return new JsonBindException(message + " (near index: " + reader.getLastIndexRead() + ")");
  }

  @Override
  public void close() throws IOException {
    reader.close();
  }

}
