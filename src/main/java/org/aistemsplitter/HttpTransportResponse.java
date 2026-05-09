package org.aistemsplitter;

public final class HttpTransportResponse {
    private final int statusCode;
    private final String body;

    public HttpTransportResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
