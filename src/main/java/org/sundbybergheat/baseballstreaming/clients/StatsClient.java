package org.sundbybergheat.baseballstreaming.clients;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;
import org.sundbybergheat.baseballstreaming.models.stats.AllStats;
import org.sundbybergheat.baseballstreaming.models.stats.AllStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStats;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.Category;
import org.sundbybergheat.baseballstreaming.models.stats.CategoryStats;
import org.sundbybergheat.baseballstreaming.models.stats.CategoryStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesId;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.StatsDataSet;
import org.sundbybergheat.baseballstreaming.models.stats.StatsException;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.BattingStats;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.CareerStats;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.CareerStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.CareerStatsV2;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.CareerStatsV3;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.CareerStatsV4;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.PitchingStats;
import org.sundbybergheat.baseballstreaming.models.stats.wbsc.TotalsRow;

public class StatsClient {

  private static final Logger LOG = LoggerFactory.getLogger(StatsClient.class);

  private static final String HTTP_CLIENT_USER_AGENT =
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";

  private static final String PLAYER_STATS_URL = "%s/en/events/%s/teams/%s/players/%s";
  private static final String PLAYER_STATS_API_URL =
      "%s/api/v1/player/stats?tab=career&fedId=%s&eventCategory=%s&pId=%s";

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
      final SeriesId seriesId,
      final boolean onlyUseThisSeriesStats)
      throws StatsException, IOException {
    final Map<String, SeriesStats> seriesStats = new HashMap<String, SeriesStats>();
    final String thisUri =
        String.format(PLAYER_STATS_URL, baseUrl, seriesId.id(), teamId, playerId);

    final Document doc = Jsoup.connect(thisUri).get();
    final String fullCareerLink = getFullCareerLink(doc);
    Optional<String> teamFlagUrl = parseTeamFlagUrl(doc);
    Optional<String> playerImageUrl = parsePlayerImageUrl(doc);
    final List<Category> categories = getCategories(fullCareerLink);
    final String apiPlayerId = getAPIPlayerId(fullCareerLink);

    Category thisCategory = getThisCategory(categories, seriesId.id());

    Optional<BatterStats> careerBatting = Optional.empty();
    Optional<PitcherStats> careerPitching = Optional.empty();

    for (Category category : categories) {
      final boolean otherSeries = !category.value().equals(thisCategory.value());
      if (onlyUseThisSeriesStats && otherSeries) {
        continue;
      }
      LOG.info("Fetching stats for {} (id={}) in '{}'.", playerName, playerId, category.text());
      CategoryStats categoryStats = getCategoryStats(category, apiPlayerId);
      Map<String, SeriesStats> mappedStats =
          mapCategoryStats(
              categoryStats, playerId, otherSeries, seriesId, teamFlagUrl, playerImageUrl);
      seriesStats.putAll(mappedStats);

      if (!otherSeries) {

        LOG.info("Selecting '{}' career stats for {}", category.text(), playerName);

        TotalsRow totalsRow = categoryStats.careerStats().totalsRow();
        careerBatting =
            totalsRow.batting().map(batting -> parseTotalBatterStats(batting, playerId));
        careerPitching =
            totalsRow.pitching().map(pitching -> parseTotalPitcherStats(pitching, playerId));
      }
    }

    return AllStatsImpl.builder()
        .seriesStats(seriesStats)
        .careerBatting(careerBatting)
        .careerPitching(careerPitching)
        .build();
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

  private Map<String, SeriesStats> mapCategoryStats(
      final CategoryStats categoryStats,
      final String playerId,
      final boolean otherSeries,
      final SeriesId thisSeriesId,
      final Optional<String> teamFlagUrl,
      final Optional<String> playerImageUrl) {
    Map<String, SeriesStats> result = new HashMap<String, SeriesStats>();
    List<BattingStats> battings = categoryStats.careerStats().batting();
    List<PitchingStats> pitchings = categoryStats.careerStats().pitching();
    Map<String, BatterStats> batterStats = parseBatterStats(battings, playerId);
    Map<String, PitcherStats> pitcherStats = parsePitcherStats(pitchings, playerId);

    Set<String> years = new HashSet<String>(batterStats.keySet());
    years.addAll(pitcherStats.keySet());

    for (String year : years) {
      String seriesId =
          !otherSeries && thisSeriesId.year().orElse(0).equals(Integer.parseInt(year))
              ? thisSeriesId.id()
              : String.format("%s-%s", year, categoryStats.category().value());
      SeriesStats seriesStats =
          SeriesStatsImpl.builder()
              .id(seriesId)
              .year(Integer.parseInt(year))
              .otherSeries(otherSeries)
              .seriesName(String.format("%s %s", categoryStats.category().text(), year))
              .batting(Optional.ofNullable(batterStats.getOrDefault(year, null)))
              .pitching(Optional.ofNullable(pitcherStats.getOrDefault(year, null)))
              .teamFlagUrl(teamFlagUrl)
              .playerImageUrl(playerImageUrl)
              .build();
      result.put(seriesId, seriesStats);
    }
    return result;
  }

  private Map<String, BatterStats> parseBatterStats(
      final List<BattingStats> battings, final String playerId) {

    Map<String, BatterStats> result = new HashMap<String, BatterStats>();

    for (BattingStats batting : battings) {
      result.put(Integer.toString(batting.year()), parseTotalBatterStats(batting, playerId));
    }

    return result;
  }

  private BatterStats parseTotalBatterStats(final BattingStats batting, final String playerId) {
    // @TODO: Remove this now unnecessary mapping between BattingStats -> BatterStats. We should be
    // able to use BattingStats directly.
    BatterStats batterStats =
        BatterStatsImpl.builder()
            .playerId(playerId)
            .teamId("")
            .games(batting.games())
            .atBats(batting.atBats())
            .runs(batting.runs())
            .hits(batting.hits())
            .doubles(batting.doubles())
            .triples(batting.triples())
            .homeruns(batting.homeruns())
            .runsBattedIn(batting.rbi())
            .totalBases(batting.totalBases())
            .walks(batting.walks())
            .hitByPitch(batting.hitByPitch())
            .strikeouts(batting.strikeouts())
            .groundoutDoublePlay(batting.groundoutDoublePlay())
            .sacrificeFlies(batting.sacrificeFlies())
            .sacrificeHits(batting.sacrificeHits())
            .stolenBases(batting.stolenBases())
            .caughtStealing(batting.caughtStealing())
            .battingAverage(batting.battingAverage())
            .slugging(batting.slugging())
            .onBasePercentage(batting.onBasePercentage())
            .onBasePercentagePlusSlugging(batting.onBasePercentagePlusSlugging())
            .build();

    return batterStats;
  }

  private Map<String, PitcherStats> parsePitcherStats(
      final List<PitchingStats> pitchings, final String playerId) {

    Map<String, PitcherStats> result = new HashMap<String, PitcherStats>();

    for (PitchingStats pitching : pitchings) {
      result.put(Integer.toString(pitching.year()), parseTotalPitcherStats(pitching, playerId));
    }
    return result;
  }

  // @TODO:
  private PitcherStats parseTotalPitcherStats(final PitchingStats pitching, final String playerId) {
    // @TODO: Remove this now unnecessary mapping between PitchingStats -> PitcherStats. We should
    // be able to use PitchingStats directly.
    PitcherStats pitcherStats =
        PitcherStatsImpl.builder()
            .playerId(playerId)
            .teamId("")
            .appearances(pitching.appearances())
            .wins(pitching.wins())
            .losses(pitching.losses())
            .era(pitching.era())
            .gamesStarted(pitching.gamesStarted())
            .saves(pitching.saves())
            .completeGames(pitching.completeGames())
            .shutouts(pitching.shutouts())
            .inningsPitched(pitching.inningsPitched())
            .hitsAllowed(pitching.hitsAllowed())
            .runsAllowed(pitching.runsAllowed())
            .earnedRunsAllowed(pitching.earnedRunsAllowed())
            .walksAllowed(pitching.walksAllowed())
            .strikeouts(pitching.strikeouts())
            .doublesAllowed(pitching.doublesAllowed())
            .triplesAllowed(pitching.triplesAllowed())
            .homerunsAllowed(pitching.homerunsAllowed())
            .atBats(pitching.atBats())
            .opponentBattingAverage(pitching.opponentBattingAverage())
            .wildPitches(pitching.wildPitches())
            .hitByPitch(pitching.hitByPitch())
            .balks(pitching.balks())
            .sacrificeFliesAllowed(pitching.sacrificeFliesAllowed())
            .sacrificeHitsAllowed(pitching.sacrificeHitsAllowed())
            .groundOuts(pitching.groundOuts())
            .flyOuts(pitching.flyOuts())
            .gameLength(-1)
            .build();

    return pitcherStats;
  }

  private String getFullCareerLink(final Document doc) throws StatsException {
    final Element fullCareerButton = doc.getElementsByClass("full_career_btn").first();

    if (fullCareerButton == null) {
      throw new StatsException("Full Career button not found on stats page.");
    }
    return fullCareerButton.parent().attributes().get("href");
  }

  private List<Category> getCategories(final String fullCareerLink)
      throws StatsException, IOException {
    Request request =
        new okhttp3.Request.Builder()
            .url(fullCareerLink)
            .addHeader("user-agent", HTTP_CLIENT_USER_AGENT)
            .get()
            .build();

    Response response = client.newCall(request).execute();

    if (!response.isSuccessful()) {
      response.close();
      throw new StatsException(
          String.format("Failed to fetch %s (got %d)", fullCareerLink, response.code()));
    }
    String body = response.body().byteString().utf8();
    response.close();
    Pattern p = Pattern.compile(".*data-filters=\"([^\"]+)\".*", Pattern.DOTALL);
    // Pattern p = Pattern.compile(".*dataset\\s+=\\s+\\(([^;]+)\\);.*", Pattern.DOTALL);
    Matcher m = p.matcher(body);
    if (!m.matches()) {
      throw new StatsException(String.format("Could not find Stats dataset @ %s", fullCareerLink));
    }
    String json = m.group(1).replaceAll("&quot;", "\"");
    StatsDataSet dataSet = JsonMapper.fromJson(json, StatsDataSet.class);
    return dataSet.elementRS().categories();
  }

  private String getAPIPlayerId(final String fullCareerLink) throws StatsException {
    Pattern pattern = Pattern.compile("^.*-([0-9]+)/history$");
    Matcher matcher = pattern.matcher(fullCareerLink);
    if (!matcher.matches()) {
      throw new StatsException(
          String.format("Unable to get API Player ID from %s.", fullCareerLink));
    }
    return matcher.group(1);
  }

  private CategoryStats getCategoryStats(final Category category, final String apiPlayerId)
      throws StatsException, IOException {

    final String uri =
        String.format(
            PLAYER_STATS_API_URL, baseUrl, category.fedId(), category.value(), apiPlayerId);
    Request request =
        new okhttp3.Request.Builder()
            .url(uri)
            .addHeader("user-agent", HTTP_CLIENT_USER_AGENT)
            .get()
            .build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body =
          response
              .body()
              .byteString()
              .utf8()
              .replaceAll("\"Totals\"", "0")
              .replaceAll("\\[\\]", "null");
      response.close();
      CareerStats careerStats = null;
      // @TODO: The WBSC APIs tend to mix arrays and maps for the same field at times. The ugly
      // solution below to try mapping to different java models should be replaced by a custom JSON
      // parser that is able to handle the differences.
      try {
        careerStats = JsonMapper.fromJson(body, CareerStats.class);
      } catch (MismatchedInputException e) {
        try {
          CareerStatsV3 temp = JsonMapper.fromJson(body, CareerStatsV3.class);
          careerStats =
              CareerStatsImpl.builder()
                  .batting(temp.batting())
                  .pitching(temp.pitching().values().stream().collect(Collectors.toList()))
                  .totalsRow(temp.totalsRow())
                  .build();
        } catch (MismatchedInputException e2) {
          try {
            CareerStatsV2 temp = JsonMapper.fromJson(body, CareerStatsV2.class);
            careerStats =
                CareerStatsImpl.builder()
                    .batting(temp.batting().values().stream().collect(Collectors.toList()))
                    .pitching(temp.pitching())
                    .totalsRow(temp.totalsRow())
                    .build();
          } catch (MismatchedInputException e3) {
            CareerStatsV4 temp = JsonMapper.fromJson(body, CareerStatsV4.class);
            careerStats =
                CareerStatsImpl.builder()
                    .batting(temp.batting().values().stream().collect(Collectors.toList()))
                    .pitching(temp.pitching().values().stream().collect(Collectors.toList()))
                    .totalsRow(temp.totalsRow())
                    .build();
          }
        }
      }
      return CategoryStatsImpl.builder().careerStats(careerStats).category(category).build();
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

  private Category getThisCategory(final List<Category> categories, final String seriesId)
      throws StatsException {
    LevenshteinDistance distance = LevenshteinDistance.getDefaultInstance();
    return categories.stream()
        .sorted(
            (a, b) ->
                distance.apply(seriesId.toLowerCase(), a.value().toLowerCase())
                    - distance.apply(seriesId.toLowerCase(), b.value().toLowerCase()))
        .findFirst()
        .orElseThrow(() -> new StatsException("Unable to get current series category"));
  }
}
