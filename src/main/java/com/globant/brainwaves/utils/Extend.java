package com.globant.brainwaves.utils;

import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Extend {
    public static <T> Predicate<T> isLike(T value) {
        return obj -> (obj.toString().contains(value.toString()));
    }

    public static Stream<String> streamScanner(final Scanner scanner) {
        final Spliterator<String> splt = Spliterators.spliterator(scanner, Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(splt, false)
                .onClose(scanner::close);
    }
}
