package org.sundbybergheat.baseballstreaming.services;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.clients.FilesClient;
import org.sundbybergheat.baseballstreaming.clients.StatsClient;
import org.sundbybergheat.baseballstreaming.models.stats.AllStats;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStats;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;
import org.sundbybergheat.baseballstreaming.models.stats.StatsException;
import org.sundbybergheat.baseballstreaming.models.wbsc.BoxScore;
import org.sundbybergheat.baseballstreaming.models.wbsc.Play;

public class FilesService {
  private static final Logger LOG = LoggerFactory.getLogger(FilesService.class);

  private static final int MINIMUM_GAMES_BATTING_STATS = 2;
  private static final int MINIMUM_APPEARANCES_PITCHING_STATS = 3;

  private Play play = null;

  private final FilesClient filesClient;
  private final StatsClient statsClient;
  private final String seriesId;

  private Map<String, AllStats> stats = new HashMap<String, AllStats>();
  private String inHoleBatter = null;

  public FilesService(
      final FilesClient filesClient, final StatsClient statsClient, final String seriesId) {
    this.filesClient = filesClient;
    this.statsClient = statsClient;
    this.seriesId = seriesId;
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

  protected void updatePlay(final Play play) throws StatsException {
    this.play = play;
    updateState();
  }

  private void updateState() throws StatsException {
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
    filesClient.writeStringToFile(
        "home_score.txt", Integer.toString(play.lineScore().homeTotals().runs()));
    filesClient.writeStringToFile(
        "away_score.txt", Integer.toString(play.lineScore().awayTotals().runs()));
    filesClient.writeStringToFile(
        "home_hits.txt", Integer.toString(play.lineScore().homeTotals().hits()));
    filesClient.writeStringToFile(
        "away_hits.txt", Integer.toString(play.lineScore().awayTotals().hits()));
    filesClient.writeStringToFile(
        "home_errors.txt", Integer.toString(play.lineScore().homeTotals().errors()));
    filesClient.writeStringToFile(
        "away_errors.txt", Integer.toString(play.lineScore().awayTotals().errors()));
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
        String.format("%d - %d", play.situation().balls(), play.situation().strikes()));
  }

  private void updateBases() throws IOException {
    String basesTarget = "bases.png";
    char first = play.situation().runner1() > 0 ? 'x' : 'o';
    char second = play.situation().runner2() > 0 ? 'x' : 'o';
    char third = play.situation().runner3() > 0 ? 'x' : 'o';
    String basesSource = String.format("bases/%c%c%c.png", third, second, first);
    filesClient.copyFile(basesSource, basesTarget);
  }

  private void updateOuts() throws IOException {
    String outsTarget = "outs.png";
    if (play.situation().outs() == 1) {
      filesClient.copyFile("outs/1.png", outsTarget);
    } else if (play.situation().outs() == 2) {
      filesClient.copyFile("outs/2.png", outsTarget);
    } else {
      filesClient.copyFile("outs/0.png", outsTarget);
    }
  }

  private void updateCurrentPitcher() throws IOException, StatsException {
    BoxScore pitcher =
        play.boxScore().entrySet().stream()
            .filter(
                kv ->
                    kv.getValue().playerId().equals(play.situation().pitcherId())
                        && (kv.getKey().startsWith("19") || kv.getKey().startsWith("29")))
            .findFirst()
            .map(kv -> kv.getValue())
            .get();
    updatePitcher(pitcher, "current_pitcher");
  }

