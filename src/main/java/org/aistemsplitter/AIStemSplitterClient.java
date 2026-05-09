package org.aistemsplitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AIStemSplitterClient {
    public static final String DEFAULT_BASE_URL = "https://api.aistemsplitter.org/v1";

    private final String apiKey;
    private final String baseUrl;
    private final HttpTransport transport;
    private final Sleeper sleeper;

    public AIStemSplitterClient(String apiKey) throws AIStemSplitterException {
        this(apiKey, DEFAULT_BASE_URL, new UrlConnectionTransport(), new ThreadSleeper());
    }

    public AIStemSplitterClient(String apiKey, String baseUrl, HttpTransport transport, Sleeper sleeper)
            throws AIStemSplitterException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new AIStemSplitterException("Missing API key", 0, "CONFIG_ERROR");
        }
        this.apiKey = apiKey;
        this.baseUrl = trimRightSlash(baseUrl == null ? DEFAULT_BASE_URL : baseUrl);
        this.transport = transport == null ? new UrlConnectionTransport() : transport;
        this.sleeper = sleeper == null ? new ThreadSleeper() : sleeper;
    }

    public Credits getCredits() throws IOException, AIStemSplitterException {
        String data = request("GET", "/credits", null, null);
        return new Credits(Json.intField(data, "balance"), Json.stringField(data, "unit"));
    }

    public AudioUpload createUpload(String filename, String contentType, long contentLength)
            throws IOException, AIStemSplitterException {
        String body = Json.object()
                .put("filename", filename)
                .put("contentType", contentType)
                .put("contentLength", contentLength)
                .toJson();
        String data = request("POST", "/audio/uploads", body, null);
        return new AudioUpload(
                Json.stringField(data, "uploadId"),
                Json.stringField(data, "uploadUrl"),
                Json.stringMapField(data, "uploadHeaders"),
                Json.stringField(data, "expiresAt"));
    }

    public UploadedAudio uploadAudio(String filename, String contentType, byte[] contents)
            throws IOException, AIStemSplitterException {
        AudioUpload upload = createUpload(filename, contentType, contents.length);
        Map<String, String> headers = new LinkedHashMap<String, String>(upload.getUploadHeaders());
        headers.put("Content-Type", contentType);
        HttpTransportResponse response = transport.execute("POST", upload.getUploadUrl(), headers, contents);
        String data = parseData(response);
        String fileUrl = Json.stringField(data, "url");
        String storageKey = Json.stringField(data, "key");
        SplitInput input = SplitInput.uploadedFile(upload.getUploadId(), fileUrl, storageKey);
        return new UploadedAudio(upload.getUploadId(), fileUrl, storageKey, upload.getExpiresAt(), input);
    }

    public CreatedSplit createSplit(CreateSplitRequest splitRequest)
            throws IOException, AIStemSplitterException {
        return createSplit(splitRequest, null);
    }

    public CreatedSplit createSplit(CreateSplitRequest splitRequest, String idempotencyKey)
            throws IOException, AIStemSplitterException {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            headers.put("Idempotency-Key", idempotencyKey);
        }
        String data = request("POST", "/audio/splits", splitRequest.toJson(), headers);
        return new CreatedSplit(
                Json.stringField(data, "id"),
                Json.stringField(data, "status"),
                Json.intField(data, "creditsUsed"),
                Json.stringField(data, "createdAt"));
    }

    public AudioSplit getSplit(String splitId) throws IOException, AIStemSplitterException {
        String encoded = URLEncoder.encode(splitId, "UTF-8").replace("+", "%20");
        String data = request("GET", "/audio/splits/" + encoded, null, null);
        return new AudioSplit(
                Json.stringField(data, "id"),
                Json.stringField(data, "status"),
                Json.stringField(data, "stemModel"),
                Json.stringField(data, "filename"),
                Json.intField(data, "durationSeconds"),
                Json.intField(data, "creditsUsed"),
                Json.stringField(data, "createdAt"),
                Json.stringField(data, "updatedAt"),
                Json.stringMapField(data, "stems"));
    }

    public AudioSplit waitForSplit(String splitId, long timeoutMillis, long intervalMillis)
            throws IOException, AIStemSplitterException, InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (true) {
            AudioSplit split = getSplit(splitId);
            if ("succeeded".equals(split.getStatus()) || "failed".equals(split.getStatus())) {
                return split;
            }
            if (System.currentTimeMillis() >= deadline) {
                throw new AIStemSplitterException("Timed out waiting for split " + splitId, 0, "TIMEOUT");
            }
            sleeper.sleep(Math.min(intervalMillis, Math.max(0, deadline - System.currentTimeMillis())));
        }
    }

    private String request(String method, String path, String body, Map<String, String> extraHeaders)
            throws IOException, AIStemSplitterException {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        if (body != null) {
            headers.put("Content-Type", "application/json");
        }
        if (extraHeaders != null) {
            headers.putAll(extraHeaders);
        }
        byte[] bodyBytes = body == null ? null : body.getBytes(StandardCharsets.UTF_8);
        return parseData(transport.execute(method, baseUrl + path, headers, bodyBytes));
    }

    private static String parseData(HttpTransportResponse response) throws AIStemSplitterException {
        String body = response.getBody() == null ? "" : response.getBody();
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300 && Json.booleanField(body, "success")) {
            return Json.objectField(body, "data");
        }
        String error = Json.objectFieldOrNull(body, "error");
        if (error != null) {
            throw new AIStemSplitterException(
                    Json.stringField(error, "message"),
                    response.getStatusCode(),
                    Json.stringField(error, "code"),
                    body);
        }
        throw new AIStemSplitterException(
                "AIStemSplitter API request failed with status " + response.getStatusCode(),
                response.getStatusCode(),
                "HTTP_ERROR",
                body);
    }

    private static String trimRightSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static final class ThreadSleeper implements Sleeper {
        public void sleep(long milliseconds) throws InterruptedException {
            Thread.sleep(milliseconds);
        }
    }

    private static final class UrlConnectionTransport implements HttpTransport {
        public HttpTransportResponse execute(String method, String url, Map<String, String> headers, byte[] body)
                throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            if (body != null) {
                connection.setDoOutput(true);
                OutputStream output = connection.getOutputStream();
                output.write(body);
                output.close();
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            return new HttpTransportResponse(status, readAll(stream));
        }

        private static String readAll(InputStream stream) throws IOException {
            if (stream == null) {
                return "";
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
