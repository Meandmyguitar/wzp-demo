
package com.wzp.util;

public class BytesUtils {
    public BytesUtils() {
    }

    public static long set(long value, int bitIndex) {
        return value | bitOfIndex(bitIndex);
    }

    public static long set(long value, int bitIndex, boolean set) {
        return set ? set(value, bitIndex) : unset(value, bitIndex);
    }

    public static long unset(long value, int bitIndex) {
        return value & ~bitOfIndex(bitIndex);
    }

    public static boolean isSet(long value, int bitIndex) {
        long mask = bitOfIndex(bitIndex);
        return (value & mask) == mask;
    }

    private static long bitOfIndex(int bitIndex) {
        if (bitIndex < 0) {
            throw new IllegalArgumentException("bitIndex不能小于0");
        } else if (bitIndex >= 64) {
            throw new IllegalArgumentException("bitIndex不能大于等于64");
        } else {
            return 1L << bitIndex;
        }
    }
}
