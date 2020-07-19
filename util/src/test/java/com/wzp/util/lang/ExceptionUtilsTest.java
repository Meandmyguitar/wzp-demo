package com.wzp.util.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ExceptionUtilsTest {

    @Test
    void testThrowUnchecked() {
        Assertions.assertThrows(IOException.class, this::throwUnchecked);
    }

    private void throwUnchecked() {
        throw ExceptionUtils.throwUnchecked(new IOException());
    }
}
