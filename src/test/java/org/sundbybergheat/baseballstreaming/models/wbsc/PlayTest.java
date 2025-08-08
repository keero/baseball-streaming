package org.sundbybergheat.baseballstreaming.models.wbsc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;

public class PlayTest {

  private static Stream<Arguments> createPlayInput() {
    return Stream.of(
        Arguments.of("/wbsc/first_play.json"),
        Arguments.of("/wbsc/last_play.json"),
        Arguments.of("/wbsc/end_of_inning_play.json"),
        Arguments.of("/wbsc/extra_inning_play.json"));
  }

  @ParameterizedTest
  @MethodSource("createPlayInput")
  void shouldParsePlayJson(String playJsonFile) throws IOException {
    String playJson = IOUtils.resourceToString(playJsonFile, StandardCharsets.UTF_8);
    JsonMapper.fromJson(playJson, org.sundbybergheat.baseballstreaming.models.wbsc.play.Play.class);
  }
}
