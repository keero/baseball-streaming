package org.sundbybergheat.baseballstreaming.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sundbybergheat.baseballstreaming.clients.FilesClient;
import org.sundbybergheat.baseballstreaming.clients.FilesClient.ImageIOHandler;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;

public class FilesServiceTest {

  private static final List<String> EXPECTED_POSITIONS =
      List.of("C", "1B", "2B", "3B", "SS", "LF", "CF", "RF");

  @Test
  void willInitializeResources(@TempDir File tempDir) throws IOException {
    ImageIOHandler imageIOHandler = new ImageIOHandler();
    FilesClient client = new FilesClient(tempDir.toString(), imageIOHandler);
    FilesService service = new FilesService(client);
    service.initResources();

    Optional<File> basesDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("bases")))
            .findFirst();
    Optional<File> teamResourcesDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("team_resources")))
            .findFirst();
    Optional<File> outsDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("outs")))
            .findFirst();

    assertTrue(basesDir.isPresent());
    assertTrue(outsDir.isPresent());
    assertTrue(teamResourcesDir.isPresent());

    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "ooo.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "oox.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "oxo.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "oxx.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "xoo.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "xox.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "xxo.png"));
    assertTrue(fileExistAndIsNonEmpty(basesDir.get(), "xxx.png"));

    assertTrue(fileExistAndIsNonEmpty(outsDir.get(), "0.png"));
    assertTrue(fileExistAndIsNonEmpty(outsDir.get(), "1.png"));
    assertTrue(fileExistAndIsNonEmpty(outsDir.get(), "2.png"));

    Optional<File> playerImagesDir =
        Stream.of(
                teamResourcesDir
                    .get()
                    .listFiles((f) -> f.isDirectory() && f.getName().equals("player_images")))
            .findFirst();
    Optional<File> flagsDir =
        Stream.of(
                teamResourcesDir
                    .get()
                    .listFiles((f) -> f.isDirectory() && f.getName().equals("flags")))
            .findFirst();
    Optional<File> colorsDir =
        Stream.of(
                teamResourcesDir
                    .get()
                    .listFiles((f) -> f.isDirectory() && f.getName().equals("colors")))
            .findFirst();

    assertTrue(playerImagesDir.isPresent());
    assertTrue(flagsDir.isPresent());
    assertTrue(colorsDir.isPresent());

    assertTrue(fileExistAndIsNonEmpty(playerImagesDir.get(), "default.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "default.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "ALB.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "KGA.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "LEK.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "RAT.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "STO.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "SUN.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "SVL.png"));
    assertTrue(fileExistAndIsNonEmpty(flagsDir.get(), "UME.png"));

    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "LEK.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "KGA.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "RAT.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "STO.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SUN.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SVL.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "UME.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "ALB.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "BRO.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "CIT.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "ENK.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "ENS.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "GEF.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "GOT.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "NIC.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "MAL.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SKE.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SKO.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "TRA.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "UPP.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "FRS.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SOD.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "ORE.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "GBR.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SWE.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "POL.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "TUR.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "SUI.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "VIL.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "EXP.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "MBI.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "LDC.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "VIF.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "default_home.png"));
    assertTrue(fileExistAndIsNonEmpty(colorsDir.get(), "default_away.png"));
  }

  @Test
  void shouldHandleEmptyPlay(@TempDir File tempDir) throws IOException {
    ImageIOHandler imageIOHandler = mock(ImageIOHandler.class);
    when(imageIOHandler.read(any(URL.class)))
        .thenReturn(ImageIO.read(IOUtils.resourceToURL("/wbsc/default-player.jpg")));
    String playJson = "{}";
    FilesClient client = new FilesClient(tempDir.toString(), imageIOHandler);
    FilesService service = new FilesService(client);
    Play play = JsonMapper.fromJson(playJson, Play.class);
    service.updatePlay(play);
  }

  private static Stream<Arguments> createPlayInput() {
    return Stream.of(
        Arguments.of("/wbsc/first_play.json"),
        Arguments.of("/wbsc/last_play.json"),
        Arguments.of("/wbsc/end_of_inning_play.json"),
        Arguments.of("/wbsc/extra_inning_play.json"));
  }

  @ParameterizedTest
  @MethodSource("createPlayInput")
  void shouldUpdatePlay(String playJsonFile, @TempDir File tempDir) throws IOException {
    ImageIOHandler imageIOHandler = mock(ImageIOHandler.class);
    when(imageIOHandler.read(any(URL.class)))
        .thenReturn(ImageIO.read(IOUtils.resourceToURL("/wbsc/default-player.jpg")));
    String playJson = IOUtils.resourceToString(playJsonFile, StandardCharsets.UTF_8);
    FilesClient client = new FilesClient(tempDir.toString(), imageIOHandler);
    client.copyFileFromResource(
        "/wbsc/default-player.jpg", "team_resources/player_images/default.png");
    FilesService service = new FilesService(client);
    Play play = JsonMapper.fromJson(playJson, Play.class);
    service.updatePlay(play);

    Optional<File> currentPitcherDir =
        Stream.of(
                tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("current_pitcher")))
            .findFirst();
    assertTrue(currentPitcherDir.isPresent());

    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "innings.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "strikeouts.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "firstname.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "era.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "pitching-title.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "stats_for_series.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "image.png"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "walks.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "count.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "fullname.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "pitching.txt"));
    assertTrue(fileExistAndIsNonEmpty(currentPitcherDir.get(), "lastname.txt"));

    Optional<File> lineupsDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("lineups")))
            .findFirst();
    assertTrue(lineupsDir.isPresent());

    Optional<File> lineupsHomeDir =
        Stream.of(lineupsDir.get().listFiles((f) -> f.isDirectory() && f.getName().equals("home")))
            .findFirst();
    assertTrue(lineupsHomeDir.isPresent());
    assertLineupIsInOrder(lineupsHomeDir.get());

    Optional<File> lineupsAwayDir =
        Stream.of(lineupsDir.get().listFiles((f) -> f.isDirectory() && f.getName().equals("away")))
            .findFirst();
    assertTrue(lineupsAwayDir.isPresent());
    assertLineupIsInOrder(lineupsAwayDir.get());

    Optional<File> currentBatterDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("current_batter")))
            .findFirst();
    assertTrue(currentBatterDir.isPresent());
    assertBatterIsInOrder(currentBatterDir.get());

    Optional<File> onDeckBatterDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("ondeck_batter")))
            .findFirst();
    assertTrue(onDeckBatterDir.isPresent());
    assertBatterIsInOrder(onDeckBatterDir.get());

    Optional<File> inHoleBatterDir =
        Stream.of(tempDir.listFiles((f) -> f.isDirectory() && f.getName().equals("inhole_batter")))
            .findFirst();
    assertTrue(inHoleBatterDir.isPresent());
    assertBatterIsInOrder(inHoleBatterDir.get());

    assertTrue(fileExistAndIsNonEmpty(tempDir, "home_team.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "home_score.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "home_hits.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "home_errors.txt"));

    assertTrue(fileExistAndIsNonEmpty(tempDir, "away_team.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "away_score.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "away_hits.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "away_errors.txt"));

    assertTrue(fileExistAndIsNonEmpty(tempDir, "count.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "balls.txt"));
    assertTrue(fileExistAndIsNonEmpty(tempDir, "strikes.txt"));

    assertTrue(fileExistAndIsNonEmpty(tempDir, "inning_text.txt"));

    File inningsTextFile =
        Stream.of(tempDir.listFiles((f) -> f.isFile() && f.getName().equals("inning_text.txt")))
            .findFirst()
            .orElseThrow();
    String content = FileUtils.readFileToString(inningsTextFile, StandardCharsets.UTF_8);
    if (!content.startsWith("FINAL")) {
      assertTrue(fileExistAndIsNonEmpty(tempDir, "inning.txt"));
      assertTrue(fileExistAndIsNonEmpty(tempDir, "inning_half.txt"));
    }
  }

  private void assertLineupIsInOrder(final File lineupsDir) throws IOException {
    for (int i = 1; i < 10; i++) {
      final String orderDirName = "%d".formatted(i);
      Optional<File> orderDir =
          Stream.of(
                  lineupsDir.listFiles((f) -> f.isDirectory() && f.getName().equals(orderDirName)))
              .findFirst();
      assertTrue(orderDir.isPresent());
      assertBatterIsInOrder(orderDir.get());
    }

    for (String pos : EXPECTED_POSITIONS) {
      Optional<File> positionDir =
          Stream.of(lineupsDir.listFiles((f) -> f.isDirectory() && f.getName().equals(pos)))
              .findFirst();
      assertTrue(positionDir.isPresent());
      assertBatterIsInOrder(positionDir.get());
    }

    Optional<File> lineupsPitcherDir =
        Stream.of(lineupsDir.listFiles((f) -> f.isDirectory() && f.getName().equals("P")))
            .findFirst();
    Optional<File> lineupsDHDir =
        Stream.of(lineupsDir.listFiles((f) -> f.isDirectory() && f.getName().equals("DH")))
            .findFirst();

    assertTrue(lineupsPitcherDir.isPresent() || lineupsDHDir.isPresent());
    if (lineupsPitcherDir.isPresent()) {
      assertBatterIsInOrder(lineupsPitcherDir.get());
    }
    if (lineupsDHDir.isPresent()) {
      assertBatterIsInOrder(lineupsDHDir.get());
    }
  }

  private void assertBatterIsInOrder(final File batterDir) throws IOException {
    assertTrue(fileExistAndIsNonEmpty(batterDir, "avg.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "firstname.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "stats_for_series.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "image.png"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "pos.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "batting-title.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "ops.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "fullname.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "lastname.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "hr.txt"));
    assertTrue(fileExistAndIsNonEmpty(batterDir, "batting.txt"));
  }

  private boolean fileExistAndIsNonEmpty(final File parent, final String fileName)
      throws IOException {
    Optional<File> file =
        Stream.of(parent.listFiles((dir, name) -> name.equals(fileName))).findFirst();
    boolean exists = file.map(File::isFile).orElse(false);
    if (exists) {
      String content = FileUtils.readFileToString(file.get(), StandardCharsets.UTF_8);
      return content.length() > 0;
    }
    return false;
  }
}
