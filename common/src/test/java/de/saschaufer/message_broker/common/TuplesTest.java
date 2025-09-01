package de.saschaufer.message_broker.common;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TuplesTest {

    @Test
    void tuple2() {
        assertThat(Tuples.of(1, "2"), is(new Tuples.Tuple2<>(1, "2")));
    }

    @Test
    void tuple3() {
        final byte[] bytes = "3".getBytes(StandardCharsets.UTF_8);
        assertThat(Tuples.of(1, "2", bytes), is(new Tuples.Tuple3<>(1, "2", bytes)));
    }
}
