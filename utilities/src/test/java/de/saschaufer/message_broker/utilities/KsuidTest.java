package de.saschaufer.message_broker.utilities;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;

class KsuidTest {

    @Test
    void generateDifferent() throws InterruptedException {

        final int numberThreads = 100;
        final int numberKsuids = 100;

        final Set<String> ksuids = Collections.synchronizedSet(new HashSet<String>(numberThreads * numberKsuids));

        final List<Thread> threads = new ArrayList<>(numberThreads);
        for (int i = 0; i < numberThreads; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < numberKsuids; j++) {
                    ksuids.add(Ksuid.generate());
                }
            }));
        }

        for (final Thread thread : threads) {
            thread.start();
        }

        for (final Thread thread : threads) {
            thread.join();
        }
        assertThat(ksuids.size(), is(numberThreads * numberKsuids));

        for (final String ksuid : ksuids) {
            assertThat(ksuid.length(), is(27));
        }
    }

    @Test
    void getSeconds() {

        final long seconds = Ksuid.getSeconds("HdzBbEgMYfUxCX43zcIds62sCnx");
        assertThat(seconds, is(287_091_323L + 1_400_000_000L));
        assertThat(LocalDateTime.ofEpochSecond(seconds, 0, ZoneOffset.UTC), is(LocalDateTime.of(2023, 6, 18, 12, 28, 43)));

        final long s1 = Ksuid.getSeconds(Ksuid.generate());
        final long s2 = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC);
        assertThat(s1, is(oneOf(s2 - 1, s2, s2 + 1)));
    }

    @Test
    void getTimestampUTC() {
        final LocalDateTime time = Ksuid.getTimestampUTC("HdzBbEgMYfUxCX43zcIds62sCnx");
        assertThat(time, is(LocalDateTime.of(2023, 6, 18, 12, 28, 43)));

        final LocalDateTime t1 = Ksuid.getTimestampUTC(Ksuid.generate());
        final LocalDateTime t2 = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        assertThat(t1, is(oneOf(t2.minus(1, ChronoUnit.SECONDS), t2, t2.plus(1, ChronoUnit.SECONDS))));
    }

    @Test
    void getTimestampLocal() {
        final LocalDateTime time = Ksuid.getTimestampLocal("HdzBbEgMYfUxCX43zcIds62sCnx");
        assertThat(time, is(LocalDateTime.of(2023, 6, 18, 12, 28, 43).atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));

        final LocalDateTime t1 = Ksuid.getTimestampLocal(Ksuid.generate());
        final LocalDateTime t2 = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        assertThat(t1, is(oneOf(t2.minus(1, ChronoUnit.SECONDS), t2, t2.plus(1, ChronoUnit.SECONDS))));
    }

    @Test
    void toHex() {
        assertThat(Ksuid.asHex("HdzBbEgMYfUxCX43zcIds62sCnx"), is("7baa1c11ed246805ac9bf927029521ef674333f9"));
    }
}
