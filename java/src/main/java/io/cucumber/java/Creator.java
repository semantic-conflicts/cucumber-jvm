package io.cucumber.java;

import io.cucumber.core.backend.Lookup;

import java.lang.reflect.Method;

public interface Creator<A, T> {
    T create(Lookup lookup, Method method, A annotation);
}
