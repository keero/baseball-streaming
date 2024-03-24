package org.sundbybergheat.baseballstreaming.clients;

import java.io.IOException;
import java.util.Collections;
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
import org.sundbybergheat.baseballstreaming.models.stats.CareerStats;
import org.sundbybergheat.baseballstreaming.models.stats.Category;
import org.sundbybergheat.baseballstreaming.models.stats.CategoryStats;
import org.sundbybergheat.baseballstreaming.models.stats.CategoryStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesId;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesIdImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.StatsDataSet;
import org.sundbybergheat.baseballstreaming.models.stats.StatsException;

public class StatsClient {

  private static final Logger LOG = LoggerFactory.getLogger(StatsClient.class);

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

        String battingHtml =
            String.format("<table>%s</table>", categoryStats.careerStats().total().battingTotal());
        String pitchingHtml =
            String.format("<table>%s</table>", categoryStats.careerStats().total().pitchingTotal());

        careerBatting = parseTotalBatterStats(battingHtml, playerId);
        careerPitching = parseTotalPitcherStats(pitchingHtml, playerId);
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
    String battingHtml = categoryStats.careerStats().stats().b();
    String pitchingHtml = categoryStats.careerStats().stats().p();
    Map<String, BatterStats> batterStats =
        battingHtml.isEmpty()
            ? Collections.emptyMap()
            : parseBatterStats(String.format("<table>%s</table>", battingHtml), playerId);
    Map<String, PitcherStats> pitcherStats =
        pitchingHtml.isEmpty()
            ? Collections.emptyMap()
            : parsePitcherStats(String.format("<table>%s</table>", pitchingHtml), playerId);

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
      final String battingHtml, final String playerId) {

    Map<String, BatterStats> result = new HashMap<String, BatterStats>();

    Document doc = Jsoup.parse(battingHtml);
    List<String> years =
        doc.getElementsByClass("year").stream().map(e -> e.text()).collect(Collectors.toList());

    List<String> teamCodes =
        doc.getElementsByClass("teamcode").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> games =
        doc.getElementsByClass("g").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> atBats =
        doc.getElementsByClass("ab").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> runs =
        doc.getElementsByClass("r").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> hits =
        doc.getElementsByClass("h").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> doubles =
        doc.getElementsByClass("double").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> triples =
        doc.getElementsByClass("triple").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> homeruns =
        doc.getElementsByClass("hr").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> runsBattedIn =
        doc.getElementsByClass("rbi").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> totalBases =
        doc.getElementsByClass("tb").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> battingAverage =
        doc.getElementsByClass("avg").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> slugging =
        doc.getElementsByClass("slg").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> onBasePercentage =
        doc.getElementsByClass("obp").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> onBasePercentagePlusSlugging =
        doc.getElementsByClass("ops").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> walks =
        doc.getElementsByClass("bb").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> hitByPitch =
        doc.getElementsByClass("hbp").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> strikeouts =
        doc.getElementsByClass("so").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> groundoutDoublePlay =
        doc.getElementsByClass("gdp").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> sacrificeFlies =
        doc.getElementsByClass("sf").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> sacrificeHits =
        doc.getElementsByClass("sh").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> stolenBases =
        doc.getElementsByClass("sb").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> caughtStealing =
        doc.getElementsByClass("cs").stream().map(e -> e.text()).collect(Collectors.toList());

    for (int i = 0; i < years.size(); i++) {
      result.put(
          years.get(i),
          BatterStatsImpl.builder()
              .playerId(playerId)
              .teamId(teamCodes.get(i))
              .games(Integer.parseInt(games.get(i)))
              .atBats(Integer.parseInt(atBats.get(i)))
              .runs(Integer.parseInt(runs.get(i)))
              .hits(Integer.parseInt(hits.get(i)))
              .doubles(Integer.parseInt(doubles.get(i)))
              .triples(Integer.parseInt(triples.get(i)))
              .homeruns(Integer.parseInt(homeruns.get(i)))
              .runsBattedIn(Integer.parseInt(runsBattedIn.get(i)))
              .totalBases(Integer.parseInt(totalBases.get(i)))
              .battingAverage(battingAverage.get(i))
              .slugging(slugging.get(i))
              .onBasePercentage(onBasePercentage.get(i))
              .onBasePercentagePlusSlugging(onBasePercentagePlusSlugging.get(i))
              .walks(Integer.parseInt(walks.get(i)))
              .hitByPitch(Integer.parseInt(hitByPitch.get(i)))
              .strikeouts(Integer.parseInt(strikeouts.get(i)))
              .groundoutDoublePlay(Integer.parseInt(groundoutDoublePlay.get(i)))
              .sacrificeFlies(Integer.parseInt(sacrificeFlies.get(i)))
              .sacrificeHits(Integer.parseInt(sacrificeHits.get(i)))
              .stolenBases(Integer.parseInt(stolenBases.get(i)))
              .caughtStealing(Integer.parseInt(caughtStealing.get(i)))
              .build());
    }
    return result;
  }

  private Optional<BatterStats> parseTotalBatterStats(
      final String battingHtml, final String playerId) {
    Document doc = Jsoup.parse(battingHtml);
    List<String> totals =
        doc.getElementsByTag("th").stream().map(e -> e.text()).collect(Collectors.toList());
    if (totals.size() != 25) {
      return Optional.empty();
    }
    BatterStats batterStats =
        BatterStatsImpl.builder()
            .playerId(playerId)
            .teamId("")
            .games(Integer.parseInt(totals.get(3)))
            .atBats(Integer.parseInt(totals.get(5)))
            .runs(Integer.parseInt(totals.get(6)))
            .hits(Integer.parseInt(totals.get(7)))
            .doubles(Integer.parseInt(totals.get(8)))
            .triples(Integer.parseInt(totals.get(9)))
            .homeruns(Integer.parseInt(totals.get(10)))
            .runsBattedIn(Integer.parseInt(totals.get(11)))
            .totalBases(Integer.parseInt(totals.get(12)))
            .walks(Integer.parseInt(totals.get(13)))
            .hitByPitch(Integer.parseInt(totals.get(14)))
            .strikeouts(Integer.parseInt(totals.get(15)))
            .groundoutDoublePlay(Integer.parseInt(totals.get(16)))
            .sacrificeFlies(Integer.parseInt(totals.get(17)))
            .sacrificeHits(Integer.parseInt(totals.get(18)))
            .stolenBases(Integer.parseInt(totals.get(19)))
            .caughtStealing(Integer.parseInt(totals.get(20)))
            .battingAverage(totals.get(21))
            .slugging(totals.get(22))
            .onBasePercentage(totals.get(23))
            .onBasePercentagePlusSlugging(totals.get(24))
            .build();

    return Optional.of(batterStats);
  }

  private Map<String, PitcherStats> parsePitcherStats(
      final String pitchingHtml, final String playerId) {

    Map<String, PitcherStats> result = new HashMap<String, PitcherStats>();

    Document doc = Jsoup.parse(pitchingHtml);
    List<String> years =
        doc.getElementsByClass("year").stream().map(e -> e.text()).collect(Collectors.toList());

    List<String> teamCodes =
        doc.getElementsByClass("teamcode").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> appearances =
        doc.getElementsByClass("pitch_appear").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> wins =
        doc.getElementsByClass("pitch_win").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> losses =
        doc.getElementsByClass("pitch_loss").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> era =
        doc.getElementsByClass("era").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> gamesStarted =
        doc.getElementsByClass("pitch_gs").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> saves =
        doc.getElementsByClass("pitch_save").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> completeGames =
        doc.getElementsByClass("pitch_cg").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> shutouts =
        doc.getElementsByClass("pitch_so").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> inningsPitched =
        doc.getElementsByClass("pitch_ip").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> hitsAllowed =
        doc.getElementsByClass("pitch_h").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> runsAllowed =
        doc.getElementsByClass("pitch_r").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> earnedRunsAllowed =
        doc.getElementsByClass("pitch_er").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> walksAllowed =
        doc.getElementsByClass("pitch_bb").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> strikeouts =
        doc.getElementsByClass("pitch_so").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> doublesAllowed =
        doc.getElementsByClass("pitch_double").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> triplesAllowed =
        doc.getElementsByClass("pitch_triple").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> homerunsAllowed =
        doc.getElementsByClass("pitch_hr").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> atBats =
        doc.getElementsByClass("pitch_ab").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> opponentBattingAverage =
        doc.getElementsByClass("bavg").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> wildPitches =
        doc.getElementsByClass("pitch_wp").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> hitByPitch =
        doc.getElementsByClass("pitch_hbp").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> balks =
        doc.getElementsByClass("pitch_bk").stream().map(e -> e.text()).collect(Collectors.toList());
    List<String> sacrificeFliesAllowed =
        doc.getElementsByClass("pitch_sfa").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> sacrificeHitsAllowed =
        doc.getElementsByClass("pitch_sha").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> groundOuts =
        doc.getElementsByClass("pitch_ground").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());
    List<String> flyOuts =
        doc.getElementsByClass("pitch_fly").stream()
            .map(e -> e.text())
            .collect(Collectors.toList());

    for (int i = 0; i < years.size(); i++) {
      result.put(
          years.get(i),
          PitcherStatsImpl.builder()
              .playerId(playerId)
              .teamId(teamCodes.get(i))
              .appearances(Integer.parseInt(appearances.get(i)))
              .wins(Integer.parseInt(wins.get(i)))
              .losses(Integer.parseInt(losses.get(i)))
              .era(era.get(i))
              .gamesStarted(Integer.parseInt(gamesStarted.get(i)))
              .saves(Integer.parseInt(saves.get(i)))
              .completeGames(Integer.parseInt(completeGames.get(i)))
              .shutouts(Integer.parseInt(shutouts.get(i)))
              .inningsPitched(inningsPitched.get(i))
              .hitsAllowed(Integer.parseInt(hitsAllowed.get(i)))
              .runsAllowed(Integer.parseInt(runsAllowed.get(i)))
              .earnedRunsAllowed(Integer.parseInt(earnedRunsAllowed.get(i)))
              .walksAllowed(Integer.parseInt(walksAllowed.get(i)))
              .strikeouts(Integer.parseInt(strikeouts.get(i)))
              .doublesAllowed(Integer.parseInt(doublesAllowed.get(i)))
              .triplesAllowed(Integer.parseInt(triplesAllowed.get(i)))
              .homerunsAllowed(Integer.parseInt(homerunsAllowed.get(i)))
              .atBats(Integer.parseInt(atBats.get(i)))
              .opponentBattingAverage(opponentBattingAverage.get(i))
              .wildPitches(Integer.parseInt(wildPitches.get(i)))
              .hitByPitch(Integer.parseInt(hitByPitch.get(i)))
              .balks(Integer.parseInt(balks.get(i)))
              .sacrificeFliesAllowed(Integer.parseInt(sacrificeFliesAllowed.get(i)))
              .sacrificeHitsAllowed(Integer.parseInt(sacrificeHitsAllowed.get(i)))
              .groundOuts(Integer.parseInt(groundOuts.get(i)))
              .flyOuts(Integer.parseInt(flyOuts.get(i)))
              .gameLength(-1)
              .build());
    }
    return result;
  }

  private Optional<PitcherStats> parseTotalPitcherStats(
      final String pitchingHtml, final String playerId) {
    Document doc = Jsoup.parse(pitchingHtml);
    List<String> totals =
        doc.getElementsByTag("th").stream().map(e -> e.text()).collect(Collectors.toList());
    if (totals.size() != 29) {
      return Optional.empty();
    }
    PitcherStats pitcherStats =
        PitcherStatsImpl.builder()
            .playerId(playerId)
            .teamId("")
            .appearances(Integer.parseInt(totals.get(5)))
            .wins(Integer.parseInt(totals.get(2)))
            .losses(Integer.parseInt(totals.get(3)))
            .era(totals.get(4))
            .gamesStarted(Integer.parseInt(totals.get(6)))
            .saves(Integer.parseInt(totals.get(7)))
            .completeGames(Integer.parseInt(totals.get(8)))
            .shutouts(Integer.parseInt(totals.get(9)))
            .inningsPitched(totals.get(10))
            .hitsAllowed(Integer.parseInt(totals.get(11)))
            .runsAllowed(Integer.parseInt(totals.get(12)))
            .earnedRunsAllowed(Integer.parseInt(totals.get(13)))
            .walksAllowed(Integer.parseInt(totals.get(14)))
            .strikeouts(Integer.parseInt(totals.get(15)))
            .doublesAllowed(Integer.parseInt(totals.get(16)))
            .triplesAllowed(Integer.parseInt(totals.get(17)))
            .homerunsAllowed(Integer.parseInt(totals.get(18)))
            .atBats(Integer.parseInt(totals.get(19)))
            .opponentBattingAverage(totals.get(20))
            .wildPitches(Integer.parseInt(totals.get(21)))
            .hitByPitch(Integer.parseInt(totals.get(22)))
            .balks(Integer.parseInt(totals.get(23)))
            .sacrificeFliesAllowed(Integer.parseInt(totals.get(24)))
            .sacrificeHitsAllowed(Integer.parseInt(totals.get(25)))
            .groundOuts(Integer.parseInt(totals.get(26)))
            .flyOuts(Integer.parseInt(totals.get(27)))
            .gameLength(-1)
            .build();

    return Optional.of(pitcherStats);
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
    Request request = new okhttp3.Request.Builder().url(fullCareerLink).get().build();

    Response response = client.newCall(request).execute();

    if (!response.isSuccessful()) {
      response.close();
      throw new StatsException(
          String.format("Failed to fetch %s (got %d)", fullCareerLink, response.code()));
    }
    String body = response.body().byteString().utf8();
    response.close();
    Pattern p = Pattern.compile(".*dataset\\s+=\\s+\\(([^;]+)\\);.*", Pattern.DOTALL);
    Matcher m = p.matcher(body);
    if (!m.matches()) {
      throw new StatsException(String.format("Could not find Stats dataset @ %s", fullCareerLink));
    }
    StatsDataSet dataSet = JsonMapper.fromJson(m.group(1), StatsDataSet.class);
    return dataSet.elementRS().categories();
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
    Request request = new okhttp3.Request.Builder().url(uri).get().build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body = response.body().byteString().utf8().replaceAll("\\[\\]", "\"\"");
      response.close();
      CareerStats careerStats = JsonMapper.fromJson(body, CareerStats.class);
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
