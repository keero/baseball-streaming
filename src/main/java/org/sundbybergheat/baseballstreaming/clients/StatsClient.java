package org.sundbybergheat.baseballstreaming.clients;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.sundbybergheat.baseballstreaming.models.stats.AllStatsImpl.Builder;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStats;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.StatsException;

public class StatsClient {
  private static final Logger LOG = LoggerFactory.getLogger(StatsClient.class);

  private static final String PLAYER_STATS_URL = "%s/en/events/%s/teams/%s/players/%s";

  private final OkHttpClient client;
  private final String baseUrl;

  public StatsClient(final OkHttpClient client, final String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  public AllStats getPlayerStats(
      final String playerName, final String playerId, final String teamId, final String seriesId)
      throws StatsException, IOException {
    Builder builder = AllStatsImpl.builder();
    String thisUri = String.format(PLAYER_STATS_URL, baseUrl, seriesId, teamId, playerId);

    int thisYear = Integer.parseInt(seriesId.split("-")[0]);

    LOG.info("Fetching stats for {} (id={}) in {}", playerName, playerId, seriesId);
    Document thisDoc = getHtmlDoc(thisUri);
    builder.putSeriesStats(
        seriesId,
        parsePlayerSeriesStats(thisDoc, playerId, teamId)
            .seriesName(seriesId)
            .year(thisYear)
            .build());

    Optional<Element> table = getTableForHeading("Other events", thisDoc);
    if (table.isPresent()) {
      Elements rows = table.get().getElementsByTag("tbody").first().getElementsByTag("tr");

      for (Element element : rows) {
        Optional<Element> link = element.getElementsByTag("a").stream().findFirst();
        if (link.isPresent()) {
          String uri = link.get().absUrl("href");
          String[] uriParts = uri.split("/");
          String id = uriParts[uriParts.length - 5];
          String seriesPlayerid = uriParts[uriParts.length - 1];
          String seriesTeamid = uriParts[uriParts.length - 3];
          String seriesName = link.get().text();
          Pattern p = Pattern.compile("^.*(2[0-9]{3}).*$");
          Matcher m = p.matcher(id);
          int seriesYear = m.matches() ? Integer.parseInt(m.group(1)) : 2000;
          LOG.info("Fetching stats for {} (id={}) in {}", playerName, seriesPlayerid, seriesName);
          Document doc = getHtmlDoc(uri);

          builder.putSeriesStats(
              id,
              parsePlayerSeriesStats(doc, seriesPlayerid, seriesTeamid)
                  .seriesName(seriesName)
                  .year(seriesYear)
                  .otherSeries(!id.substring(5).equalsIgnoreCase(seriesId.substring(5)))
                  .build());
        }
      }
    }

    return builder.build();
  }

  private Optional<Element> getTableForHeading(final String heading, final Document doc) {
    return doc.getElementsByAttributeValue("class", "box-container").stream()
        .filter(
            e ->
                !e.getElementsByTag("h3").isEmpty()
                    && heading.equals(e.getElementsByTag("h3").get(0).text()))
        .map(e -> e.getElementsByTag("table"))
        .findFirst()
        .map(t -> t.first());
  }

  private SeriesStatsImpl.Builder parsePlayerSeriesStats(
      final Document doc, final String playerId, final String teamId) {
    return SeriesStatsImpl.builder()
        .teamFlagUrl(parseTeamFlagUrl(doc))
        .playerImageUrl(parsePlayerImageUrl(doc))
        .batting(parsePlayerBatterStats(doc, playerId, teamId))
        .pitching(parsePlayerPitcherStats(doc, playerId, teamId));
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

    Optional<Element> table = getTableForHeading("Batting stats", doc);

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

    Optional<Element> table = getTableForHeading("Pitching stats", doc);

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

    PitcherStatsImpl.Builder builder =
        PitcherStatsImpl.builder()
            .playerId(playerId)
            .teamId(teamId)
            .appearances(appearances)
            .wins(Integer.parseInt(totals.get(1)))
            .losses(Integer.parseInt(totals.get(2)))
            .era(totals.get(3))
            .gamesStarted(Integer.parseInt(totals.get(4)))
            .saves(Integer.parseInt(totals.get(5)))
            .completeGames(Integer.parseInt(totals.get(6)))
            .shutouts(Integer.parseInt(totals.get(7)))
            .inningsPitched(totals.get(8))
            .hitsAllowed(Integer.parseInt(totals.get(9)))
            .runsAllowed(Integer.parseInt(totals.get(10)))
            .earnedRunsAllowed(Integer.parseInt(totals.get(11)))
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
            .flyOuts(Integer.parseInt(totals.get(25)));

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
}
