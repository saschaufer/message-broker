package de.saschaufer.message_broker.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Random;

public class Ksuid {

    public static final int TIME_OFFSET = 1_400_000_000;
    public static final int TIME_BYTES = Integer.BYTES;
    public static final int RANDOM_BYTES = 16;
    private static final Random random = new Random();

    private Ksuid() {
    }

    /**
     * Generates a new KSUID.<br>
     * It is made up of the following parts:<br>
     * 1. 4 bytes for seconds since 2014-05-13 in big endian byte order.<br>
     * 2. 16 random bytes.<br>
     * The 20 bytes are encoded in base62, which makes 27 characters.
     *
     * @return KSUID
     */
    public static String generate() {

        final ByteBuffer byteBuffer = ByteBuffer.allocate(TIME_BYTES + RANDOM_BYTES);

        // 4 bytes for seconds since 2014-05-13 (unsigned, big endian)
        final long time = Instant.now().toEpochMilli() / 1000 - TIME_OFFSET;
        byteBuffer.put(ByteUtils.changeEndian(ByteUtils.signedLongToUnsignedInteger(time), ByteOrder.nativeOrder(), ByteOrder.BIG_ENDIAN));

        // 16 random bytes.
        final byte[] payload = new byte[RANDOM_BYTES];
        random.nextBytes(payload);
        byteBuffer.put(payload);

        // Encode to Base62.
        final String ksuid = new String(Base62.encode(byteBuffer.array()));

        // On rare occasions the KSUID is not 27 characters long. In that case just try again.
        if (ksuid.length() != 27) {
            try {
                Thread.sleep(100L);
            } catch (final Exception e) {
                // ignore it
            }
            return generate();
        }

        return ksuid;
    }

    /**
     * Reads the time stamp of the KSUID and adds the offset.
     *
     * @param ksuid to read the time stamp from
     * @return the time stamp plus offset.
     */
    public static long getSeconds(final String ksuid) {

        // Decode from Base62.
        final byte[] bytes = Base62.decode(ksuid.getBytes(StandardCharsets.UTF_8));

        // Get the time bytes.
        final byte[] time_bytes = new byte[TIME_BYTES];
        System.arraycopy(bytes, 0, time_bytes, 0, TIME_BYTES);

        // Get signed long in system endian.
        return ByteUtils.unsignedIntegerToSignedLong(ByteUtils.changeEndian(time_bytes, ByteOrder.BIG_ENDIAN, ByteOrder.nativeOrder())) + TIME_OFFSET;
    }

    /**
     * Reads the time stamp of the KSUID and adds the offset.
     *
     * @param ksuid to read the time stamp from
     * @return the time stamp plus offset as UTC {@link LocalDateTime}.
     */
    public static LocalDateTime getTimestampUTC(final String ksuid) {
        return LocalDateTime.ofEpochSecond(getSeconds(ksuid), 0, ZoneOffset.UTC);
    }

    /**
     * Reads the time stamp of the KSUID and adds the offset.
     *
     * @param ksuid to read the time stamp from
     * @return the time stamp plus offset as a {@link LocalDateTime} at {@link ZoneId#systemDefault()}.
     */
    public static LocalDateTime getTimestampLocal(final String ksuid) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(getSeconds(ksuid)), ZoneId.systemDefault());
    }

    /**
     * Decodes the {@code ksuid} from base62 and encodes it in base16 (hexadecimal).
     *
     * @param ksuid to encode
     * @return the encoded {@code ksuid}
     */
    public static String asHex(final String ksuid) {
        final byte[] bytes = Base62.decode(ksuid.getBytes(StandardCharsets.UTF_8));
        return new String(Base16.encode(bytes));
    }
}
