package com.github.tylersharpe.json;

import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.util.JavaType;
import com.github.tylersharpe.json.util.ThrowingRunnable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.github.tylersharpe.json.JsonContext.Type.ARRAY;
import static com.github.tylersharpe.json.JsonContext.Type.OBJECT;
import static java.util.stream.Collectors.toList;

public class JsonWriter implements Closeable, Flushable {

    private Deque<JsonContext> contextStack = new ArrayDeque<>();
    private Set<Integer> activeReferenceIds = new HashSet<>();
    private Writer writer;
    private JsonToken lastTokenWritten;
    private SerializationContext serializationContext;

    JsonWriter(OutputStream outputStream, SerializationContext serializationContext) {
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.serializationContext = serializationContext;
    }

    public SerializationContext getSerializationContext() {
        return serializationContext;
    }

    public void writeKey(Object key) throws IOException {
        if (key instanceof String stringKey) {
            writeString(stringKey);
        } else {
            writeValue(key);
        }
        serializationContext.getWhitespaceWriter().writeColon(writer);
        lastTokenWritten = JsonToken.COLON;
    }

    public void writeValue(Object obj) throws IOException {
        writeValue(obj, obj == null ? Object.class : obj.getClass());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void writeValue(Object obj, Type objType) throws IOException {
        Class rawType = objType instanceof Class ? (Class) objType : JavaType.from(objType).getRawType();

        JsonAdapter adapter = this.serializationContext.getAdapter(rawType);
        if (adapter == null) {
            throw new JsonBindException("Cannot find adapter for " + objType);
        }

        adapter.writeObject(this, obj);
    }

    public void writeByte(byte b) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(b));
    }

    public void writeShort(short s) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(s));
    }

    public void writeInt(int i) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(i));
    }

    public void writeLong(long l) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(String.valueOf(l)));
    }

    public void writeFloat(float f) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(String.valueOf(f)));
    }

    public void writeDouble(double d) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(String.valueOf(d)));
    }

    public void writeBoolean(boolean bool) throws IOException {
        writeContextItem(bool ? JsonToken.TRUE : JsonToken.FALSE, () -> writer.write(String.valueOf(bool)));
    }

    public void writeString(String str) throws IOException {
        writeContextItem(JsonToken.QUOTE, () -> {
            writer.write('"');

            int lastWrittenIndex = -1;
            for (int currentIndex = 0; currentIndex < str.length(); currentIndex++) {
                char c = str.charAt(currentIndex);

                if (c == '"') {
                    if (lastWrittenIndex < currentIndex - 1) {
                        writer.write(str, lastWrittenIndex + 1, currentIndex - lastWrittenIndex - 1);
                    }
                    writer.write("\\\"");
                    lastWrittenIndex = currentIndex;
                }
            }

            int remainingChars = str.length() - (lastWrittenIndex + 1);
            if (remainingChars > 0) {
                writer.write(str, lastWrittenIndex + 1, remainingChars);
            }

            writer.write('"');
        });
    }

    public void writeNumber(Number number) throws IOException {
        writeContextItem(JsonToken.NUMBER, () -> writer.write(String.valueOf(number)));
    }

    public void writeNull() throws IOException {
        writeContextItem(JsonToken.NULL, () -> writer.write("null"));
    }

    private void writeContextItem(JsonToken endingToken, ThrowingRunnable<IOException> action) throws IOException {
        writePrecedingCommaAndWhitespaceIfNeeded();

        action.run();

        if (contextStack.peek() != null) {
            contextStack.peek().hasEntries = true;
        }
        lastTokenWritten = endingToken;
    }

    public void writeStartArray() throws IOException {
        writeStartArray(null);
    }

    public void writeStartArray(Object reference) throws IOException {
        beginContext(ARRAY, reference);
    }

    public void writeEndArray() throws IOException {
        endContext(ARRAY);
    }

    public void writeStartObject() throws IOException {
        writeStartObject(null);
    }

    public void writeStartObject(Object reference) throws IOException {
        beginContext(OBJECT, reference);
    }

    public void writeEndObject() throws IOException {
        endContext(OBJECT);
    }

    @SuppressWarnings("ConstantConditions")
    private void beginContext(JsonContext.Type type, Object reference) throws IOException {
        writePrecedingCommaAndWhitespaceIfNeeded();

        if (reference != null && !activeReferenceIds.add(System.identityHashCode(reference))) {
            // Object ID seen before - we have a cyclic reference
            Object currentReference = contextStack.peek().reference;

            List<String> referenceChain = contextStack.stream()
                    .map(ctx -> ctx.reference)
                    .distinct()
                    .map(ref -> "  " + (ref == null ? null : ref.getClass()))
                    .collect(toList());

            Collections.reverse(referenceChain);

            throw new CyclicReferenceException(
                    "Cyclic reference to " + reference.getClass() + " detected in " + currentReference.getClass() + "\nThrough reference chain:\n" + String.join("\n", referenceChain)
            );
        }

        contextStack.push(new JsonContext(type, false, reference));
        var startingToken = type == ARRAY ? JsonToken.START_ARRAY : JsonToken.START_OBJECT;
        writer.write(startingToken.character);
        lastTokenWritten = startingToken;
    }

    private void endContext(JsonContext.Type type) throws IOException {
        if (contextStack.peek() == null || contextStack.peek().type != type) {
            throw new MalformedJsonException("Cannot end " + type + " context because the writer is not currently within that context");
        }

        if (contextStack.peek() != null && contextStack.peek().hasEntries) {
            serializationContext.getWhitespaceWriter().writeItemSeparator(writer, contextStack.size() - 1);
        }

        JsonContext finishedContext = contextStack.pop();
        if (finishedContext.reference != null) {
            activeReferenceIds.remove(System.identityHashCode(finishedContext.reference));
        }

        if (contextStack.peek() != null) {
            contextStack.peek().hasEntries = true;
        }

        var endingToken = type == ARRAY ? JsonToken.END_ARRAY : JsonToken.END_OBJECT;
        writer.write(endingToken.character);
        lastTokenWritten = endingToken;
    }

    private void writePrecedingCommaAndWhitespaceIfNeeded() throws IOException {
        if (contextStack.isEmpty() || lastTokenWritten == JsonToken.COLON) {
            return;
        }

        if (contextStack.peek().hasEntries) {
            writer.write(',');
        }

        serializationContext.getWhitespaceWriter().writeItemSeparator(writer, contextStack.size());
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

}