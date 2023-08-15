package de.saschaufer.message_broker.utilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteUtilsTest {

    @ParameterizedTest
    @MethodSource
    void signedLongToUnsignedInteger(final long in, final byte[] expected) {
        final byte[] bytes = ByteUtils.signedLongToUnsignedInteger(in);
        assertThat(bytes, is(expected));
    }

    static Stream<Arguments> signedLongToUnsignedInteger() {
        return Stream.of(
                Arguments.of(0, new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0}),
                Arguments.of(1, new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1}),
                Arguments.of(16_843_009, new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 1}),
                Arguments.of(2_155_905_152L, new byte[]{(byte) 128, (byte) 128, (byte) 128, (byte) 128}),
                Arguments.of(4_294_967_295L, new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255})
        );
    }

    @ParameterizedTest
    @MethodSource
    void signedLongToUnsignedInteger_errors(final long in, final String expected) {
        final RuntimeException e = assertThrows(RuntimeException.class, () -> ByteUtils.signedLongToUnsignedInteger(in));
        assertThat(e.getMessage(), is(expected));
    }

    static Stream<Arguments> signedLongToUnsignedInteger_errors() {
        return Stream.of(
                Arguments.of(0 - 1, "Must not be smaller than 0 or larger than 4,294,967,295."),
                Arguments.of(4_294_967_295L + 1, "Must not be smaller than 0 or larger than 4,294,967,295.")
        );
    }

    @ParameterizedTest
    @MethodSource
    void unsignedIntegerToSignedLong(final byte[] in, final long expected) {
        final long l = ByteUtils.unsignedIntegerToSignedLong(in);
        assertThat(l, is(expected));
    }

    static Stream<Arguments> unsignedIntegerToSignedLong() {
        return Stream.of(
                Arguments.of(new byte[]{(byte) 0}, 0),
                Arguments.of(new byte[]{(byte) 1}, 1),
                Arguments.of(new byte[]{(byte) 255}, 255),
                Arguments.of(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0}, 0),
                Arguments.of(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1}, 1),
                Arguments.of(new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 1}, 16_843_009),
                Arguments.of(new byte[]{(byte) 128, (byte) 128, (byte) 128, (byte) 128}, 2_155_905_152L),
                Arguments.of(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255}, 4_294_967_295L)
        );
    }

    @Test
    void unsignedIntegerToSignedLong_errors() {

        final byte[] in = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};

        final RuntimeException e = assertThrows(RuntimeException.class, () -> ByteUtils.unsignedIntegerToSignedLong(in));
        assertThat(e.getMessage(), is(String.format("Must not be longer than %s Bytes.", Integer.BYTES)));
    }

    @ParameterizedTest
    @MethodSource
    void toBits(final byte[] in, final List<String> expected) {
        final List<String> bits = ByteUtils.toBits(in);
        assertThat(bits, is(expected));
    }

    static Stream<Arguments> toBits() {
        return Stream.of(
                Arguments.of(new byte[]{(byte) 0}, List.of("00000000")),
                Arguments.of(new byte[]{(byte) 1}, List.of("00000001")),
                Arguments.of(new byte[]{(byte) 127}, List.of("01111111")),
                Arguments.of(new byte[]{(byte) -128}, List.of("10000000")),
                Arguments.of(new byte[]{(byte) 255}, List.of("11111111")),
                Arguments.of(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0}, List.of("00000000", "00000000", "00000000", "00000000")),
                Arguments.of(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1}, List.of("00000000", "00000000", "00000000", "00000001")),
                Arguments.of(new byte[]{(byte) 1, (byte) 1, (byte) 1, (byte) 1}, List.of("00000001", "00000001", "00000001", "00000001")),
                Arguments.of(new byte[]{(byte) 127, (byte) 127, (byte) 127, (byte) 127}, List.of("01111111", "01111111", "01111111", "01111111")),
                Arguments.of(new byte[]{(byte) -128, (byte) -128, (byte) -128, (byte) -128}, List.of("10000000", "10000000", "10000000", "10000000")),
                Arguments.of(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255}, List.of("11111111", "11111111", "11111111", "11111111"))
        );
    }

    @ParameterizedTest
    @MethodSource
    void changeEndian(final byte[] in, final ByteOrder from, final ByteOrder to, final byte[] expected) {
        final byte[] bytes = ByteUtils.changeEndian(in, from, to);
        assertThat(bytes, is(expected));
    }

    static Stream<Arguments> changeEndian() {
        return Stream.of(

                // little to little endian
                Arguments.of(new byte[]{(byte) 0}, ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 0}),
                Arguments.of(new byte[]{(byte) 1}, ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0}, ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 1, (byte) 0}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0, (byte) 128}, ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 1, (byte) 0, (byte) 128}),

                // big to big endian
                Arguments.of(new byte[]{(byte) 0}, ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 0}),
                Arguments.of(new byte[]{(byte) 1}, ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0}, ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 1, (byte) 0}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0, (byte) 128}, ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 1, (byte) 0, (byte) 128}),

                // little to big endian
                Arguments.of(new byte[]{(byte) 0}, ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 0}),
                Arguments.of(new byte[]{(byte) 1}, ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0}, ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 0, (byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0, (byte) 128}, ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN, new byte[]{(byte) 128, (byte) 0, (byte) 1}),

                // big to little endian
                Arguments.of(new byte[]{(byte) 0}, ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 0}),
                Arguments.of(new byte[]{(byte) 1}, ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0}, ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 0, (byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0, (byte) 128}, ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN, new byte[]{(byte) 128, (byte) 0, (byte) 1}),

                // system to system endian
                Arguments.of(new byte[]{(byte) 0}, ByteOrder.nativeOrder(), ByteOrder.nativeOrder(), new byte[]{(byte) 0}),
                Arguments.of(new byte[]{(byte) 1}, ByteOrder.nativeOrder(), ByteOrder.nativeOrder(), new byte[]{(byte) 1}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0}, ByteOrder.nativeOrder(), ByteOrder.nativeOrder(), new byte[]{(byte) 1, (byte) 0}),
                Arguments.of(new byte[]{(byte) 1, (byte) 0, (byte) 128}, ByteOrder.nativeOrder(), ByteOrder.nativeOrder(), new byte[]{(byte) 1, (byte) 0, (byte) 128})
        );
    }
}
