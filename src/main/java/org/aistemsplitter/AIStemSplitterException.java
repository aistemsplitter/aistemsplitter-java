package org.aistemsplitter;

public final class AIStemSplitterException extends Exception {
    private final int statusCode;
    private final String code;
    private final String details;

    public AIStemSplitterException(String message, int statusCode, String code) {
        this(message, statusCode, code, null);
    }

    public AIStemSplitterException(String message, int statusCode, String code, String details) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.details = details;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }
}