  private void updatePitcher(final BoxScore pitcher, final String subdir)
      throws IOException, StatsException {
    filesClient.writeStringToFile(
        subdir + "/count.txt", pitcher.pitches().map(p -> String.format("P: %d", p)).orElse(""));

    filesClient.writeStringToFile(subdir + "/firstname.txt", pitcher.firstName());
    filesClient.writeStringToFile(subdir + "/fullname.txt", pitcher.name());
    filesClient.writeStringToFile(subdir + "/lastname.txt", pitcher.lastName());
    if (!stats.containsKey(pitcher.playerId())) {
      stats.put(
          pitcher.playerId(),
          statsClient.getPlayerStats(
              pitcher.name(), pitcher.playerId(), pitcher.teamId(), seriesId));
    }

    AllStats allStats = stats.get(pitcher.playerId());

    // Update images
    if (allStats.seriesStats().containsKey(seriesId)) {
      updateImages(
          subdir, allStats.seriesStats().get(seriesId), pitcher.teamId(), pitcher.playerId());
    } else {
      filesClient.copyFile("team_resources/player_images/default.png", subdir + "/image.png");
      filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
    }

    SeriesStats selectedSeries = selectSeriesPitching(allStats, seriesId);

    if (selectedSeries == null || selectedSeries.pitching().isEmpty()) {
      LOG.warn("No stats for pitcher {} (id={})", pitcher.name(), pitcher.playerId());
      filesClient.writeStringToFile(subdir + "/era.txt", "-");
      filesClient.writeStringToFile(subdir + "/games.txt", "0");
      filesClient.writeStringToFile(subdir + "/hits.txt", "0");
      filesClient.writeStringToFile(subdir + "/hrs-allowed.txt", "0");
      filesClient.writeStringToFile(subdir + "/innings.txt", "0.0");
      filesClient.writeStringToFile(subdir + "/strikeouts.txt", "0");
      filesClient.writeStringToFile(subdir + "/walks.txt", "0");
      filesClient.writeStringToFile(subdir + "/wins-losses.txt", "0 - 0");
      return;
    }
    PitcherStats pitcherStats = selectedSeries.pitching().get();

    filesClient.writeStringToFile(subdir + "/era.txt", pitcherStats.era());
    filesClient.writeStringToFile(
        subdir + "/games.txt", Integer.toString(pitcherStats.appearances()));
    filesClient.writeStringToFile(
        subdir + "/hits.txt", Integer.toString(pitcherStats.hitsAllowed()));
    filesClient.writeStringToFile(
        subdir + "/hrs-allowed.txt", Integer.toString(pitcherStats.homerunsAllowed()));
    filesClient.writeStringToFile(subdir + "/innings.txt", pitcherStats.inningsPitched());
    filesClient.writeStringToFile(
        subdir + "/strikeouts.txt", Integer.toString(pitcherStats.strikeouts()));
    filesClient.writeStringToFile(
        subdir + "/walks.txt", Integer.toString(pitcherStats.walksAllowed()));
    filesClient.writeStringToFile(
        subdir + "/wins-losses.txt",
        String.format("%d - %d", pitcherStats.wins(), pitcherStats.losses()));
    String statsForSeries = "";
    if (selectedSeries.otherSeries()) {
      statsForSeries = String.format("Stats for %s", selectedSeries.seriesName());
    } else {
      statsForSeries =
          selectedSeries.id().equals(seriesId)
              ? "Stats for this season"
              : String.format(
                  "Stats for %s season",
                  selectedSeries.year().map(y -> y.toString()).orElse("other"));
    }
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", statsForSeries);

    filesClient.writeStringToFile(
        subdir + "/career/era.txt", allStats.careerPitching().map(s -> s.era()).orElse("-"));
    filesClient.writeStringToFile(
        subdir + "/career/games.txt",
        allStats.careerPitching().map(s -> Integer.toString(s.appearances())).orElse("0"));
    filesClient.writeStringToFile(
        subdir + "/career/hits.txt",
        allStats.careerPitching().map(s -> Integer.toString(s.hitsAllowed())).orElse("0"));
    filesClient.writeStringToFile(
        subdir + "/career/hrs-allowed.txt",
        allStats.careerPitching().map(s -> Integer.toString(s.homerunsAllowed())).orElse("0"));
    filesClient.writeStringToFile(
        subdir + "/career/innings.txt",
        allStats.careerPitching().map(s -> s.inningsPitched()).orElse("0.0"));
    filesClient.writeStringToFile(
        subdir + "/career/strikeouts.txt",
        allStats.careerPitching().map(s -> Integer.toString(s.strikeouts())).orElse("0"));
    filesClient.writeStringToFile(
        subdir + "/career/walks.txt",
        allStats.careerPitching().map(s -> Integer.toString(s.walksAllowed())).orElse("0"));
    filesClient.writeStringToFile(
        subdir + "/career/wins-losses.txt",
        allStats
            .careerPitching()
            .map(s -> String.format("%d - %d", s.wins(), s.losses()))
            .orElse("0 - 0"));
  }

  private void updateCurrentBatter() throws IOException, StatsException {
    String batterId = play.situation().batterId();
    BoxScore batter = BatterTools.aggregatedBoxScore(batterId, play);
    updateBatter(batter, "current_batter");
  }

  private void updateOnDeckBatter() throws IOException, StatsException {
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
    BoxScore batter = BatterTools.aggregatedBoxScore(play.boxScore().get(key).playerId(), play);
    updateBatter(batter, "ondeck_batter");
  }

