package org.sundbybergheat.baseballstreaming.services;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.clients.FilesClient;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Player;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayerSeasonStats;

public class FilesService {
  private static final Logger LOG = LoggerFactory.getLogger(FilesService.class);

  private Play play = null;

  private final FilesClient filesClient;

  public FilesService(final FilesClient filesClient) {
    this.filesClient = filesClient;
  }

  public void initResources() throws IOException {

    if (!filesClient.fileExists("baseball-streaming-all-overlays.json")) {
      String obsScenes =
          IOUtils.resourceToString(
              "/obs/baseball-streaming-all-overlays.json", StandardCharsets.UTF_8);
      filesClient.writeStringToFile(
          "baseball-streaming-all-overlays.json",
          obsScenes.replace("[PATH_TO_OBS_RESOURCE_DIR]", filesClient.getResourceBasePath()));
    }
    filesClient.copyFileFromResource("/bases/ooo.png", "bases/ooo.png");
    filesClient.copyFileFromResource("/bases/oox.png", "bases/oox.png");
    filesClient.copyFileFromResource("/bases/oxo.png", "bases/oxo.png");
    filesClient.copyFileFromResource("/bases/oxx.png", "bases/oxx.png");
    filesClient.copyFileFromResource("/bases/xoo.png", "bases/xoo.png");
    filesClient.copyFileFromResource("/bases/xox.png", "bases/xox.png");
    filesClient.copyFileFromResource("/bases/xxo.png", "bases/xxo.png");
    filesClient.copyFileFromResource("/bases/xxx.png", "bases/xxx.png");

    filesClient.copyFileFromResource("/outs/0.png", "outs/0.png");
    filesClient.copyFileFromResource("/outs/1.png", "outs/1.png");
    filesClient.copyFileFromResource("/outs/2.png", "outs/2.png");

    filesClient.copyFileFromResource(
        "/images/player-image-default.png", "team_resources/player_images/default.png");
    filesClient.copyFileFromResource(
        "/images/team-flag-default.png", "team_resources/flags/default.png");

    ImageTools.getTeamColors().entrySet().stream()
        .forEach(
            entry -> {
              try {
                filesClient.writePng(
                    entry.getValue(),
                    String.format("team_resources/colors/%s.png", entry.getKey()));
              } catch (IOException e) {
                LOG.error("Unable to write team color file for {}", entry.getKey(), e);
              }
            });
  }

  protected void updatePlay(final Play play) {
    this.play = play;
    updateState();
  }

  private void updateState() {
    try {
      updateScoreBoard();
      updateLineups();
      updateCurrentPitcher();
      updateCurrentBatter();
      updateOnDeckBatter();
      updateInHoleBatter();
    } catch (IOException e) {
      LOG.error("Something went wrong", e);
    }
  }

  private void updateScoreBoard() throws IOException {
    String homeColorFile = String.format("team_resources/colors/%s.png", play.eventHome());
    String awayColorFile = String.format("team_resources/colors/%s.png", play.eventAway());
    String homeFlagFile = String.format("team_resources/flags/%s.png", play.eventHomeId());
    String awayFlagFile = String.format("team_resources/flags/%s.png", play.eventAwayId());
    String defaultHomeColorFile = "team_resources/colors/default_home.png";
    String defaultAwayColorFile = "team_resources/colors/default_away.png";
    String defaultFlagFile = "team_resources/flags/default.png";

    filesClient.copyFile(
        filesClient.fileExists(homeColorFile) ? homeColorFile : defaultHomeColorFile,
        "home_color.png");
    filesClient.copyFile(
        filesClient.fileExists(awayColorFile) ? awayColorFile : defaultAwayColorFile,
        "away_color.png");
    filesClient.copyFile(
        filesClient.fileExists(homeFlagFile) ? homeFlagFile : defaultFlagFile, "home_flag.png");
    filesClient.copyFile(
        filesClient.fileExists(awayFlagFile) ? awayFlagFile : defaultFlagFile, "away_flag.png");
    filesClient.writeStringToFile("home_team.txt", play.eventHome());
    filesClient.writeStringToFile("away_team.txt", play.eventAway());
    filesClient.writeStringToFile("home_score.txt", play.lineScore().homeTotals().runs());
    filesClient.writeStringToFile("away_score.txt", play.lineScore().awayTotals().runs());
    filesClient.writeStringToFile("home_hits.txt", play.lineScore().homeTotals().hits());
    filesClient.writeStringToFile("away_hits.txt", play.lineScore().awayTotals().hits());
    filesClient.writeStringToFile("home_errors.txt", play.lineScore().homeTotals().errors());
    filesClient.writeStringToFile("away_errors.txt", play.lineScore().awayTotals().errors());
    updateBases();
    updateOuts();
    if (!"FINAL".equals(play.situation().currentInning())) {
      filesClient.writeStringToFile(
          "inning_half.txt",
          play.situation().currentInning().startsWith("TOP") ? "\u25B2" : "\u25BC");
      filesClient.writeStringToFile("inning.txt", play.situation().currentInning().split(" ")[1]);
    }
    filesClient.writeStringToFile("inning_text.txt", play.situation().currentInning());
    filesClient.writeStringToFile(
        "count.txt",
        String.format("%s - %s", play.situation().balls(), play.situation().strikes()));
    filesClient.writeStringToFile("balls.txt", play.situation().balls());
    filesClient.writeStringToFile("strikes.txt", play.situation().strikes());
  }

