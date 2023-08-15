package de.saschaufer.message_broker.utilities;

import java.io.ByteArrayOutputStream;

/**
 * Encoder/Decoder to/from Base62.<br>
 * Inspired by <a href="https://github.com/seruco/base62">https://github.com/seruco/base62</a>
 */
public class Base62 {

    /**
     * Characters needed to encode to Base62:<br>
     * 0-9<br>
     * A-Z<br>
     * a-z
     */
    private static final byte[] ALPHABET = {

            /* 0 - 9 */
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9',

            /* A - Z */
            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J',
            (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T',
            (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',

            /* a - z */
            (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
            (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't',
            (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z'
    };

    /**
     * Characters needed to decode from Base62 (Unicode Alphabet):<br>
     * Index 48 - 57: 0 - 9<br>
     * Index 65 - 90: A - Z<br>
     * Index 97 - 122: a - z<br>
     * The gabs between the characters are filled up with zeros. Those are not used , but are needed to create an array
     * of the needed characters on the respective index.
     */
    private static final byte[] UNICODE = {

            /* Gap */
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),

            /* 0 - 9 */
            (byte) (0 & 0xFF), (byte) (1 & 0xFF), (byte) (2 & 0xFF), (byte) (3 & 0xFF), (byte) (4 & 0xFF), (byte) (5 & 0xFF),
            (byte) (6 & 0xFF), (byte) (7 & 0xFF), (byte) (8 & 0xFF), (byte) (9 & 0xFF),

            /* Gap */
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),
            (byte) (0 & 0xFF),

            /* A - Z */
            (byte) (10 & 0xFF), (byte) (11 & 0xFF), (byte) (12 & 0xFF), (byte) (13 & 0xFF), (byte) (14 & 0xFF),
            (byte) (15 & 0xFF), (byte) (16 & 0xFF), (byte) (17 & 0xFF), (byte) (18 & 0xFF), (byte) (19 & 0xFF),
            (byte) (20 & 0xFF), (byte) (21 & 0xFF), (byte) (22 & 0xFF), (byte) (23 & 0xFF), (byte) (24 & 0xFF),
            (byte) (25 & 0xFF), (byte) (26 & 0xFF), (byte) (27 & 0xFF), (byte) (28 & 0xFF), (byte) (29 & 0xFF),
            (byte) (30 & 0xFF), (byte) (31 & 0xFF), (byte) (32 & 0xFF), (byte) (33 & 0xFF), (byte) (34 & 0xFF),
            (byte) (35 & 0xFF), (byte)

            /* Gap */
            (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF), (byte) (0 & 0xFF),

            /* a - z */
            (byte) (36 & 0xFF), (byte) (37 & 0xFF), (byte) (38 & 0xFF), (byte) (39 & 0xFF), (byte) (40 & 0xFF),
            (byte) (41 & 0xFF), (byte) (42 & 0xFF), (byte) (43 & 0xFF), (byte) (44 & 0xFF), (byte) (45 & 0xFF),
            (byte) (46 & 0xFF), (byte) (47 & 0xFF), (byte) (48 & 0xFF), (byte) (49 & 0xFF), (byte) (50 & 0xFF),
            (byte) (51 & 0xFF), (byte) (52 & 0xFF), (byte) (53 & 0xFF), (byte) (54 & 0xFF), (byte) (55 & 0xFF),
            (byte) (56 & 0xFF), (byte) (57 & 0xFF), (byte) (58 & 0xFF), (byte) (59 & 0xFF), (byte) (60 & 0xFF),
            (byte) (61 & 0xFF)
    };

    private static final int BASE_256 = 256;
    private static final int BASE_62 = 62;

    private Base62() {
    }

    /**
     * Encodes the {@code input} to Base62.
     *
     * @param input to encode
     * @return the encoded {@code input}
     */
    public static byte[] encode(final byte[] input) {
        return translate(coding(input, BASE_256, BASE_62), ALPHABET);
    }

    /**
     * Decodes the Base62 {@code input}.
     *
     * @param input to decode
     * @return decoded {@code input}
     */
    public static byte[] decode(final byte[] input) {
        return coding(translate(input, UNICODE), BASE_62, BASE_256);
    }

    /**
     * Encodes the {@code input} from {@code baseInput} to {@code baseOutput}.
     *
     * @param input      to encode
     * @param baseInput  the base of the input
     * @param baseOutput the base of the output
     * @return the encoded {@code input}
     */
    private static byte[] coding(final byte[] input, final int baseInput, final int baseOutput) {

        final int estimatedLength = (int) Math.ceil((Math.log(baseInput) / Math.log(baseOutput)) * input.length);
        final ByteArrayOutputStream out = new ByteArrayOutputStream(estimatedLength);

        byte[] in = input.clone();

        while (in.length > 0) {
            final ByteArrayOutputStream quotient = new ByteArrayOutputStream(in.length);

            int remainder = 0;

            for (int i = 0; i < in.length; i++) {
                final int accumulator = (in[i] & 0xFF) + remainder * baseInput;
                final int digit = (accumulator - (accumulator % baseOutput)) / baseOutput;

                remainder = accumulator % baseOutput;

                if (quotient.size() > 0 || digit > 0) {
                    quotient.write(digit);
                }
            }

            out.write(remainder);

            in = quotient.toByteArray();
        }

        // Pad output with zeros corresponding to the number of leading zeros in the message.
        for (int i = 0; i < input.length - 1 && input[i] == 0; i++) {
            out.write(0);
        }

        return revert(out.toByteArray());
    }

    /**
     * Turns the array around.
     *
     * @param input the array to turn around
     * @return the turned around array
     */
    private static byte[] revert(final byte[] input) {

        final byte[] in = input.clone();

        final int middle = in.length / 2;
        for (int i = 0; i < middle; i++) {
            final byte lower = in[i];
            in[i] = in[in.length - i - 1];
            in[in.length - i - 1] = lower;
        }

        return in;
    }

    /**
     * Translates the {@code input} through the {@code dictionary}. The {@code input} is an array of numbers
     * representing the indices of the characters in the {@code dictionary}.
     *
     * @param input      to translate
     * @param dictionary to translate with
     * @return the translated {@code input}
     */
    private static byte[] translate(final byte[] input, final byte[] dictionary) {

        final byte[] in = input.clone();

        for (int i = 0; i < in.length; i++) {
            in[i] = dictionary[in[i]];
        }

        return in;
    }
}