  private void updateInHoleBatter() throws IOException, StatsException {
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
    BoxScore batter = BatterTools.aggregatedBoxScore(play.boxScore().get(key).playerId(), play);
    if (batter.playerId().equals(inHoleBatter)) {
      return;
    }
    inHoleBatter = batter.playerId();
    stats.put(
        batter.playerId(),
        statsClient.getPlayerStats(batter.name(), batter.playerId(), batter.teamId(), seriesId));

    updateBatter(batter, "inhole_batter");
  }

  private void updateBatter(final BoxScore batter, final String subdir)
      throws IOException, StatsException {

    filesClient.writeStringToFile(subdir + "/firstname.txt", batter.firstName());
    filesClient.writeStringToFile(subdir + "/lastname.txt", batter.lastName().toUpperCase());
    filesClient.writeStringToFile(subdir + "/pos.txt", batter.position().orElse(""));
    filesClient.writeStringToFile(subdir + "/fullname.txt", batter.name());

    if (!stats.containsKey(batter.playerId())) {
      stats.put(
          batter.playerId(),
          statsClient.getPlayerStats(batter.name(), batter.playerId(), batter.teamId(), seriesId));
    }

    AllStats allStats = stats.get(batter.playerId());

    // Update images
    if (allStats.seriesStats().containsKey(seriesId)) {
      updateImages(
          subdir, allStats.seriesStats().get(seriesId), batter.teamId(), batter.playerId());
    } else {
      filesClient.copyFile("team_resources/player_images/default.png", subdir + "/image.png");
      filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
    }

    SeriesStats selectedSeries = selectSeriesBatting(allStats, seriesId);
    filesClient.writeStringToFile(
        subdir + "/batting.txt", BatterTools.batterNarrative(batter, stats, seriesId, play));

    if (selectedSeries == null || selectedSeries.batting().isEmpty()) {
      LOG.warn("No stats for batter {} (id={})", batter.name(), batter.playerId());
      filesClient.writeStringToFile(subdir + "/avg.txt", "");
      filesClient.writeStringToFile(subdir + "/ops.txt", "");
      filesClient.writeStringToFile(subdir + "/hr.txt", "");
      filesClient.writeStringToFile(subdir + "/rbi.txt", "");
      filesClient.writeStringToFile(subdir + "/stats_for_series.txt", "");
      return;
    }

    BatterStats batterStats = selectedSeries.batting().get();

    filesClient.writeStringToFile(subdir + "/avg.txt", batterStats.battingAverage());
    filesClient.writeStringToFile(subdir + "/ops.txt", batterStats.onBasePercentagePlusSlugging());
    filesClient.writeStringToFile(subdir + "/hr.txt", Integer.toString(batterStats.homeruns()));
    filesClient.writeStringToFile(
        subdir + "/rbi.txt", Integer.toString(batterStats.runsBattedIn()));
    filesClient.writeStringToFile(
        subdir + "/batting.txt", BatterTools.batterNarrative(batter, selectedSeries, play));

    String statsForSeries = "";
    if (selectedSeries.otherSeries()) {
      statsForSeries = String.format("Stats for %s", selectedSeries.seriesName());
    } else {
      statsForSeries =
          selectedSeries.id().equals(seriesId)
              ? "Stats for this season"
              : String.format(
                  "Stats for %s season",
                  selectedSeries.year().map(y -> y.toString()).orElse("other"));
    }
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", statsForSeries);

    filesClient.writeStringToFile(
        subdir + "/career/games.txt",
        allStats.careerBatting().map(s -> Integer.toString(s.games())).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/avg.txt",
        allStats.careerBatting().map(s -> s.battingAverage()).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/ops.txt",
        allStats.careerBatting().map(s -> s.onBasePercentagePlusSlugging()).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/hr.txt",
        allStats.careerBatting().map(s -> Integer.toString(s.homeruns())).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/rbi.txt",
        allStats.careerBatting().map(s -> Integer.toString(s.runsBattedIn())).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/hits.txt",
        allStats.careerBatting().map(s -> Integer.toString(s.hits())).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/atbats.txt",
        allStats.careerBatting().map(s -> Integer.toString(s.atBats())).orElse(""));
    filesClient.writeStringToFile(
        subdir + "/career/sb.txt",
        allStats.careerBatting().map(s -> Integer.toString(s.stolenBases())).orElse(""));
  }