  private void updateBases() throws IOException {
    String basesTarget = "bases.png";
    char first = "0".equals(play.situation().runnerOnFirst()) ? 'o' : 'x';
    char second = "0".equals(play.situation().runnerOnSecond()) ? 'o' : 'x';
    char third = "0".equals(play.situation().runnerOnThird()) ? 'o' : 'x';
    String basesSource = String.format("bases/%c%c%c.png", third, second, first);
    filesClient.copyFile(basesSource, basesTarget);
  }

  private void updateOuts() throws IOException {
    String outsTarget = "outs.png";
    if ("1".equals(play.situation().outs())) {
      filesClient.copyFile("outs/1.png", outsTarget);
    } else if ("2".equals(play.situation().outs())) {
      filesClient.copyFile("outs/2.png", outsTarget);
    } else {
      filesClient.copyFile("outs/0.png", outsTarget);
    }
  }

  private void updateCurrentPitcher() throws IOException {
    Player pitcher =
        play.boxScore().entrySet().stream()
            .filter(
                kv ->
                    kv.getKey()
                        .startsWith(
                            play.situation().currentInning().startsWith("TOP") ? "29" : "19"))
            .sorted((a, b) -> Integer.parseInt(b.getKey()) - Integer.parseInt(a.getKey()))
            .findFirst()
            .map(kv -> kv.getValue())
            .get();
    updatePitcher(pitcher, "current_pitcher");
  }

  private void updatePitcher(final Player pitcher, final String subdir) throws IOException {
    filesClient.writeStringToFile(subdir + "/count.txt", String.format("P: %s", pitcher.pitches()));

    filesClient.writeStringToFile(subdir + "/firstname.txt", pitcher.firstName());
    filesClient.writeStringToFile(subdir + "/fullname.txt", pitcher.fullName());
    filesClient.writeStringToFile(subdir + "/lastname.txt", pitcher.lastName());

    // Update images
    updateImages(pitcher, subdir);

    filesClient.writeStringToFile(
        subdir + "/pitching.txt",
        PitcherTools.pitcherNarrative(pitcher, Integer.parseInt(play.innings())));
    filesClient.writeStringToFile(
        subdir + "/pitching-title.txt", PitcherTools.pitcherNarrativeTitle(pitcher));

    final PlayerSeasonStats stats = pitcher.seasonStats();

    final Integer earnedRuns = Integer.parseInt(stats.earnedRuns());
    final Integer outs = Integer.parseInt(stats.outs());
    final Integer walks = Integer.parseInt(stats.pitcherWalks());
    final Integer strikeouts = Integer.parseInt(stats.pitcherStrikeouts());

    if (earnedRuns == 0 && outs == 0 && walks == 0 && strikeouts == 0) {
      LOG.warn("No stats for pitcher {} (id={})", pitcher.fullName(), pitcher.playerId());
      filesClient.writeStringToFile(subdir + "/era.txt", "-");
      filesClient.writeStringToFile(subdir + "/games.txt", "");
      filesClient.writeStringToFile(subdir + "/hits.txt", "");
      filesClient.writeStringToFile(subdir + "/hrs-allowed.txt", "");
      filesClient.writeStringToFile(subdir + "/innings.txt", "0.0");
      filesClient.writeStringToFile(subdir + "/strikeouts.txt", "0");
      filesClient.writeStringToFile(subdir + "/walks.txt", "0");
      filesClient.writeStringToFile(subdir + "/wins-losses.txt", "");
      return;
    }

    double innings = outs / 3.0;
    double era = earnedRuns / (innings / Integer.parseInt(play.innings()));

    filesClient.writeStringToFile(
        subdir + "/era.txt", String.format("%.2f", era).replace(",", "."));
    filesClient.writeStringToFile(subdir + "/games.txt", "");
    filesClient.writeStringToFile(subdir + "/hits.txt", "");
    filesClient.writeStringToFile(subdir + "/hrs-allowed.txt", "");
    filesClient.writeStringToFile(subdir + "/innings.txt", PitcherTools.inningsVal(innings));
    filesClient.writeStringToFile(subdir + "/strikeouts.txt", stats.pitcherStrikeouts());
    filesClient.writeStringToFile(subdir + "/walks.txt", stats.pitcherWalks());
    filesClient.writeStringToFile(subdir + "/wins-losses.txt", "");
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", "Stats for this season");
  }

