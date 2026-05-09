package org.aistemsplitter;

public final class UploadedAudio {
    private final String uploadId;
    private final String fileUrl;
    private final String storageKey;
    private final String expiresAt;
    private final SplitInput input;

    public UploadedAudio(String uploadId, String fileUrl, String storageKey, String expiresAt, SplitInput input) {
        this.uploadId = uploadId;
        this.fileUrl = fileUrl;
        this.storageKey = storageKey;
        this.expiresAt = expiresAt;
        this.input = input;
    }

    public String getUploadId() {
        return uploadId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public SplitInput getInput() {
        return input;
    }
}
