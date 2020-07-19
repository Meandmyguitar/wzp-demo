package com.wzp.util.lang;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

class StringUtilsTest {

    @Test
    void testMask() {
        Assertions.assertEquals("12**56", StringUtils.mask("123456", 2, 2, '*'));
        Assertions.assertEquals("****56", StringUtils.mask("123456", 0, 2, '*'));
        Assertions.assertEquals("12****", StringUtils.mask("123456", 2, 0, '*'));
        Assertions.assertEquals("", StringUtils.mask("", 0, 0, '*'));

        Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtils.mask("123456", 4, 4, '*'));
    }

    @Test
    void testCollapseByCharset() {
        Assertions.assertEquals("说", StringUtils.collapseByCharset("说中文", Charset.forName("utf-8"), 5));
    }
}