  private SeriesStats selectSeriesBatting(final AllStats stats, final String seriesId) {

    Matcher matcher = Pattern.compile("^.*(2[0-9]{3}).*$").matcher(seriesId);
    Map<String, SeriesStats> seriesStats = stats.seriesStats();
    Optional<Integer> thisYear =
        matcher.matches() ? Optional.of(Integer.parseInt(matcher.group(1))) : Optional.empty();

    // Select this season if stats are available
    if (seriesStats.containsKey(seriesId)
        && seriesStats
            .get(seriesId)
            .batting()
            .map(b -> b.games() > MINIMUM_GAMES_BATTING_STATS)
            .orElse(false)) {
      return seriesStats.get(seriesId);
    }

    // else try to find previous season stats
    Optional<SeriesStats> lastSeason =
        seriesStats.values().stream()
            .filter(s -> !s.otherSeries())
            .sorted((a, b) -> b.year().orElse(0) - a.year().orElse(0))
            .findFirst();
    if (lastSeason
        .map(s -> s.batting().map(b -> b.games() > MINIMUM_GAMES_BATTING_STATS).orElse(false))
        .orElse(false)) {
      return lastSeason.get();
    }

    // else try to find other series stats this year
    Optional<SeriesStats> otherSeries =
        seriesStats.values().stream()
            .filter(
                s ->
                    s.year().filter(y -> y.equals(thisYear.orElse(0))).isPresent()
                        && s.otherSeries())
            .findFirst();
    if (otherSeries
        .map(s -> s.batting().map(b -> b.games() > MINIMUM_GAMES_BATTING_STATS).orElse(false))
        .orElse(false)) {
      return otherSeries.get();
    }

    // else try to find other series previous season stats
    Optional<SeriesStats> otherSeriesLastSeason =
        seriesStats.values().stream()
            .filter(s -> s.otherSeries())
            .sorted((a, b) -> b.year().orElse(0) - a.year().orElse(0))
            .findFirst();
    if (otherSeriesLastSeason
        .map(s -> s.batting().map(b -> b.games() > MINIMUM_GAMES_BATTING_STATS).orElse(false))
        .orElse(false)) {
      return otherSeriesLastSeason.get();
    }

    // else pick any stats
    return seriesStats.values().stream()
        .filter(s -> s.batting().map(b -> b.games() > MINIMUM_GAMES_BATTING_STATS).orElse(false))
        .findFirst()
        .orElse(null);
  }

  private SeriesStats selectSeriesPitching(final AllStats stats, final String seriesId) {

    Matcher matcher = Pattern.compile("^.*(2[0-9]{3}).*$").matcher(seriesId);
    Map<String, SeriesStats> seriesStats = stats.seriesStats();
    Optional<Integer> thisYear =
        matcher.matches() ? Optional.of(Integer.parseInt(matcher.group(1))) : Optional.empty();

    // Select this season if stats are available
    if (seriesStats.containsKey(seriesId)
        && seriesStats
            .get(seriesId)
            .pitching()
            .map(p -> p.appearances() > MINIMUM_APPEARANCES_PITCHING_STATS)
            .orElse(false)) {
      return seriesStats.get(seriesId);
    }

    // else try to find previous season stats
    Optional<SeriesStats> lastSeason =
        seriesStats.values().stream()
            .filter(s -> !s.otherSeries())
            .sorted((a, b) -> b.year().orElse(0) - a.year().orElse(0))
            .findFirst();
    if (lastSeason
        .map(
            s ->
                s.pitching()
                    .map(p -> p.appearances() > MINIMUM_APPEARANCES_PITCHING_STATS)
                    .orElse(false))
        .orElse(false)) {
      return lastSeason.get();
    }

    // else try to find other series stats this year
    Optional<SeriesStats> otherSeries =
        seriesStats.values().stream()
            .filter(
                s ->
                    s.year().filter(y -> y.equals(thisYear.orElse(0))).isPresent()
                        && s.otherSeries())
            .findFirst();
    if (otherSeries
        .map(
            s ->
                s.pitching()
                    .map(p -> p.appearances() > MINIMUM_APPEARANCES_PITCHING_STATS)
                    .orElse(false))
        .orElse(false)) {
      return otherSeries.get();
    }

    // else try to find other series previous season stats
    Optional<SeriesStats> otherSeriesLastSeason =
        seriesStats.values().stream()
            .filter(s -> s.otherSeries())
            .sorted((a, b) -> b.year().orElse(0) - a.year().orElse(0))
            .findFirst();
    if (otherSeriesLastSeason
        .map(
            s ->
                s.pitching()
                    .map(p -> p.appearances() > MINIMUM_APPEARANCES_PITCHING_STATS)
                    .orElse(false))
        .orElse(false)) {
      return otherSeriesLastSeason.get();
    }

    // else pick stats with most appearances
    return seriesStats.values().stream()
        .sorted(
            (s1, s2) ->
                s2.pitching().map(p -> p.appearances()).orElse(0)
                    - s1.pitching().map(p -> p.appearances()).orElse(0))
        .findFirst()
        .orElse(null);
  }

