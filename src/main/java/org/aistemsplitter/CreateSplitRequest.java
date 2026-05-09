package org.aistemsplitter;

public final class CreateSplitRequest {
    private final SplitInput input;
    private String stemModel;
    private String outputFormat;
    private String webhookUrl;

    public CreateSplitRequest(SplitInput input) {
        this.input = input;
    }

    public String toJson() {
        Json.ObjectBuilder builder = Json.object().putRaw("input", input.toJson());
        if (stemModel != null) {
            builder.put("stemModel", stemModel);
        }
        if (outputFormat != null) {
            builder.put("outputFormat", outputFormat);
        }
        if (webhookUrl != null) {
            builder.put("webhookUrl", webhookUrl);
        }
        return builder.toJson();
    }

    public SplitInput getInput() {
        return input;
    }

    public String getStemModel() {
        return stemModel;
    }

    public void setStemModel(String stemModel) {
        this.stemModel = stemModel;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
