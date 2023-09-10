package com.github.tylerwilliams.json.util;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {

    void run() throws E;

}
