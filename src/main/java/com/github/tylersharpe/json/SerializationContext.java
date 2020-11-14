package com.github.tylersharpe.json;

import com.github.tylersharpe.json.adapter.JsonAdapter;

/**
 * Exposes methods which provide information that may be needed during serialization / deserialization
 */
public interface SerializationContext {

    boolean isSerializeNulls();

    boolean isAllowComments();

    WhitespaceWriter getWhitespaceWriter();

    <T> JsonAdapter<? super T> getAdapter(Class<T> klass);

}
