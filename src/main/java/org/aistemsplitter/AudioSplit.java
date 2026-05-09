package org.aistemsplitter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AudioSplit {
    private final String id;
    private final String status;
    private final String stemModel;
    private final String filename;
    private final int durationSeconds;
    private final int creditsUsed;
    private final String createdAt;
    private final String updatedAt;
    private final Map<String, String> stems;

    public AudioSplit(
            String id,
            String status,
            String stemModel,
            String filename,
            int durationSeconds,
            int creditsUsed,
            String createdAt,
            String updatedAt,
            Map<String, String> stems) {
        this.id = id;
        this.status = status;
        this.stemModel = stemModel;
        this.filename = filename;
        this.durationSeconds = durationSeconds;
        this.creditsUsed = creditsUsed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.stems = stems == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<String, String>(stems));
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getStemModel() {
        return stemModel;
    }

    public String getFilename() {
        return filename;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Map<String, String> getStems() {
        return stems;
    }
}