  private void updateCurrentBatter() throws IOException {
    String batterId = play.situation().batterId();
    Player batter = BatterTools.aggregatedBoxScore(batterId, play);
    updateBatter(batter, "current_batter");
  }

  private void updateOnDeckBatter() throws IOException {
    String currentBatterKey =
        play.playData().stream()
            .sorted((a, b) -> Integer.parseInt(b.atBat()) - Integer.parseInt(a.atBat()))
            .findFirst()
            .get()
            .batter();
    int side = Integer.parseInt(currentBatterKey.substring(0, 2));
    int order = Integer.parseInt(currentBatterKey.substring(2, 3));
    int nextOrder = order == 9 ? 1 : order + 1;
    String prefix = String.format("%d%d", side, nextOrder);
    String key =
        play.boxScore().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
            .findFirst()
            .get();
    Player batter = BatterTools.aggregatedBoxScore(play.boxScore().get(key).playerId(), play);
    updateBatter(batter, "ondeck_batter");
  }

  private void updateInHoleBatter() throws IOException {
    String currentBatterKey =
        play.playData().stream()
            .sorted((a, b) -> Integer.parseInt(b.atBat()) - Integer.parseInt(a.atBat()))
            .findFirst()
            .get()
            .batter();
    int side = Integer.parseInt(currentBatterKey.substring(0, 2));
    int order = Integer.parseInt(currentBatterKey.substring(2, 3));
    int nextNextOrder = order == 8 ? 1 : (order == 9 ? 2 : order + 2);
    String prefix = String.format("%d%d", side, nextNextOrder);
    String key =
        play.boxScore().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
            .findFirst()
            .get();
    Player batter = BatterTools.aggregatedBoxScore(play.boxScore().get(key).playerId(), play);
    updateBatter(batter, "inhole_batter");
  }

