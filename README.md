# AIStemSplitter Java SDK

Official Java SDK for [AIStemSplitter](https://aistemsplitter.org), an AI-powered stem splitter that separates vocals, drums, bass, and other instruments from uploaded audio files or direct audio URLs.

## Links

- Homepage: https://aistemsplitter.org
- API docs: https://aistemsplitter.org/developers/api
- API base: https://api.aistemsplitter.org/v1
- OpenAPI: https://api.aistemsplitter.org/openapi.yaml

## Maven

```xml
<dependency>
  <groupId>org.aistemsplitter</groupId>
  <artifactId>aistemsplitter-java</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Gradle

```gradle
implementation 'org.aistemsplitter:aistemsplitter-java:0.1.0'
```

## Usage

```java
import org.aistemsplitter.AIStemSplitterClient;
import org.aistemsplitter.CreateSplitRequest;
import org.aistemsplitter.SplitInput;

AIStemSplitterClient client = new AIStemSplitterClient(
    System.getenv("AISTEMSPLITTER_API_KEY"));

CreateSplitRequest request = new CreateSplitRequest(
    SplitInput.directUrl("https://example.com/song.mp3"));
request.setStemModel("6s");

System.out.println(client.createSplit(request).getId());
```

Publishing is blocked until Maven Central namespace ownership, Sonatype Central Portal credentials, and GPG signing are available.
