package org.aistemsplitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AIStemSplitterClientTest {
    public static void main(String[] args) throws Exception {
        getCreditsSendsBearerAuth();
        createSplitSendsBodyAndIdempotencyKey();
        uploadAudioCreatesReservation();
        waitForSplitPollsUntilTerminalStatus();
        apiErrorsThrowTypedException();
        System.out.println("AIStemSplitter Java SDK tests passed");
    }

    private static void getCreditsSendsBearerAuth() throws Exception {
        RecordingTransport transport = new RecordingTransport() {
            public HttpTransportResponse execute(String method, String url, Map<String, String> headers, byte[] body) {
                record(method, url, headers, body);
                assertEquals("GET", method);
                assertEquals("https://api.aistemsplitter.org/v1/credits", url);
                assertEquals("Bearer ast_test_123", headers.get("Authorization"));
                return json(200, "{\"success\":true,\"data\":{\"balance\":6200,\"unit\":\"seconds\"}}");
            }
        };
        Credits credits = new AIStemSplitterClient("ast_test_123", null, transport, noSleep()).getCredits();
        assertEquals(6200, credits.getBalance());
        assertEquals("seconds", credits.getUnit());
        assertEquals(1, transport.requests.size());
    }

    private static void createSplitSendsBodyAndIdempotencyKey() throws Exception {
        RecordingTransport transport = new RecordingTransport() {
            public HttpTransportResponse execute(String method, String url, Map<String, String> headers, byte[] body) {
                record(method, url, headers, body);
                assertEquals("https://api.example.test/v1/audio/splits", url);
                assertEquals("retry-001", headers.get("Idempotency-Key"));
                String json = new String(body, StandardCharsets.UTF_8);
                assertTrue(json.contains("\"type\":\"direct_url\""));
                assertTrue(json.contains("\"stemModel\":\"6s\""));
                return json(200, "{\"success\":true,\"data\":{\"id\":\"split_123\",\"status\":\"queued\",\"creditsUsed\":214,\"createdAt\":\"2026-05-03T10:20:30.000Z\"}}");
            }
        };
        CreateSplitRequest request = new CreateSplitRequest(SplitInput.directUrl("https://example.com/song.mp3"));
        request.setStemModel("6s");
        CreatedSplit split = new AIStemSplitterClient(
                "ast_test_123", "https://api.example.test/v1/", transport, noSleep()).createSplit(request, "retry-001");
        assertEquals("split_123", split.getId());
        assertEquals(214, split.getCreditsUsed());
    }

    private static void uploadAudioCreatesReservation() throws Exception {
        RecordingTransport transport = new RecordingTransport() {
            public HttpTransportResponse execute(String method, String url, Map<String, String> headers, byte[] body) {
                record(method, url, headers, body);
                if (url.endsWith("/audio/uploads")) {
                    String json = new String(body, StandardCharsets.UTF_8);
                    assertTrue(json.contains("\"filename\":\"song.mp3\""));
                    assertTrue(json.contains("\"contentLength\":5"));
                    return json(200, "{\"success\":true,\"data\":{\"uploadId\":\"upl_123\",\"uploadUrl\":\"https://upload.example.com\",\"uploadHeaders\":{\"X-Upload-Token\":\"token_123\"},\"expiresAt\":\"2026-05-03T10:25:30.000Z\"}}");
                }
                assertEquals("https://upload.example.com", url);
                assertEquals("token_123", headers.get("X-Upload-Token"));
                assertEquals("audio/mpeg", headers.get("Content-Type"));
                return json(200, "{\"success\":true,\"data\":{\"url\":\"https://cdn.example.com/audio/api/key_123/upl_123/song.mp3\",\"key\":\"audio/api/key_123/upl_123/song.mp3\"}}");
            }
        };
        UploadedAudio upload = new AIStemSplitterClient("ast_test_123", null, transport, noSleep())
                .uploadAudio("song.mp3", "audio/mpeg", new byte[] {1, 2, 3, 4, 5});
        assertEquals(2, transport.requests.size());
        assertEquals("upl_123", upload.getInput().getUploadId());
        assertEquals("audio/api/key_123/upl_123/song.mp3", upload.getStorageKey());
    }

    private static void waitForSplitPollsUntilTerminalStatus() throws Exception {
        RecordingTransport transport = new RecordingTransport() {
            private int attempts = 0;

            public HttpTransportResponse execute(String method, String url, Map<String, String> headers, byte[] body) {
                record(method, url, headers, body);
                attempts++;
                String status = attempts == 1 ? "processing" : "succeeded";
                return json(200, "{\"success\":true,\"data\":{\"id\":\"split_123\",\"status\":\"" + status + "\",\"stemModel\":\"6s\",\"filename\":\"song.mp3\",\"durationSeconds\":214,\"creditsUsed\":214,\"createdAt\":\"2026-05-03T10:20:30.000Z\",\"updatedAt\":\"2026-05-03T10:22:01.000Z\",\"error\":null}}");
            }
        };
        AudioSplit split = new AIStemSplitterClient("ast_test_123", null, transport, noSleep())
                .waitForSplit("split_123", 1000, 25);
        assertEquals("succeeded", split.getStatus());
        assertEquals(2, transport.requests.size());
    }

    private static void apiErrorsThrowTypedException() throws Exception {
        RecordingTransport transport = new RecordingTransport() {
            public HttpTransportResponse execute(String method, String url, Map<String, String> headers, byte[] body) {
                record(method, url, headers, body);
                return json(401, "{\"success\":false,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid API key\"}}");
            }
        };
        try {
            new AIStemSplitterClient("bad_key", null, transport, noSleep()).getCredits();
            throw new AssertionError("Expected API error");
        } catch (AIStemSplitterException error) {
            assertEquals(401, error.getStatusCode());
            assertEquals("UNAUTHORIZED", error.getCode());
        }
    }

    private static Sleeper noSleep() {
        return new Sleeper() {
            public void sleep(long milliseconds) {
            }
        };
    }

    private static HttpTransportResponse json(int status, String body) {
        return new HttpTransportResponse(status, body);
    }

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private abstract static class RecordingTransport implements HttpTransport {
        final List<RequestRecord> requests = new ArrayList<RequestRecord>();

        void record(String method, String url, Map<String, String> headers, byte[] body) {
            requests.add(new RequestRecord(method, url, headers, body));
        }
    }

    private static final class RequestRecord {
        final String method;
        final String url;
        final Map<String, String> headers;
        final byte[] body;

        RequestRecord(String method, String url, Map<String, String> headers, byte[] body) {
            this.method = method;
            this.url = url;
            this.headers = new LinkedHashMap<String, String>(headers);
            this.body = body;
        }
    }
}
