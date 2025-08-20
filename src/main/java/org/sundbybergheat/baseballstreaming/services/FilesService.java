package org.sundbybergheat.baseballstreaming.services;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.math.NumberUtils;
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
              try {
                filesClient.copyFileFromResource(
                    "/images/team_flags/%s.png".formatted(entry.getKey()),
                    "team_resources/flags/%s.png".formatted(entry.getKey()));
              } catch (IOException e) {
                LOG.debug("Unable to find club flag file for {}", entry.getKey(), e);
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

  private void copyFileByPriority(
      final String pattern, final List<String> fileNames, final String to) throws IOException {
    for (String fileName : fileNames) {
      if (fileName != null && fileName.length() > 0) {
        final String from = pattern.formatted(fileName);
        if (filesClient.fileExists(from)) {
          filesClient.copyFile(from, to);
          return;
        }
      }
    }
  }

  private void updateScoreBoard() throws IOException {

    copyFileByPriority(
        "team_resources/colors/%s.png",
        List.of(play.eventHome().orElse(""), "default_home"), "home_color.png");
    copyFileByPriority(
        "team_resources/colors/%s.png",
        List.of(play.eventAway().orElse(""), "default_away"), "away_color.png");
    copyFileByPriority(
        "team_resources/flags/%s.png",
        List.of(play.eventHomeId().orElse(""), play.eventHome().orElse(""), "default"),
        "home_flag.png");
    copyFileByPriority(
        "team_resources/flags/%s.png",
        List.of(play.eventAwayId().orElse(""), play.eventAway().orElse(""), "default"),
        "away_flag.png");

    filesClient.writeStringToFile("home_team.txt", play.eventHome().orElse("HOME"));
    filesClient.writeStringToFile("away_team.txt", play.eventAway().orElse("AWAY"));

    final Map<String, String> lineScore = new HashMap<>(6);

    play.lineScore()
        .ifPresent(
            ls -> {
              ls.homeTotals()
                  .ifPresent(
                      ht -> {
                        lineScore.put("homeScore", ht.runs().orElse("0"));
                        lineScore.put("homeHits", ht.hits().orElse("0"));
                        lineScore.put("homeErrors", ht.errors().orElse("0"));
                      });
              ls.awayTotals()
                  .ifPresent(
                      at -> {
                        lineScore.put("awayScore", at.runs().orElse("0"));
                        lineScore.put("awayHits", at.hits().orElse("0"));
                        lineScore.put("awayErrors", at.errors().orElse("0"));
                      });
            });

    filesClient.writeStringToFile("home_score.txt", lineScore.getOrDefault("homeScore", "0"));
    filesClient.writeStringToFile("away_score.txt", lineScore.getOrDefault("awayScore", "0"));
    filesClient.writeStringToFile("home_hits.txt", lineScore.getOrDefault("homeHits", "0"));
    filesClient.writeStringToFile("away_hits.txt", lineScore.getOrDefault("awayHits", "0"));
    filesClient.writeStringToFile("home_errors.txt", lineScore.getOrDefault("homeErrors", "0"));
    filesClient.writeStringToFile("away_errors.txt", lineScore.getOrDefault("awayErrors", "0"));
    updateBases();
    updateOuts();
    String currentInning = play.situation().map(s -> s.currentInning().orElse("")).orElse("");
    if (!"FINAL".equals(currentInning)) {
      filesClient.writeStringToFile(
          "inning_half.txt", currentInning.startsWith("TOP") ? "\u25B2" : "\u25BC");
      List<String> innningParts = List.of(currentInning.split(" "));
      String inning = innningParts.size() > 1 ? innningParts.get(1) : "";
      filesClient.writeStringToFile("inning.txt", inning);
    }
    filesClient.writeStringToFile("inning_text.txt", currentInning);

    final Map<String, String> situation = new HashMap<>(2);

    play.situation()
        .ifPresent(
            s -> {
              situation.put("balls", s.balls().orElse("0"));
              situation.put("strikes", s.strikes().orElse("0"));
            });

    filesClient.writeStringToFile(
        "count.txt",
        String.format(
            "%s - %s",
            situation.getOrDefault("balls", "0"), situation.getOrDefault("strikes", "0")));
    filesClient.writeStringToFile("balls.txt", situation.getOrDefault("balls", "0"));
    filesClient.writeStringToFile("strikes.txt", situation.getOrDefault("strikes", "0"));
  }

  private void updateBases() throws IOException {
    final Map<String, Character> runners = new HashMap<>(3);
    String basesTarget = "bases.png";
    play.situation()
        .ifPresent(
            s -> {
              runners.put("first", "0".equals(s.runnerOnFirst().orElse("0")) ? 'o' : 'x');
              runners.put("second", "0".equals(s.runnerOnSecond().orElse("0")) ? 'o' : 'x');
              runners.put("third", "0".equals(s.runnerOnThird().orElse("0")) ? 'o' : 'x');
            });
    String basesSource =
        String.format(
            "bases/%c%c%c.png",
            runners.getOrDefault("third", 'o'),
            runners.getOrDefault("second", 'o'),
            runners.getOrDefault("first", 'o'));
    filesClient.copyFile(basesSource, basesTarget);
  }

  private void updateOuts() throws IOException {
    String outs = play.situation().flatMap(s -> s.outs()).orElse("0");
    filesClient.writeStringToFile("outs.txt", outs);
    String outsTarget = "outs.png";
    if ("1".equals(outs)) {
      filesClient.copyFile("outs/1.png", outsTarget);
    } else if ("2".equals(outs)) {
      filesClient.copyFile("outs/2.png", outsTarget);
    } else {
      filesClient.copyFile("outs/0.png", outsTarget);
    }
  }

  private void updateCurrentPitcher() throws IOException {
    String currentInning = play.situation().map(s -> s.currentInning().orElse("")).orElse("");

    Optional<Player> pitcher =
        play.boxScore().entrySet().stream()
            .filter(kv -> kv.getKey().startsWith(currentInning.startsWith("TOP") ? "29" : "19"))
            .sorted((a, b) -> Integer.parseInt(b.getKey()) - Integer.parseInt(a.getKey()))
            .findFirst()
            .map(kv -> kv.getValue());

    if (pitcher.isPresent()) {
      updatePitcher(pitcher.get(), "current_pitcher");
    } else {
      LOG.error("Failed to find current pitcher.");
    }
  }

  private void updatePitcher(final Player pitcher, final String subdir) throws IOException {
    filesClient.writeStringToFile(
        subdir + "/count.txt", String.format("P: %s", pitcher.pitches().orElse("0")));

    filesClient.writeStringToFile(subdir + "/firstname.txt", pitcher.firstName().orElse(""));
    filesClient.writeStringToFile(subdir + "/fullname.txt", pitcher.fullName().orElse(""));
    filesClient.writeStringToFile(subdir + "/lastname.txt", pitcher.lastName().orElse(""));

    // Update images
    updateImages(pitcher, subdir);

    int gameLength = play.innings().map(i -> NumberUtils.toInt(i, 9)).orElse(9);
    filesClient.writeStringToFile(
        subdir + "/pitching.txt", PitcherTools.pitcherNarrative(pitcher, gameLength));
    filesClient.writeStringToFile(
        subdir + "/pitching-title.txt", PitcherTools.pitcherNarrativeTitle(pitcher));

    // Initialize season stats with stats so far in this game
    Integer earnedRuns = pitcher.earnedRuns().map(er -> NumberUtils.toInt(er)).orElse(0);
    Integer outs =
        Long.valueOf(
                Math.round(3 * PitcherTools.inningsVal(pitcher.inningsPitched().orElse("0.0"))))
            .intValue();
    Integer walks = pitcher.pitcherWalks().map(bb -> NumberUtils.toInt(bb)).orElse(0);
    Integer strikeouts = pitcher.pitcherStrikeouts().map(so -> NumberUtils.toInt(so)).orElse(0);
    Double innings = 0.0;
    Double era = 0.0;

    if (pitcher.seasonStats().isPresent()) {
      PlayerSeasonStats stats = pitcher.seasonStats().get();
      // Add on season stats if available
      earnedRuns += stats.earnedRuns().map(er -> NumberUtils.toInt(er, 0)).orElse(0);
      outs += stats.outs().map(er -> NumberUtils.toInt(er, 0)).orElse(0);
      walks += stats.pitcherWalks().map(er -> NumberUtils.toInt(er, 0)).orElse(0);
      strikeouts += stats.pitcherStrikeouts().map(er -> NumberUtils.toInt(er, 0)).orElse(0);
      if (outs > 0) {
        innings = outs / 3.0;
        era = earnedRuns / (innings / gameLength);
      }
    }

    filesClient.writeStringToFile(
        subdir + "/era.txt", String.format("%.2f", era).replace(",", "."));
    filesClient.writeStringToFile(subdir + "/innings.txt", PitcherTools.inningsVal(innings));
    filesClient.writeStringToFile(subdir + "/strikeouts.txt", strikeouts.toString());
    filesClient.writeStringToFile(subdir + "/walks.txt", walks.toString());
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", "Stats for this season");
  }

  private void updateCurrentBatter() throws IOException {
    Optional<String> batterId = play.situation().map(s -> s.batterId()).orElse(Optional.empty());

    if (batterId.isPresent()) {
      Optional<Player> batter = BatterTools.aggregatedBoxScore(batterId.get(), play);
      if (batter.isPresent()) {
        updateBatter(batter.get(), "current_batter");
      } else {
        LOG.error("Failed to find data for current batter (id={}).", batterId.get());
      }
    } else {
      LOG.error("Failed to find current batter.");
    }
  }

  private void updateOnDeckBatter() throws IOException {
    Optional<String> maybeCurrentBatterKey =
        play.playData().stream()
            .sorted(
                (a, b) ->
                    Integer.parseInt(b.atBat().orElse("0"))
                        - Integer.parseInt(a.atBat().orElse("0")))
            .findFirst()
            .flatMap(pd -> pd.batter());
    Optional<String> onDeckBatterId = Optional.empty();
    if (maybeCurrentBatterKey.isPresent()) {
      String currentBatterKey = maybeCurrentBatterKey.get();
      int side = Integer.parseInt(currentBatterKey.substring(0, 2));
      int order = Integer.parseInt(currentBatterKey.substring(2, 3));
      int nextOrder = order == 9 ? 1 : order + 1;
      String prefix = String.format("%d%d", side, nextOrder);
      Optional<String> key =
          play.boxScore().keySet().stream()
              .filter(k -> k.startsWith(prefix))
              .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
              .findFirst();
      if (key.isPresent()) {
        onDeckBatterId = play.boxScore().get(key.get()).playerId();
      }
    }
    if (onDeckBatterId.isPresent()) {
      Optional<Player> batter = BatterTools.aggregatedBoxScore(onDeckBatterId.get(), play);
      if (batter.isPresent()) {
        updateBatter(batter.get(), "ondeck_batter");
      } else {
        LOG.error("Failed to find data for on-deck batter (id={}).", onDeckBatterId.get());
      }
    } else {
      LOG.error("Failed to find on-deck batter.");
    }
  }

  private void updateInHoleBatter() throws IOException {
    Optional<String> maybeCurrentBatterKey =
        play.playData().stream()
            .sorted(
                (a, b) ->
                    Integer.parseInt(b.atBat().orElse("0"))
                        - Integer.parseInt(a.atBat().orElse("0")))
            .findFirst()
            .flatMap(pd -> pd.batter());
    Optional<String> inHoleBatterId = Optional.empty();
    if (maybeCurrentBatterKey.isPresent()) {
      String currentBatterKey = maybeCurrentBatterKey.get();
      int side = Integer.parseInt(currentBatterKey.substring(0, 2));
      int order = Integer.parseInt(currentBatterKey.substring(2, 3));
      int nextNextOrder = order == 8 ? 1 : (order == 9 ? 2 : order + 2);
      String prefix = String.format("%d%d", side, nextNextOrder);
      Optional<String> key =
          play.boxScore().keySet().stream()
              .filter(k -> k.startsWith(prefix))
              .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
              .findFirst();
      if (key.isPresent()) {
        inHoleBatterId = play.boxScore().get(key.get()).playerId();
      }
    }
    if (inHoleBatterId.isPresent()) {
      Optional<Player> batter = BatterTools.aggregatedBoxScore(inHoleBatterId.get(), play);
      if (batter.isPresent()) {
        updateBatter(batter.get(), "inhole_batter");
      } else {
        LOG.error("Failed to find data for in-hole batter (id={}).", inHoleBatterId.get());
      }
    } else {
      LOG.error("Failed to find in-hole batter.");
    }
  }

  private void updateBatter(final Player batter, final String subdir) throws IOException {

    filesClient.writeStringToFile(subdir + "/firstname.txt", batter.firstName().orElse(""));
    filesClient.writeStringToFile(
        subdir + "/lastname.txt", batter.lastName().map(ln -> ln.toUpperCase()).orElse(""));
    filesClient.writeStringToFile(subdir + "/pos.txt", batter.position().orElse(""));
    filesClient.writeStringToFile(subdir + "/fullname.txt", batter.fullName().orElse(""));

    // Update images
    updateImages(batter, subdir);
    filesClient.writeStringToFile(
        subdir + "/batting.txt", BatterTools.batterNarrative(batter, play));
    filesClient.writeStringToFile(
        subdir + "/batting-title.txt", BatterTools.batterNarrativeTitle(batter));

    Integer hits = batter.hits().map(h -> NumberUtils.toInt(h)).orElse(0);
    Integer doubles = batter.doubles().map(d -> NumberUtils.toInt(d)).orElse(0);
    Integer triples = batter.triples().map(t -> NumberUtils.toInt(t)).orElse(0);
    Integer homeruns = batter.homeruns().map(hr -> NumberUtils.toInt(hr)).orElse(0);
    Integer singles = hits - doubles - triples - homeruns;
    Integer walks = batter.walks().map(bb -> NumberUtils.toInt(bb)).orElse(0);
    Integer hitByPitch = batter.hitByPitch().map(hbp -> NumberUtils.toInt(hbp)).orElse(0);
    Integer plateAppearances = batter.plateAppearances().map(pa -> NumberUtils.toInt(pa)).orElse(0);
    Integer atBats = batter.atBats().map(ab -> NumberUtils.toInt(ab)).orElse(0);

    if (batter.seasonStats().isPresent()) {
      final PlayerSeasonStats stats = batter.seasonStats().get();
      hits += stats.hits().map(h -> NumberUtils.toInt(h)).orElse(0);
      doubles += stats.doubles().map(d -> NumberUtils.toInt(d)).orElse(0);
      triples += stats.triples().map(t -> NumberUtils.toInt(t)).orElse(0);
      homeruns += stats.homeruns().map(hr -> NumberUtils.toInt(hr)).orElse(0);
      singles = hits - doubles - triples - homeruns;
      walks += stats.walks().map(bb -> NumberUtils.toInt(bb)).orElse(0);
      hitByPitch += stats.hitByPitch().map(hbp -> NumberUtils.toInt(hbp)).orElse(0);
      plateAppearances += stats.plateAppearances().map(pa -> NumberUtils.toInt(pa)).orElse(0);
      atBats += stats.atBats().map(ab -> NumberUtils.toInt(ab)).orElse(0);
    }

    Double battingAverage = atBats > 0 ? hits.doubleValue() / atBats.doubleValue() : 0.0;
    Double onBasePercentage =
        plateAppearances > 0 ? (hits + walks + hitByPitch) / plateAppearances.doubleValue() : 0.0;
    Double sluggingPercentage =
        atBats > 0
            ? (singles + doubles * 2.0 + triples * 3.0 + homeruns * 4.0) / atBats.doubleValue()
            : 0.0;

    String battingAverageString = "%.3f".formatted(battingAverage).replace(",", ".");
    String opsString = "%.3f".formatted(onBasePercentage + sluggingPercentage).replace(",", ".");
    filesClient.writeStringToFile(
        subdir + "/avg.txt",
        battingAverageString.startsWith("0")
            ? battingAverageString.substring(1)
            : battingAverageString);
    filesClient.writeStringToFile(
        subdir + "/ops.txt", opsString.startsWith("0") ? opsString.substring(1) : opsString);
    filesClient.writeStringToFile(subdir + "/hr.txt", homeruns.toString());
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", "Stats for this season");
  }

  private void updateImages(final Player player, final String subdir) throws IOException {
    Optional<String> teamFlagPath =
        player.teamId().map(tId -> String.format("team_resources/flags/%s.png", tId));
    Optional<String> clubFlagPath =
        player.teamCode().map(tc -> String.format("team_resources/flags/%s.png", tc));

    if (teamFlagPath.map(tfp -> filesClient.fileExists(tfp)).orElse(false)) {
      filesClient.copyFile(teamFlagPath.get(), subdir + "/flag.png");
    } else if (clubFlagPath.map(cfp -> filesClient.fileExists(cfp)).orElse(false)) {
      filesClient.copyFile(clubFlagPath.get(), subdir + "/flag.png");
    } else {
      filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
    }

    boolean imageCopied = false;
    if (player.teamId().isPresent() && player.playerId().isPresent()) {
      String playerImagePath =
          String.format(
              "team_resources/player_images/%s-%s.png",
              player.teamId().get(), player.playerId().get());

      // If player image doesn't exist locally but we have a URL (that is not default image) to
      // download from, then download it
      if (!filesClient.fileExists(playerImagePath)
          && player
              .imageUrl()
              .map(url -> !"https://static.wbsc.org/assets/images/default-player.jpg".equals(url))
              .orElse(false)) {
        filesClient.copyImageFromURL(URI.create(player.imageUrl().get()).toURL(), playerImagePath);
      }

      // If the player image exists, then copy it to the target dir
      if (filesClient.fileExists(playerImagePath)) {
        filesClient.copyFile(playerImagePath, subdir + "/image.png");
        imageCopied = true;
      }
    }
    // If player image hasn't been copied, then use default image
    if (!imageCopied) {
      filesClient.copyFile("team_resources/player_images/default.png", subdir + "/image.png");
    }

    String teamColorFile = "home_color.png";
    if (player.teamId().isPresent() && play.eventHomeId().isPresent()) {
      String teamId = player.teamId().get();
      String eventHomeId = play.eventHomeId().get();
      teamColorFile = teamId.equals(eventHomeId) ? "home_color.png" : "away_color.png";
    }
    filesClient.copyFile(teamColorFile, subdir + "/team_color.png");
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
          batter
              .position()
              .map(
                  s -> {
                    String[] split = s.split("/");
                    return split.length > 0 ? split[split.length - 1] : "";
                  })
              .orElse("");
      if (position.length() > 0) {
        updateBatter(batter, "lineups/home/" + position);
      }
    }

    for (int order = 1; order < 10; order += 1) {
      updateBatter(awayLineup.get(order - 1), "lineups/away/" + order);
    }

    for (Player batter : awayLineup) {
      String position =
          batter
              .position()
              .map(
                  s -> {
                    String[] split = s.split("/");
                    return split.length > 0 ? split[split.length - 1] : "";
                  })
              .orElse("");
      if (position.length() > 0) {
        updateBatter(batter, "lineups/away/" + position);
      }
    }

    updatePitcher(LineupTools.getHomePitcher(play), "lineups/home/pitcher");
    updatePitcher(LineupTools.getAwayPitcher(play), "lineups/away/pitcher");
  }
}
