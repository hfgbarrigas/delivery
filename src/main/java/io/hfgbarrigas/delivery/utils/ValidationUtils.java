package io.hfgbarrigas.delivery.utils;

import java.util.Collection;

public class ValidationUtils {

    private ValidationUtils() {
    }

    public static Boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }
}
