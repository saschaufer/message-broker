package de.saschaufer.message_broker.plugin.spi;

public abstract class Constants {
    public static abstract class Logging {
        public static final String CORRELATION_ID = "correlation-id";
        public static final String DETAILS = "details";
        public static final String ENDPOINT = "endpoint";
        public static final String HEADER = "header";
        public static final String METHOD = "method";
        public static final String PAYLOAD = "payload";
    }

    public static abstract class Http {
        public static abstract class Header {
            public static final String CORRELATION_ID = "X-Correlation-Id";
            public static final String SERVER_TIMING = "Server-Timing";
        }
    }
}
