package de.saschaufer.message_broker.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ByteUtils {

    /**
     * Converts a {@link Long} into bytes of an unsigned {@link Integer}. The {@code input} must not be smaller than 0
     * or larger than 4,294,967,295.
     *
     * @param input to convert
     * @return the converted {@code input}
     */
    public static byte[] signedLongToUnsignedInteger(final long input) {

        if (input > 4_294_967_295L || input < 0) {
            throw new RuntimeException("Must not be smaller than 0 or larger than 4,294,967,295.");
        }

        final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
        byteBuffer.putLong(input);

        final byte[] bytes = new byte[Integer.BYTES];
        System.arraycopy(byteBuffer.array(), Integer.BYTES, bytes, 0, Integer.BYTES);

        return bytes;
    }

    /**
     * Converts bytes of an unsigned {@link Integer} into a signed {@link Long}.
     *
     * @param bytes to convert
     * @return the converted {@code bytes}
     */
    public static long unsignedIntegerToSignedLong(final byte[] bytes) {

        if (bytes.length > Integer.BYTES) {
            throw new RuntimeException(String.format("Must not be longer than %s Bytes.", Integer.BYTES));
        }

        long value = 0l;

        for (final byte b : bytes) {
            value = (value << 8) + (b & 255);
        }

        return value;
    }

    /**
     * Converts the {@code bytes} to a List of Strings, were each String contains the 0s and 1s of the byte.
     *
     * @param bytes to convert
     * @return the converted {@code bytes}
     */
    public static List<String> toBits(final byte[] bytes) {

        final List<String> bits = new ArrayList<>(bytes.length);
        for (final byte b : bytes) {
            final StringBuilder result = new StringBuilder();
            int val = b;
            for (int i = 0; i < 8; i++) {
                result.append((val & 128) == 0 ? 0 : 1); // 128 = 1000 0000
                val <<= 1;
            }
            bits.add(result.toString());
        }

        return bits;
    }

    /**
     * Changes the byte order (little or big endian) of the {@code bytes}.
     *
     * @param bytes to convert
     * @param from  byte order of the {@code bytes}
     * @param to    byte order to convert the {@code bytes} to
     * @return the converted {@code bytes}
     */
    public static byte[] changeEndian(final byte[] bytes, final ByteOrder from, final ByteOrder to) {

        if (from == to) {
            return bytes;
        }

        // reverse bytes
        final int middle = bytes.length / 2;
        for (int i = 0; i < middle; i++) {
            final byte lower = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = lower;
        }

        return bytes;
    }
}
