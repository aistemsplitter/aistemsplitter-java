package org.aistemsplitter;

public final class SplitInput {
    private final String type;
    private final String url;
    private final String uploadId;
    private final String fileUrl;
    private final String storageKey;

    private SplitInput(String type, String url, String uploadId, String fileUrl, String storageKey) {
        this.type = type;
        this.url = url;
        this.uploadId = uploadId;
        this.fileUrl = fileUrl;
        this.storageKey = storageKey;
    }

    public static SplitInput directUrl(String url) {
        return new SplitInput("direct_url", url, null, null, null);
    }

    public static SplitInput uploadedFile(String uploadId, String fileUrl, String storageKey) {
        return new SplitInput("uploaded_file", null, uploadId, fileUrl, storageKey);
    }

    public String toJson() {
        Json.ObjectBuilder builder = Json.object().put("type", type);
        if (url != null) {
            builder.put("url", url);
        }
        if (uploadId != null) {
            builder.put("uploadId", uploadId).put("fileUrl", fileUrl).put("storageKey", storageKey);
        }
        return builder.toJson();
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
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
}