  private void updateBatter(final Player batter, final String subdir) throws IOException {

    filesClient.writeStringToFile(subdir + "/firstname.txt", batter.firstName());
    filesClient.writeStringToFile(subdir + "/lastname.txt", batter.lastName().toUpperCase());
    filesClient.writeStringToFile(subdir + "/pos.txt", batter.position());
    filesClient.writeStringToFile(subdir + "/fullname.txt", batter.fullName());

    // Update images
    updateImages(batter, subdir);
    filesClient.writeStringToFile(
        subdir + "/batting.txt", BatterTools.batterNarrative(batter, play));
    filesClient.writeStringToFile(
        subdir + "/batting-title.txt", BatterTools.batterNarrativeTitle(batter));

    final PlayerSeasonStats stats = batter.seasonStats();

    if (Integer.parseInt(stats.plateAppearances()) == 0) {
      LOG.warn("No stats for batter {} (id={})", batter.fullName(), batter.playerId());
      filesClient.writeStringToFile(subdir + "/avg.txt", "");
      filesClient.writeStringToFile(subdir + "/ops.txt", "");
      filesClient.writeStringToFile(subdir + "/hr.txt", "");
      filesClient.writeStringToFile(subdir + "/rbi.txt", "");
      filesClient.writeStringToFile(subdir + "/stats_for_series.txt", "");
      filesClient.writeStringToFile(subdir + "/batting-title.txt", "");
      return;
    }

    Integer hits = Integer.parseInt(stats.hits());
    Integer doubles = Integer.parseInt(stats.doubles());
    Integer triples = Integer.parseInt(stats.triples());
    Integer homeruns = Integer.parseInt(stats.homeruns());
    Integer singles = hits - doubles - triples - homeruns;
    Integer walks = Integer.parseInt(stats.walks());
    Integer hitByPitch = Integer.parseInt(stats.hitByPitch());
    Integer plateAppearances = Integer.parseInt(stats.plateAppearances());
    Integer atBats = Integer.parseInt(stats.atBats());
    Double battingAverage = hits.doubleValue() / atBats.doubleValue();
    Double onBasePercentage = (hits + walks + hitByPitch) / plateAppearances.doubleValue();
    Double sluggingPercentage =
        (singles + doubles * 2.0 + triples * 3.0 + homeruns * 4.0) / atBats.doubleValue();
    filesClient.writeStringToFile(
        subdir + "/avg.txt", "%.3f".formatted(battingAverage).replace(",", ".").substring(1));
    filesClient.writeStringToFile(
        subdir + "/ops.txt",
        "%.3f".formatted(onBasePercentage + sluggingPercentage).replace(",", ".").substring(1));
    filesClient.writeStringToFile(subdir + "/hr.txt", stats.homeruns());
    filesClient.writeStringToFile(subdir + "/rbi.txt", "");
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", "Stats for this season");
  }

  private void updateImages(final Player player, final String subdir) throws IOException {
    String teamFlagPath = String.format("team_resources/flags/%s.png", player.teamId());
    String clubFlagPath = String.format("team_resources/flags/%s.png", player.teamCode());

    if (filesClient.fileExists(teamFlagPath)) {
      filesClient.copyFile(teamFlagPath, subdir + "/flag.png");
    } else if (filesClient.fileExists(clubFlagPath)) {
      filesClient.copyFile(clubFlagPath, subdir + "/flag.png");
    } else {
      filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
    }

    String playerImagePath =
        String.format("team_resources/player_images/%s-%s.png", player.teamId(), player.playerId());
    if (filesClient.fileExists(playerImagePath)) {
      filesClient.copyFile(playerImagePath, subdir + "/image.png");
    } else {
      filesClient.copyImageFromURL(URI.create(player.imageUrl()).toURL(), playerImagePath);
    }

    filesClient.copyFile(
        player.teamId().equals(play.eventHomeId()) ? "home_color.png" : "away_color.png",
        subdir + "/team_color.png");
  }

  private void updateLineups() throws IOException {
    List<Player> homeLineup = LineupTools.getHomeLineup(play);
    List<Player> awayLineup = LineupTools.getAwayLineup(play);

    if (homeLineup.size() != 9 || awayLineup.size() != 9) {
      LOG.error(
          "Invalid lineups. Expected 9 batters each but got {} home batters and {} away batters.",
          homeLineup.size(),
          awayLineup.size());
      return;
    }

    for (int order = 1; order < 10; order += 1) {
      updateBatter(homeLineup.get(order - 1), "lineups/home/" + order);
    }

    for (Player batter : homeLineup) {
      String position =
          Optional.ofNullable(batter.position())
              .map(
                  s -> {
                    String[] split = s.split("/");
                    return split.length > 0 ? split[split.length - 1] : "";
                  })
              .orElse("");
      updateBatter(batter, "lineups/home/" + position);
    }

    for (int order = 1; order < 10; order += 1) {
      updateBatter(awayLineup.get(order - 1), "lineups/away/" + order);
    }

    for (Player batter : awayLineup) {
      String position =
          Optional.ofNullable(batter.position())
              .map(
                  s -> {
                    String[] split = s.split("/");
                    return split.length > 0 ? split[split.length - 1] : "";
                  })
              .orElse("");
      updateBatter(batter, "lineups/away/" + position);
    }

    updatePitcher(LineupTools.getHomePitcher(play), "lineups/home/pitcher");
    updatePitcher(LineupTools.getAwayPitcher(play), "lineups/away/pitcher");
  }
}
