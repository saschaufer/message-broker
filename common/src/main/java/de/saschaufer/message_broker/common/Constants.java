package de.saschaufer.message_broker.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public abstract static class Http {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public abstract static class Query {
            public static final String DIRECTORY_ID = "directory-id";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public abstract static class Header {
            public static final String CORRELATION_ID = "X-Correlation-ID";
            public static final String FILE_ID = "X-File-ID";
            public static final String FILE_HASH = "X-File-Hash";
            public static final String USER_AGENT = "User-Agent";
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public abstract static class BodyPart {
            public static final String FILE = "file";
            public static final String VARIABLES = "variables";
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public abstract static class Logging {
        public static final String CORRELATION_ID = "correlation-id";
        public static final String DETAILS = "details";
    }
}
