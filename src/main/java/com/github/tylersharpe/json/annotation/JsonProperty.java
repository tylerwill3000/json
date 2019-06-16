package com.github.tylersharpe.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to customize the JSON property that a field maps to.
 * By default, the field's name is used as the JSON property to map to
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonProperty {

  String value();

}
