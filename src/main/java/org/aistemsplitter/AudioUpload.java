package org.aistemsplitter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AudioUpload {
    private final String uploadId;
    private final String uploadUrl;
    private final Map<String, String> uploadHeaders;
    private final String expiresAt;

    public AudioUpload(String uploadId, String uploadUrl, Map<String, String> uploadHeaders, String expiresAt) {
        this.uploadId = uploadId;
        this.uploadUrl = uploadUrl;
        this.uploadHeaders = Collections.unmodifiableMap(new LinkedHashMap<String, String>(uploadHeaders));
        this.expiresAt = expiresAt;
    }

    public String getUploadId() {
        return uploadId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public Map<String, String> getUploadHeaders() {
        return uploadHeaders;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
