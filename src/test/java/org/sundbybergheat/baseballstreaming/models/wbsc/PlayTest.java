package org.sundbybergheat.baseballstreaming.models.wbsc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;

public class PlayTest {
  @Test
  void shouldParsePlayJson() throws IOException {
    String playJson = IOUtils.resourceToString("/wbsc/playXX.json", StandardCharsets.UTF_8);

    Play play = JsonMapper.fromJson(playJson, Play.class);
    System.out.println(JsonMapper.toJson(play));
  }
}
