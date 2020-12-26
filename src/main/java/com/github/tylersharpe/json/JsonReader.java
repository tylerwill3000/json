package com.github.tylersharpe.json;

import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.util.StopSequenceReader;
import com.github.tylersharpe.json.util.JavaType;
import com.github.tylersharpe.json.util.ThrowingRunnable;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Function;

import static com.github.tylersharpe.json.JsonToken.*;

public class JsonReader implements Closeable {

    private static final Set<Character> LITERAL_STOPS = Set.of(
            COMMA.character, END_ARRAY.character, END_OBJECT.character);

    private static final Set<Character> QUOTE_STOP = Set.of(QUOTE.character);

    private StopSequenceReader reader;
    private JsonToken lastTokenRead;
    private SerializationContext serializationContext;

    JsonReader(InputStream inputStream, SerializationContext serializationContext) {
        this.reader = new StopSequenceReader(
                        new BufferedReader(
                          new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
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
        return (T) readType(bindClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
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
        return parseNextNumber(Byte::parseByte);
    }

    public short readShort() throws IOException {
        return parseNextNumber(Short::parseShort);
    }

    public int readInt() throws IOException {
        return parseNextNumber(Integer::parseInt);
    }

    public long readLong() throws IOException {
        return parseNextNumber(Long::parseLong);
    }

    public float readFloat() throws IOException {
        return parseNextNumber(Float::parseFloat);
    }

    public double readDouble() throws IOException {
        return parseNextNumber(Double::parseDouble);
    }

    public BigInteger readBigInteger() throws IOException {
        return parseNextNumber(BigInteger::new);
    }

    public BigDecimal readBigDecimal() throws IOException {
        return parseNextNumber(BigDecimal::new);
    }

    private <T extends Number> T parseNextNumber(Function<String, T> parser) throws IOException {
        String literal = readLiteral();
        try {
            var number = parser.apply(literal);
            lastTokenRead = NUMBER;
            return number;
        } catch (NumberFormatException e) {
            throw createJsonBindException("Cannot parse number from sequence '" + literal + "'");
        }
    }

    public Number readNumber() throws IOException {
        String literal = readLiteral();
        char firstChar = literal.charAt(0);

        if (Character.isDigit(firstChar) || firstChar == '.' || firstChar == '-') {
            try {
                Number number = literal.contains(".") ? new BigDecimal(literal) : new BigInteger(literal);
                lastTokenRead = NUMBER;
                return number;
            } catch (NumberFormatException ignore) {
                throw createJsonBindException("Cannot read '" + literal + "' as a number");
            }
        } else {
            throw createJsonBindException("Cannot read '" + literal + "' as a number");
        }
    }

    public boolean readBoolean() throws IOException {
        String literal = readLiteral();
        switch (literal) {
            case "true" -> {
                lastTokenRead = TRUE;
                return true;
            }
            case "false" -> {
                lastTokenRead = FALSE;
                return false;
            }
            default -> throw createJsonBindException("Expected boolean but instead found sequence '" + literal + "'");
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
            throw createJsonBindException("Expected 'null' but instead found sequence '" + literal + "'");
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
                lastTokenRead == QUOTE || lastTokenRead == NULL || lastTokenRead == TRUE || lastTokenRead == FALSE) {
            assertNextToken(COMMA);
        }
    }

    private void assertNextToken(JsonToken token) throws IOException {
        consumeWhitespaceAndComments();

        char next = reader.nextCharacter();
        if (next != token.character) {
            throw createMalformedJsonException("Expected '" + token.character + "' character but instead found '" + next + "'");
        }

        lastTokenRead = token;
    }

    void assertStreamFullyConsumed() throws IOException {
        consumeWhitespaceAndComments();
        if (reader.peek() != (char) -1) {
            throw createMalformedJsonException("Additional characters were found after parsing main JSON");
        }
    }

    public JsonToken peek() throws IOException {
        consumeWhitespaceAndComments();

        char peekChar = reader.peekNextUpcomingOnly();
        JsonToken nextToken = JsonToken.fromCharacter(peekChar);
        if (nextToken == null) {
            throw createMalformedJsonException("Cannot determine next JSON token from character '" + peekChar + "'");
        }

        return nextToken;
    }

    private void consumeWhitespaceAndComments() throws IOException {
        reader.munchWhitespace();

        if (serializationContext.isAllowComments() && reader.peekNextUpcomingOnly() == '/') {
            reader.nextCharacter(); // Burn '/'
            char commentType = reader.peek();
            switch (commentType) {
                case '/' -> reader.readUntil("\n");
                case '*' -> reader.readUntil("*/");
                default -> throw createMalformedJsonException("Cannot parse a comment starting from '/" + commentType + "'");
            }

            consumeWhitespaceAndComments(); // In case there are more comment lines
        }
    }

    public MalformedJsonException createMalformedJsonException(String message) {
        return new MalformedJsonException(message + " (near index: " + reader.getLastIndexRead() + ")");
    }

    public JsonBindException createJsonBindException(String message) {
        return new JsonBindException(message + " (near index: " + reader.getLastIndexRead() + ")");
    }

    public JsonBindException createJsonBindException(String message, Throwable rootCause) {
        return new JsonBindException(message + " (near index: " + reader.getLastIndexRead() + ")", rootCause);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
