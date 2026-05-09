package org.aistemsplitter;

public final class CreatedSplit {
    private final String id;
    private final String status;
    private final int creditsUsed;
    private final String createdAt;

    public CreatedSplit(String id, String status, int creditsUsed, String createdAt) {
        this.id = id;
        this.status = status;
        this.creditsUsed = creditsUsed;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
