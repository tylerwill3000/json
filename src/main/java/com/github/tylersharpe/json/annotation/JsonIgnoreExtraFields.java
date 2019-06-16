package com.github.tylersharpe.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes marked with this annotation will allow un-mapped JSON properties to be ignored during de-serialization
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonIgnoreExtraFields {}
