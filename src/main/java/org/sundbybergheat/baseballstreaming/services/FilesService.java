package org.sundbybergheat.baseballstreaming.services;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    String prefix = play.situation().currentInning().startsWith("TOP") ? "29" : "19";
    String key =
        play.boxScore().keySet().stream()
            .filter(k -> k.startsWith(prefix))
            .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
            .findFirst()
            .get();
    BoxScore pitcher = play.boxScore().get(key);

    updatePitcher(pitcher, "current_pitcher");
  }

  private void updatePitcher(final BoxScore pitcher, final String subdir)
      throws IOException, StatsException {
    filesClient.writeStringToFile(
        subdir + "/count.txt", pitcher.pitches().map(p -> p.toString()).orElse(""));

    if (!stats.containsKey(pitcher.playerId())) {
      stats.put(
          pitcher.playerId(),
          statsClient.getPlayerStats(
              pitcher.name(), pitcher.playerId(), pitcher.teamId(), seriesId));
    }

    SeriesStats seriesStats = stats.get(pitcher.playerId()).seriesStats().get(seriesId);
    Optional<PitcherStats> maybePitcherStats = seriesStats.pitching();
    if (maybePitcherStats.isEmpty()) {
      LOG.warn("No pitching stats found for {}, setting default values.", pitcher.name());
      filesClient.writeStringToFile(subdir + "/era.txt", "-");
      filesClient.writeStringToFile(subdir + "/firstname.txt", pitcher.firstName());
      filesClient.writeStringToFile(subdir + "/fullname.txt", pitcher.name());
      filesClient.writeStringToFile(subdir + "/games.txt", "0");
      filesClient.writeStringToFile(subdir + "/hits.txt", "0");
      filesClient.writeStringToFile(subdir + "/hrs-allowed.txt", "0");
      filesClient.writeStringToFile(subdir + "/innings.txt", "0.0");
      filesClient.writeStringToFile(subdir + "/lastname.txt", pitcher.lastName());
      filesClient.writeStringToFile(subdir + "/strikeouts.txt", "0");
      filesClient.writeStringToFile(subdir + "/walks.txt", "0");
      filesClient.writeStringToFile(subdir + "/wins-losses.txt", "0 - 0");

      updateImages(subdir + "", seriesStats, pitcher.teamId(), pitcher.playerId());
      return;
    }
    PitcherStats pitcherStats = maybePitcherStats.get();

    filesClient.writeStringToFile(subdir + "/era.txt", pitcherStats.era());
    filesClient.writeStringToFile(subdir + "/firstname.txt", pitcher.firstName());
    filesClient.writeStringToFile(subdir + "/fullname.txt", pitcher.name());
    filesClient.writeStringToFile(
        subdir + "/games.txt", Integer.toString(pitcherStats.appearances()));
    filesClient.writeStringToFile(
        subdir + "/hits.txt", Integer.toString(pitcherStats.hitsAllowed()));
    filesClient.writeStringToFile(
        subdir + "/hrs-allowed.txt", Integer.toString(pitcherStats.homerunsAllowed()));
    filesClient.writeStringToFile(subdir + "/innings.txt", pitcherStats.inningsPitched());
    filesClient.writeStringToFile(subdir + "/lastname.txt", pitcher.lastName());
    filesClient.writeStringToFile(
        subdir + "/strikeouts.txt", Integer.toString(pitcherStats.strikeouts()));
    filesClient.writeStringToFile(
        subdir + "/walks.txt", Integer.toString(pitcherStats.walksAllowed()));
    filesClient.writeStringToFile(
        subdir + "/wins-losses.txt",
        String.format("%d - %d", pitcherStats.wins(), pitcherStats.losses()));

    updateImages(subdir + "", seriesStats, pitcher.teamId(), pitcher.playerId());
  }

  private void updateCurrentBatter() throws IOException, StatsException {
    String key =
        play.playData().stream()
            .sorted((a, b) -> Integer.parseInt(b.atBat()) - Integer.parseInt(a.atBat()))
            .findFirst()
            .get()
            .batter();
    BoxScore batter = BatterTools.aggregatedBoxScore(play.boxScore().get(key).playerId(), play);
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
    filesClient.writeStringToFile(subdir + "/lastname.txt", batter.lastName());
    filesClient.writeStringToFile(subdir + "/pos.txt", batter.position().orElse(""));
    filesClient.writeStringToFile(subdir + "/fullname.txt", batter.name());

    if (!stats.containsKey(batter.playerId())) {
      stats.put(
          batter.playerId(),
          statsClient.getPlayerStats(batter.name(), batter.playerId(), batter.teamId(), seriesId));
    }
    Map<String, SeriesStats> seriesStats = stats.get(batter.playerId()).seriesStats();
    SeriesStats selectedSeries = selectSeries(seriesStats, seriesId);

    if (selectedSeries == null) {
      LOG.warn("No stats for batter {} (id={})", batter.name(), batter.playerId());
      filesClient.writeStringToFile(subdir + "/avg.txt", "");
      filesClient.writeStringToFile(subdir + "/ops.txt", "");
      filesClient.writeStringToFile(subdir + "/hr.txt", "");
      filesClient.writeStringToFile(subdir + "/rbi.txt", "");
      filesClient.writeStringToFile(
          subdir + "/batting.txt", BatterTools.batterNarrative(batter, null, play));
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

    int thisYear = Integer.parseInt(seriesId.split("-")[0]);

    String statsForSeries = null;
    if (selectedSeries.otherSeries()) {
      statsForSeries = selectedSeries.seriesName();
    } else {
      statsForSeries =
          selectedSeries.year() == thisYear
              ? "This Season"
              : String.format("%d Season", selectedSeries.year());
    }
    filesClient.writeStringToFile(subdir + "/stats_for_series.txt", statsForSeries);

    updateImages(subdir, selectedSeries, batter.teamId(), batter.playerId());
  }

  private SeriesStats selectSeries(Map<String, SeriesStats> seriesStats, final String seriesId) {

    int thisYear = Integer.parseInt(seriesId.split("-")[0]);

    // Select this season if stats are available
    if (seriesStats.containsKey(seriesId)
        && seriesStats.get(seriesId).batting().map(s -> s.games() > 0).orElse(false)) {
      return seriesStats.get(seriesId);
    }

    // else try to find previous season stats
    Optional<SeriesStats> lastSeason =
        seriesStats.values().stream()
            .filter(s -> !s.otherSeries())
            .sorted((a, b) -> b.year() - a.year())
            .findFirst();
    if (lastSeason.map(s -> s.batting().map(b -> b.games() > 0).orElse(false)).orElse(false)) {
      return lastSeason.get();
    }

    // else try to find other series stats
    Optional<SeriesStats> otherSeries =
        seriesStats.values().stream()
            .filter(s -> s.year() == thisYear && s.otherSeries())
            .findFirst();
    if (otherSeries.map(s -> s.batting().map(b -> b.games() > 0).orElse(false)).orElse(false)) {
      return otherSeries.get();
    }

    // else try to find other series previous season stats
    Optional<SeriesStats> otherSeriesLastSeason =
        seriesStats.values().stream()
            .filter(s -> s.otherSeries())
            .sorted((a, b) -> b.year() - a.year())
            .findFirst();
    if (otherSeriesLastSeason
        .map(s -> s.batting().map(b -> b.games() > 0).orElse(false))
        .orElse(false)) {
      return otherSeriesLastSeason.get();
    }

    // else pick any stats
    return seriesStats.values().stream()
        .filter(s -> s.batting().map(b -> b.games() > 0).orElse(false))
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
      if (selectedSeries.teamFlagUrl().isEmpty()) {
        filesClient.copyFile("team_resources/flags/default.png", subdir + "/flag.png");
      } else {
        filesClient.copyFileFromURL(new URL(selectedSeries.teamFlagUrl().get()), teamFlagPath);
        filesClient.copyFile(teamFlagPath, subdir + "/flag.png");
      }
    } else {
      filesClient.copyFile(teamFlagPath, subdir + "/flag.png");
    }

    String playerImagePath =
        String.format("team_resources/player_images/%s-%s.png", teamId, playerId);
    if (!filesClient.fileExists(playerImagePath)) {
      if (selectedSeries.playerImageUrl().isEmpty()) {
        filesClient.copyFile("team_resources/player_images/default.png", subdir + "/image.png");
      } else {
        filesClient.copyFileFromURL(
            new URL(selectedSeries.playerImageUrl().get()), playerImagePath);
        filesClient.copyFile(playerImagePath, subdir + "/image.png");
      }
    } else {
      filesClient.copyFile(playerImagePath, subdir + "/image.png");
    }
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

    for (int order = 1; order < 10; order += 1) {
      updateBatter(awayLineup.get(order - 1), "lineups/away/" + order);
    }

    updatePitcher(LineupTools.getHomePitcher(play), "lineups/home/pitcher");
    updatePitcher(LineupTools.getAwayPitcher(play), "lineups/away/pitcher");
  }
}
