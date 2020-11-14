package com.github.tylersharpe.json.annotation;

import com.github.tylersharpe.json.adapter.JsonAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a custom {@link com.github.tylersharpe.json.adapter.JsonAdapter} to use for a specific field
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonSerialization {

    Class<? extends JsonAdapter<?>> value();

}
