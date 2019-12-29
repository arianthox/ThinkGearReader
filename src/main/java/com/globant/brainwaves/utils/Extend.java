package com.globant.brainwaves.utils;

import java.util.function.Predicate;

public class Extend {
    public static <T> Predicate<T> isLike(T value) {
        return obj -> (obj.toString().contains(value.toString()));
    }
}
