package de.saschaufer.message_broker.utilities;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


class Base62Test {

    @Test
    void encode() {
        final String input = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÄÖÜß!\\\"§$%&/()=?`";
        final byte[] output = Base62.encode(input.getBytes(StandardCharsets.UTF_8));
        assertThat(new String(output), is("6ai4DwlLq20P8HN9xj0MsqGca4YbDRR5sYyfjjGI1uo5lolX7efaVbqVLEOWZF4OeMsKFnNoSnw9FTvgJRZgqfVrwLkzg0mmR8UiiJkzf94svsoWu"));
    }

    @Test
    void decode() {
        final String input = "6ai4DwlLq20P8HN9xj0MsqGca4YbDRR5sYyfjjGI1uo5lolX7efaVbqVLEOWZF4OeMsKFnNoSnw9FTvgJRZgqfVrwLkzg0mmR8UiiJkzf94svsoWu";
        final byte[] output = Base62.decode(input.getBytes(StandardCharsets.UTF_8));
        assertThat(new String(output), is("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÄÖÜß!\\\"§$%&/()=?`"));
    }
}
