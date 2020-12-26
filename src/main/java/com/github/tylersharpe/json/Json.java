package com.github.tylersharpe.json;

import com.github.tylersharpe.json.adapter.DefaultAdapterHolder;
import com.github.tylersharpe.json.adapter.JsonAdapter;
import com.github.tylersharpe.json.util.TypeRegistry;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * Central entry point to the JSON serialization API.
 * Provides various methods for serializing and parsing JSON objects
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Json implements SerializationContext {

    private WhitespaceWriter whitespaceWriter;
    private boolean serializeNulls;
    private boolean allowComments;
    private TypeRegistry<JsonAdapter<?>> adapters;

    public Json() {
        this.whitespaceWriter = WhitespaceWriter.NO_WHITESPACE;
        this.adapters = DefaultAdapterHolder.newRegistry();
    }

    @Override
    public WhitespaceWriter getWhitespaceWriter() {
        return this.whitespaceWriter;
    }

    public void setWhitespaceWriter(WhitespaceWriter whitespaceWriter) {
        this.whitespaceWriter = whitespaceWriter;
    }

    @Override
    public boolean isSerializeNulls() {
        return this.serializeNulls;
    }

    public void setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
    }

    @Override
    public boolean isAllowComments() {
        return allowComments;
    }

    public void setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
    }

    public <T> void registerAdapter(Class<T> clazz, JsonAdapter<? super T> adapter) {
        adapters.register(clazz, adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> JsonAdapter<? super T> getAdapter(Class<T> klass) {
        return (JsonAdapter<? super T>) this.adapters.get(klass);
    }

    public Object parse(String str) throws IOException {
        return parse(str, Object.class);
    }

    public <T> T parse(String str, Class<T> bindClass) throws IOException {
        Object result = parse(str, (Type) bindClass);
        return bindClass.cast(result);
    }

    public Object parse(String str, Type bindType) throws IOException {
        var inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        return parse(inputStream, bindType);
    }

    public Object parse(InputStream input) throws IOException {
        return parse(input, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(InputStream input, Class<T> bindClass) throws IOException {
        return (T) parse(input, (Type) bindClass);
    }

    public Object parse(InputStream input, Type bindType) throws IOException {
        var jsonReader = new JsonReader(input, this);
        Object parsedObject = jsonReader.readType(bindType);
        jsonReader.assertStreamFullyConsumed();
        return parsedObject;
    }

    public String serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialize(obj, out);
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    public void serialize(Object obj, OutputStream out) throws IOException {
        var writer = new JsonWriter(out, this);
        writer.writeValue(obj);
        writer.flush();
    }

}
