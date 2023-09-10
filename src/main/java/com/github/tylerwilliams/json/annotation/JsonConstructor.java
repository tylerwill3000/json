package com.github.tylerwilliams.json.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a constructor that should be used to create objects during de-serialization.
 * This allows immutable classes to be created which consume all parameters in the constructor
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonConstructor {}
