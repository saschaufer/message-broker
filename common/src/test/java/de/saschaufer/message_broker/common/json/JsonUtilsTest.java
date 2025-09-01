package de.saschaufer.message_broker.common.json;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JsonUtilsTest {

    @Test
    void toJson_positive() {

        final TestObject testObject = new TestObject(
                "abc",
                123,
                MediaType.TEXT_PLAIN,
                LocalDateTime.parse("2027-01-02T13:14:15.123456789"),
                Instant.ofEpochSecond(789),
                List.of(Map.of(
                        "a", new TestObject(
                                "def",
                                456,
                                null,
                                null,
                                null,
                                List.of(Map.of(
                                        "b", new TestObject(
                                                "ghi",
                                                789,
                                                MediaType.APPLICATION_PDF,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        );

        final String json = assertDoesNotThrow(() -> JsonUtils.toJson(testObject));

        assertThat(json, is("""
                {
                    "string": "abc",
                    "integer": 123,
                    "mediaType": "text/plain",
                    "localDateTime": "2027-01-02T13:14:15.123456789",
                    "instant": "1970-01-01T00:13:09Z",
                    "list": [
                        {
                            "a": {
                                "string": "def",
                                "integer": 456,
                                "list": [
                                    {
                                        "b": {
                                            "string": "ghi",
                                            "integer": 789,
                                            "mediaType": "application/pdf",
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
                """
                .replace("\n", "")
                .replace("    ", "")
                .replace(": ", ":")
        ));
    }

    @Test
    void toJson_positive_Null() {

        final TestObject testObject = null;

        final String json = assertDoesNotThrow(() -> JsonUtils.toJson(testObject));

        assertThat(json, nullValue());
    }

    @Test
    void toJson_positive_String() {

        final String string = "abc";

        final String json = assertDoesNotThrow(() -> JsonUtils.toJson(string));

        assertThat(json, is("abc"));
    }

    @Test
    void toJson_positive_Bytes() {

        final byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);

        final String json = assertDoesNotThrow(() -> JsonUtils.toJson(bytes));

        assertThat(json, is("abc"));
    }

    @Test
    void fromJson_positive_Bytes() {

        final byte[] bytes = """
                {
                    "string": "abc",
                    "integer": 123,
                    "mediaType": "application/json",
                    "localDateTime": "2027-01-02T13:14:15.123456789",
                    "instant": "1970-01-01T00:13:09Z",
                    "list": [
                        {
                            "a": {
                                "string": "def",
                                "integer": 456,
                                "mediaType": null,
                                "list": [
                                    {
                                        "b": {
                                            "string": "ghi",
                                            "integer": 789,
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
                """.getBytes(StandardCharsets.UTF_8);

        final TestObject testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(bytes, TestObject.class));

        assertThat(testObject, is(new TestObject(
                "abc",
                123,
                MediaType.APPLICATION_JSON,
                LocalDateTime.parse("2027-01-02T13:14:15.123456789"),
                Instant.ofEpochSecond(789),
                List.of(Map.of(
                        "a", new TestObject(
                                "def",
                                456,
                                null,
                                null,
                                null,
                                List.of(Map.of(
                                        "b", new TestObject(
                                                "ghi",
                                                789,
                                                null,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        )));
    }

    @Test
    void fromJson_positive_BytesNull() {

        final byte[] bytes = null;

        final TestObject testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(bytes, TestObject.class));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_BytesEmpty() {

        final byte[] bytes = {};

        final TestObject testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(bytes, TestObject.class));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_String() {

        final String string = """
                {
                    "string": "abc",
                    "integer": 123,
                    "mediaType": "application/json",
                    "localDateTime": "2027-01-02T13:14:15.123456789",
                    "instant": "1970-01-01T00:13:09Z",
                    "list": [
                        {
                            "a": {
                                "string": "def",
                                "integer": 456,
                                "mediaType": null,
                                "list": [
                                    {
                                        "b": {
                                            "string": "ghi",
                                            "integer": 789,
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
                """;

        final TestObject testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(string, TestObject.class));

        assertThat(testObject, is(new TestObject(
                "abc",
                123,
                MediaType.APPLICATION_JSON,
                LocalDateTime.parse("2027-01-02T13:14:15.123456789"),
                Instant.ofEpochSecond(789),
                List.of(Map.of(
                        "a", new TestObject(
                                "def",
                                456,
                                null,
                                null,
                                null,
                                List.of(Map.of(
                                        "b", new TestObject(
                                                "ghi",
                                                789,
                                                null,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        )));
    }

    @Test
    void fromJson_positive_StringNull() {

        final String string = null;

        final TestObject testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(string, TestObject.class));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_StringEmpty() {

        final String string = "";

        final TestObject testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(string, TestObject.class));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_BytesList() {

        final byte[] bytes = """
                [
                {
                    "string": "abc",
                    "integer": 123,
                    "mediaType": "application/json",
                    "localDateTime": "2027-01-02T13:14:15.123456789",
                    "instant": "1970-01-01T00:13:09Z",
                    "list": [
                        {
                            "a": {
                                "string": "def",
                                "integer": 456,
                                "list": [
                                    {
                                        "b": {
                                            "string": "ghi",
                                            "integer": 789,
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    "string": "def",
                    "integer": 456,
                    "mediaType": "text/plain",
                    "list": [
                        {
                            "b": {
                                "string": "ghi",
                                "integer": 789,
                                "localDateTime": "2026-01-02T13:14:15.123456789",
                                "instant": "1970-01-01T00:07:36Z",
                                "list": [
                                    {
                                        "c": {
                                            "string": "jkl",
                                            "integer": 101,
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
                ]
                """.getBytes(StandardCharsets.UTF_8);

        final List<TestObject> testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(bytes, new TypeReference<List<TestObject>>() {
        }));

        assertThat(testObject.size(), is(2));
        assertThat(testObject.getFirst(), is(new TestObject(
                "abc",
                123,
                MediaType.APPLICATION_JSON,
                LocalDateTime.parse("2027-01-02T13:14:15.123456789"),
                Instant.ofEpochSecond(789),
                List.of(Map.of(
                        "a", new TestObject(
                                "def",
                                456,
                                null,
                                null,
                                null,
                                List.of(Map.of(
                                        "b", new TestObject(
                                                "ghi",
                                                789,
                                                null,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        )));
        assertThat(testObject.getLast(), is(new TestObject(
                "def",
                456,
                MediaType.TEXT_PLAIN,
                null,
                null,
                List.of(Map.of(
                        "b", new TestObject(
                                "ghi",
                                789,
                                null,
                                LocalDateTime.parse("2026-01-02T13:14:15.123456789"),
                                Instant.ofEpochSecond(456),
                                List.of(Map.of(
                                        "c", new TestObject(
                                                "jkl",
                                                101,
                                                null,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        )));
    }

    @Test
    void fromJson_positive_BytesListNull() {

        final byte[] bytes = null;

        final List<TestObject> testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(bytes, new TypeReference<List<TestObject>>() {
        }));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_BytesListEmpty() {

        final byte[] bytes = {};

        final List<TestObject> testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(bytes, new TypeReference<List<TestObject>>() {
        }));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_StringList() {

        final String string = """
                [
                {
                    "string": "abc",
                    "integer": 123,
                    "mediaType": "application/json",
                    "localDateTime": "2027-01-02T13:14:15.123456789",
                    "instant": "1970-01-01T00:13:09Z",
                    "list": [
                        {
                            "a": {
                                "string": "def",
                                "integer": 456,
                                "list": [
                                    {
                                        "b": {
                                            "string": "ghi",
                                            "integer": 789,
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                },
                {
                    "string": "def",
                    "integer": 456,
                    "mediaType": "text/plain",
                    "list": [
                        {
                            "b": {
                                "string": "ghi",
                                "integer": 789,
                                "localDateTime": "2026-01-02T13:14:15.123456789",
                                "instant": "1970-01-01T00:07:36Z",
                                "list": [
                                    {
                                        "c": {
                                            "string": "jkl",
                                            "integer": 101,
                                            "localDateTime":"2025-01-02T13:14:15.123456789",
                                            "instant":"1970-01-01T00:02:03Z",
                                            "list": [{}]
                                        }
                                    }
                                ]
                            }
                        }
                    ]
                }
                ]
                """;

        final List<TestObject> testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(string, new TypeReference<List<TestObject>>() {
        }));

        assertThat(testObject.size(), is(2));
        assertThat(testObject.getFirst(), is(new TestObject(
                "abc",
                123,
                MediaType.APPLICATION_JSON,
                LocalDateTime.parse("2027-01-02T13:14:15.123456789"),
                Instant.ofEpochSecond(789),
                List.of(Map.of(
                        "a", new TestObject(
                                "def",
                                456,
                                null,
                                null,
                                null,
                                List.of(Map.of(
                                        "b", new TestObject(
                                                "ghi",
                                                789,
                                                null,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        )));
        assertThat(testObject.getLast(), is(new TestObject(
                "def",
                456,
                MediaType.TEXT_PLAIN,
                null,
                null,
                List.of(Map.of(
                        "b", new TestObject(
                                "ghi",
                                789,
                                null,
                                LocalDateTime.parse("2026-01-02T13:14:15.123456789"),
                                Instant.ofEpochSecond(456),
                                List.of(Map.of(
                                        "c", new TestObject(
                                                "jkl",
                                                101,
                                                null,
                                                LocalDateTime.parse("2025-01-02T13:14:15.123456789"),
                                                Instant.ofEpochSecond(123),
                                                List.of(Map.of())
                                        )
                                ))
                        )
                ))
        )));
    }

    @Test
    void fromJson_positive_StringListNull() {

        final String string = null;

        final List<TestObject> testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(string, new TypeReference<List<TestObject>>() {
        }));

        assertThat(testObject, nullValue());
    }

    @Test
    void fromJson_positive_StringListEmpty() {

        final String string = "";

        final List<TestObject> testObject = assertDoesNotThrow(() -> JsonUtils.fromJson(string, new TypeReference<List<TestObject>>() {
        }));

        assertThat(testObject, nullValue());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestObject {
        private String string;
        private Integer integer;
        private MediaType mediaType;
        private LocalDateTime localDateTime;
        private Instant instant;
        private List<Map<String, TestObject>> list;
    }
}
