package org.aistemsplitter;

import java.io.IOException;
import java.util.Map;

public interface HttpTransport {
    HttpTransportResponse execute(
            String method,
            String url,
            Map<String, String> headers,
            byte[] body) throws IOException;
}