  private void updateImages(
      final String subdir,
      final SeriesStats selectedSeries,
      final String teamId,
      final String playerId)
      throws IOException {
    String teamFlagPath = String.format("team_resources/flags/%s.png", teamId);
    if (!filesClient.fileExists(teamFlagPath)) {
      if (selectedSeries.teamFlagUrl().isEmpty()
          || selectedSeries.teamFlagUrl().get().isEmpty()
          || selectedSeries.teamFlagUrl().get().toLowerCase().endsWith(".svg")) {
        filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
      } else {
        filesClient.copyImageFromURL(new URL(selectedSeries.teamFlagUrl().get()), teamFlagPath);
        if (filesClient.fileExists(teamFlagPath)) {
          filesClient.copyFile(teamFlagPath, subdir + "/flag.png");
        } else {
          filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
        }
      }
    } else {
      filesClient.copyFile(teamFlagPath, subdir + "/flag.png");
    }

    String playerImagePath =
        String.format("team_resources/player_images/%s-%s.png", teamId, playerId);
    if (!filesClient.fileExists(playerImagePath)) {
      if (selectedSeries.playerImageUrl().isEmpty()
          || selectedSeries.playerImageUrl().get().isEmpty()
          || selectedSeries.playerImageUrl().get().toLowerCase().endsWith(".svg")) {
        filesClient.copyFile("team_resources/player_images/default.png", subdir + "/image.png");
      } else {
        filesClient.copyImageFromURL(
            new URL(selectedSeries.playerImageUrl().get()), playerImagePath);
        if (filesClient.fileExists(playerImagePath)) {
          filesClient.copyFile(playerImagePath, subdir + "/image.png");
        } else {
          filesClient.copyFile("team_resources/player_images/default.png", subdir + "/image.png");
        }
      }
    } else {
      filesClient.copyFile(playerImagePath, subdir + "/image.png");
    }

    filesClient.copyFile(
        teamId.equals(play.eventHomeId()) ? "home_color.png" : "away_color.png",
        subdir + "/team_color.png");
  }

  private void updateLineups() throws IOException, StatsException {
    List<BoxScore> homeLineup = LineupTools.getHomeLineup(play);
    List<BoxScore> awayLineup = LineupTools.getAwayLineup(play);

    if (homeLineup.size() != 9 || awayLineup.size() != 9) {
      throw new StatsException(
          String.format(
              "Invalid lineups. Expected 9 batters each but got %d home batters and %d away batters.",
              homeLineup.size(), awayLineup.size()));
    }

    for (int order = 1; order < 10; order += 1) {
      updateBatter(homeLineup.get(order - 1), "lineups/home/" + order);
    }

    for (BoxScore batter : homeLineup) {
      String position =
          batter
              .position()
              .map(
                  s -> {
                    String[] split = s.split("/");
                    return split.length > 0 ? split[split.length - 1] : "";
                  })
              .orElse("");
      if (isValidPosition(position)) {
        updateBatter(batter, "lineups/home/" + position);
      }
    }

    for (int order = 1; order < 10; order += 1) {
      updateBatter(awayLineup.get(order - 1), "lineups/away/" + order);
    }

    for (BoxScore batter : awayLineup) {
      String position =
          batter
              .position()
              .map(
                  s -> {
                    String[] split = s.split("/");
                    return split.length > 0 ? split[split.length - 1] : "";
                  })
              .orElse("");
      if (isValidPosition(position)) {
        updateBatter(batter, "lineups/away/" + position);
      }
    }

    updatePitcher(LineupTools.getHomePitcher(play), "lineups/home/pitcher");
    updatePitcher(LineupTools.getAwayPitcher(play), "lineups/away/pitcher");
  }

  private boolean isValidPosition(final String position) {
    return List.of("P", "C", "1B", "2B", "3B", "SS", "LF", "CF", "RF").contains(position);
  }
}
