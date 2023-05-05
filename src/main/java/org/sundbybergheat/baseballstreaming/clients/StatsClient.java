package org.sundbybergheat.baseballstreaming.clients;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.models.stats.AllStats;
import org.sundbybergheat.baseballstreaming.models.stats.AllStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStats;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.Player;
import org.sundbybergheat.baseballstreaming.models.stats.PlayerImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesId;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesIdImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.StatsException;

public class StatsClient {
  private static final Logger LOG = LoggerFactory.getLogger(StatsClient.class);

  private static final String PLAYER_STATS_URL = "%s/en/events/%s/teams/%s/players/%s";
  private static final String TEAM_ROSTER_URL = "%s/en/events/%s/teams/%s";

  private static final String BATTING_STATS_HEADER = "BATTING STATS";
  private static final String PITCHING_STATS_HEADER = "PITCHING STATS";
  private static final String ROSTER_HEADER = "ROSTER";

  private final OkHttpClient client;
  private final String baseUrl;

  public StatsClient(final OkHttpClient client, final String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  public AllStats getPlayerStats(
      final String playerName,
      final String playerId,
      final String teamId,
      final String seriesId,
      final boolean onlyUseThisSeriesStats)
      throws StatsException, IOException {
    Map<String, SeriesStats> seriesStats = new HashMap<String, SeriesStats>();
    String thisUri = String.format(PLAYER_STATS_URL, baseUrl, seriesId, teamId, playerId);

    SeriesId thisSeriesId = parseSeriesId(seriesId);
    Optional<Integer> thisYear = thisSeriesId.year();

    LOG.info("Fetching stats for {} (id={}) in {}", playerName, playerId, seriesId);
    Document thisDoc = getHtmlDoc(thisUri);
    seriesStats.put(
        seriesId,
        parsePlayerSeriesStats(thisDoc, playerId, teamId, thisUri)
            .id(seriesId)
            .seriesName(seriesId)
            .year(thisYear)
            .build());

    Optional<Element> table = getTableForHeading("Other events", thisDoc);
    if (!onlyUseThisSeriesStats && table.isPresent()) {
      Elements rows = table.get().getElementsByTag("tbody").first().getElementsByTag("tr");

      for (Element element : rows) {
        Optional<Element> link = element.getElementsByTag("a").stream().findFirst();
        if (link.isPresent()) {
          String otherName = link.get().text();
          String uri = link.get().absUrl("href");
          String path = new URL(uri).getPath();

          Pattern pathPattern1 =
              Pattern.compile("^/+([^/]+)/events/([^/]+)/teams/([^/]+)/players/([^/]+)$");
          Pattern pathPattern2 =
              Pattern.compile("^/+([^/]+)/([^/]+)/teams/([^/]+)/players/([^/]+)$");
          Matcher pathMatcher1 = pathPattern1.matcher(path);
          Matcher pathMatcher2 = pathPattern2.matcher(path);

          String lang;
          SeriesId otherSeriesId;
          String otherTeamId;
          String otherPlayerId;

          if (pathMatcher1.matches()) {
            lang = pathMatcher1.group(1);
            otherSeriesId = parseSeriesId(pathMatcher1.group(2));
            otherTeamId = pathMatcher1.group(3);
            otherPlayerId = pathMatcher1.group(4);
          } else if (pathMatcher2.matches()) {
            lang = "en";
            otherSeriesId = parseSeriesId(pathMatcher2.group(2));
            otherTeamId = pathMatcher2.group(3);
            otherPlayerId = pathMatcher2.group(4);
          } else {
            LOG.warn(
                "Could not extract info from path {} (unrecognized pattern). Skipping stats for {}",
                path,
                otherName);
            continue;
          }

          final Optional<Integer> otherYear = otherSeriesId.year();
          final boolean sameSeries =
              thisSeriesId.prefix().isPresent()
                  && thisSeriesId.postfix().isPresent()
                  && thisSeriesId.prefix().equals(otherSeriesId.prefix())
                  && thisSeriesId.postfix().equals(otherSeriesId.postfix());

          LOG.info("Fetching stats for {} (id={}) in {}", playerName, otherPlayerId, otherName);

          String patchedUri = uri;

          if (!"en".equals(lang)) {
            String patchedPath =
                String.format(
                    "/en/events/%s/teams/%s/players/%s",
                    otherSeriesId.id(), otherTeamId, otherPlayerId);
            patchedUri = uri.replace(path, patchedPath);
          }

          Document doc = getHtmlDoc(patchedUri);

          seriesStats.put(
              otherSeriesId.id(),
              parsePlayerSeriesStats(doc, otherPlayerId, otherTeamId, patchedUri)
                  .id(otherSeriesId.id())
                  .seriesName(otherName)
                  .year(otherYear)
                  .otherSeries(!sameSeries)
                  .build());
        }
      }
    }

    return AllStatsImpl.builder()
        .seriesStats(seriesStats)
        .careerBatting(CareerStatsTools.getCareerBattingStats(seriesStats.values()))
        .careerPitching(CareerStatsTools.getCareerPitchingStats(seriesStats.values()))
        .build();
  }

  public List<Player> getRoster(final String seriesId, final String teamId)
      throws StatsException, IOException {
    String uri = String.format(TEAM_ROSTER_URL, baseUrl, seriesId, teamId);

    LOG.info("Fetching roster for {} in {}", teamId, seriesId);
    Document doc = getHtmlDoc(uri);
    Element table =
        getTableForHeading(ROSTER_HEADER, doc)
            .orElseThrow(() -> new StatsException(String.format("Cannot find roster @ %s", uri)));

    List<Player> roster =
        table.getElementsByTag("tbody").first().getElementsByTag("tr").stream()
            .map(
                e -> {
                  Elements playerInfo = e.getElementsByTag("td");
                  int number = Integer.parseInt(playerInfo.get(0).text());
                  Element playerNameLink = playerInfo.get(1).getElementsByTag("a").first();
                  String[] split = playerNameLink.attr("href").split("/");
                  int playerId = Integer.parseInt(split[split.length - 1]);
                  String playerName = playerNameLink.text();
                  return PlayerImpl.builder().id(playerId).number(number).name(playerName).build();
                })
            .collect(Collectors.toList());
    return roster;
  }

  private SeriesId parseSeriesId(final String seriesId) {

    SeriesIdImpl.Builder builder = SeriesIdImpl.builder().id(seriesId);
    final Pattern pattern = Pattern.compile("^(.*)(2[0-9]{3})(.*)$");
    final Matcher matcher = pattern.matcher(seriesId);

    if (matcher.matches()) {
      builder
          .prefix(matcher.group(1))
          .year(Integer.parseInt(matcher.group(2)))
          .postfix(matcher.group(3));
    }
    return builder.build();
  }

  private Optional<Element> getTableForHeading(final String heading, final Document doc) {
    return doc.getElementsByAttributeValue("class", "box-container").stream()
        .filter(
            e ->
                !e.getElementsByTag("h3").isEmpty()
                    && heading.equalsIgnoreCase(e.getElementsByTag("h3").get(0).text()))
        .map(e -> e.getElementsByTag("table"))
        .findFirst()
        .map(t -> t.first());
  }

  private SeriesStatsImpl.Builder parsePlayerSeriesStats(
      final Document doc, final String playerId, final String teamId, final String sourceUri) {
    Optional<BatterStats> batterStats = parsePlayerBatterStats(doc, playerId, teamId);
    Optional<PitcherStats> pitcherStats = parsePlayerPitcherStats(doc, playerId, teamId);
    if (batterStats.isEmpty() && pitcherStats.isEmpty()) {
      LOG.warn(
          "Found neither batting nor pitching stats for player {} @ {}. "
              + "Either it is the first game for the player in the series or something has changed on the stats page. "
              + "Are we searching for the correct sections ('{}' for batting stats and '{}' for pitching stats)?",
          playerId,
          sourceUri,
          BATTING_STATS_HEADER,
          PITCHING_STATS_HEADER);
    }
    return SeriesStatsImpl.builder()
        .teamFlagUrl(parseTeamFlagUrl(doc))
        .playerImageUrl(parsePlayerImageUrl(doc))
        .batting(batterStats)
        .pitching(pitcherStats);
  }

  private Optional<String> parseTeamFlagUrl(final Document doc) {
    Optional<String> findFirst =
        doc.getElementsByClass("flag-icon").stream()
            .filter(e -> e.tag().equals(Tag.valueOf("img")))
            .map(e -> e.attr("src"))
            .findFirst();
    return findFirst;
  }

  private Optional<String> parsePlayerImageUrl(final Document doc) {
    return doc.getElementsByClass("player-image").stream().map(e -> e.attr("src")).findFirst();
  }

  private Optional<BatterStats> parsePlayerBatterStats(
      final Document doc, final String playerId, final String teamId) {

    Optional<Element> table = getTableForHeading(BATTING_STATS_HEADER, doc);

    if (table.isEmpty()) {
      return Optional.empty();
    }

    int games = table.get().getElementsByTag("tbody").first().getElementsByTag("tr").size();

    Optional<Elements> row =
        table.map(
            t ->
                t.getElementsByTag("tfoot")
                    .first()
                    .getElementsByTag("tr")
                    .first()
                    .getElementsByTag("th"));

    List<String> totals = List.of(row.get().text().split(" "));

    BatterStatsImpl.Builder builder =
        BatterStatsImpl.builder()
            .playerId(playerId)
            .teamId(teamId)
            .games(games)
            .atBats(Integer.parseInt(totals.get(2)))
            .runs(Integer.parseInt(totals.get(3)))
            .hits(Integer.parseInt(totals.get(4)))
            .doubles(Integer.parseInt(totals.get(5)))
            .triples(Integer.parseInt(totals.get(6)))
            .homeruns(Integer.parseInt(totals.get(7)))
            .runsBattedIn(Integer.parseInt(totals.get(8)))
            .totalBases(Integer.parseInt(totals.get(9)))
            .battingAverage(totals.get(10))
            .slugging(totals.get(11))
            .onBasePercentage(totals.get(12))
            .onBasePercentagePlusSlugging(totals.get(13))
            .walks(Integer.parseInt(totals.get(14)))
            .hitByPitch(Integer.parseInt(totals.get(15)))
            .strikeouts(Integer.parseInt(totals.get(16)))
            .groundoutDoublePlay(Integer.parseInt(totals.get(17)))
            .sacrificeFlies(Integer.parseInt(totals.get(18)))
            .sacrificeHits(Integer.parseInt(totals.get(19)))
            .stolenBases(Integer.parseInt(totals.get(20)))
            .caughtStealing(Integer.parseInt(totals.get(21)));

    return Optional.of(builder.build());
  }

  private Optional<PitcherStats> parsePlayerPitcherStats(
      final Document doc, final String playerId, final String teamId) {

    Optional<Element> table = getTableForHeading(PITCHING_STATS_HEADER, doc);

    if (table.isEmpty()) {
      return Optional.empty();
    }

    int appearances = table.get().getElementsByTag("tbody").first().getElementsByTag("tr").size();

    Optional<Elements> row =
        table.map(
            t ->
                t.getElementsByTag("tfoot")
                    .first()
                    .getElementsByTag("tr")
                    .first()
                    .getElementsByTag("th"));

    List<String> totals = List.of(row.get().text().split(" "));

    String era = totals.get(3);
    String inningsPitched = totals.get(8);
    int earnedRunsAllowed = Integer.parseInt(totals.get(11));

    int gameLength = getGameLength(era, inningsPitched, earnedRunsAllowed);

    PitcherStatsImpl.Builder builder =
        PitcherStatsImpl.builder()
            .playerId(playerId)
            .teamId(teamId)
            .appearances(appearances)
            .wins(Integer.parseInt(totals.get(1)))
            .losses(Integer.parseInt(totals.get(2)))
            .era(era)
            .gamesStarted(Integer.parseInt(totals.get(4)))
            .saves(Integer.parseInt(totals.get(5)))
            .completeGames(Integer.parseInt(totals.get(6)))
            .shutouts(Integer.parseInt(totals.get(7)))
            .inningsPitched(inningsPitched)
            .hitsAllowed(Integer.parseInt(totals.get(9)))
            .runsAllowed(Integer.parseInt(totals.get(10)))
            .earnedRunsAllowed(earnedRunsAllowed)
            .walksAllowed(Integer.parseInt(totals.get(12)))
            .strikeouts(Integer.parseInt(totals.get(13)))
            .doublesAllowed(Integer.parseInt(totals.get(14)))
            .triplesAllowed(Integer.parseInt(totals.get(15)))
            .homerunsAllowed(Integer.parseInt(totals.get(16)))
            .atBats(Integer.parseInt(totals.get(17)))
            .opponentBattingAverage(totals.get(18))
            .wildPitches(Integer.parseInt(totals.get(19)))
            .hitByPitch(Integer.parseInt(totals.get(20)))
            .balks(Integer.parseInt(totals.get(21)))
            .sacrificeFliesAllowed(Integer.parseInt(totals.get(22)))
            .sacrificeHitsAllowed(Integer.parseInt(totals.get(23)))
            .groundOuts(Integer.parseInt(totals.get(24)))
            .flyOuts(Integer.parseInt(totals.get(25)))
            .gameLength(gameLength);

    return Optional.of(builder.build());
  }

  private Document getHtmlDoc(final String uri) throws StatsException, IOException {

    Request request = new okhttp3.Request.Builder().url(uri).get().build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body = response.body().byteString().utf8();
      response.close();
      return Jsoup.parse(body);
    }
    String responseString = response.toString();
    response.close();

    if (response.code() == 404) {
      throw new StatsException(
          "Stats could not be found. Please check that you are using the correct series identifier for this game.");
    }
    throw new StatsException(
        String.format("Unexpected response from %s, got %s", uri, responseString));
  }

  private int getGameLength(
      final String era, final String inningsPitched, final int earnedRunsAllowed) {
    double eraVal = Double.parseDouble(era);
    String[] split = inningsPitched.split("\\.");
    double inningsPitchedVal = Double.parseDouble(split[0]) + Double.parseDouble(split[1]) / 3.0;

    return (int) Math.round(eraVal * inningsPitchedVal / earnedRunsAllowed);
  }
}
