package de.saschaufer.message_broker.utilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base16Test {

    @Test
    void encode() {
        final String in = "egpITfzo2nkWSO7DzENPbWHg93CfDr02YzpBtIsaAZ4qdickKV0iREaDTvvHrL40QYJrR7eeuLhMq1p8ClO";
        assertThat(Base16.encode(in.getBytes()), is("6567704954667a6f326e6b57534f37447a454e50625748673933436644723032597a704274497361415a34716469636b4b5630695245614454767648724c343051594a7252376565754c684d71317038436c4f"));
    }

    @Test
    void decode() {
        final String in = "6567704954667a6f326e6b57534f37447a454e50625748673933436644723032597a704274497361415a34716469636b4b5630695245614454767648724c343051594a7252376565754c684d71317038436c4f";
        assertThat(new String(Base16.decode(in)), is("egpITfzo2nkWSO7DzENPbWHg93CfDr02YzpBtIsaAZ4qdickKV0iREaDTvvHrL40QYJrR7eeuLhMq1p8ClO"));
    }

    @ParameterizedTest
    @MethodSource
    void decode_errors(final String in, final String expected) {
        final RuntimeException e = assertThrows(RuntimeException.class, () -> Base16.decode(in));
        assertThat(e.getMessage(), is(expected));
    }

    static Stream<Arguments> decode_errors() {
        return Stream.of(
                Arguments.of("123", "Expected an even number of characters."),
                Arguments.of("12g3", "'g' is not part of the hexadecimal alphabet."),
                Arguments.of("123h", "'h' is not part of the hexadecimal alphabet.")
        );
    }
}
