package de.saschaufer.message_broker.utilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Encoder/Decoder to/from Base16 (hexadecimal).
 */
public class Base16 {

    /**
     * Characters needed to encode to Base16:<br>
     * 0-9<br>
     * a-f<br>
     * <br>
     * Every 4 bytes are replaced with one character from the alphabet based on the index.
     */
    private static final char[] ALPHABET = "0123456789abcdef".toCharArray();

    /**
     * Characters needed to decode from Base16:<br>
     * Every character is replaced with the respective byte and every two characters are merged into one byte.
     */
    private static final Map<Character, Byte> BYTES = new HashMap<>() {{
        put('0', (byte) 0);
        put('1', (byte) 1);
        put('2', (byte) 2);
        put('3', (byte) 3);
        put('4', (byte) 4);
        put('5', (byte) 5);
        put('6', (byte) 6);
        put('7', (byte) 7);
        put('8', (byte) 8);
        put('9', (byte) 9);
        put('a', (byte) 10);
        put('b', (byte) 11);
        put('c', (byte) 12);
        put('d', (byte) 13);
        put('e', (byte) 14);
        put('f', (byte) 15);
    }};

    private Base16() {
    }

    /**
     * Encodes the {@code input} to Base16.
     *
     * @param input to encode
     * @return the encoded {@code input}
     */
    public static String encode(final byte[] input) {

        final char[] hex = new char[input.length * 2];

        for (int i = 0; i < input.length; i++) {
            int v = input[i] & 0xFF;
            hex[i * 2] = ALPHABET[v >>> 4];       // upper 4 bits
            hex[i * 2 + 1] = ALPHABET[v & 0x0F];  // lower 4 bits
        }

        return new String(hex);
    }

    /**
     * Decodes the Base16 {@code input}.
     *
     * @param input to decode
     * @return decoded {@code input}
     */
    public static byte[] decode(final String input) {

        if (input.length() % 2 != 0) {
            throw new RuntimeException("Expected an even number of characters.");
        }

        final byte[] bytes = new byte[input.length() / 2];

        int count = 0;
        for (int i = 0; i < input.length(); i += 2) {

            final char cu = input.charAt(i);
            final char cl = input.charAt(i + 1);

            if (!BYTES.containsKey(cu)) {
                throw new RuntimeException(String.format("'%s' is not part of the hexadecimal alphabet.", cu));
            }

            if (!BYTES.containsKey(cl)) {
                throw new RuntimeException(String.format("'%s' is not part of the hexadecimal alphabet.", cl));
            }

            final byte u = BYTES.get(cu);
            final byte l = BYTES.get(cl);
            bytes[count] = (byte) (u << 4);
            bytes[count] = (byte) (bytes[count] | l);
            count++;
        }

        return bytes;
    }
}
