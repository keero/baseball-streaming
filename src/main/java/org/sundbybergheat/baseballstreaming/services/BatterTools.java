package org.sundbybergheat.baseballstreaming.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayData;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Player;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayerSeasonStats;

public class BatterTools {
  private static final Logger LOG = LoggerFactory.getLogger(BatterTools.class);

  public static Optional<Player> aggregatedBoxScore(final String playerId, final Play play) {

    return play.boxScore().entrySet().stream()
        .filter(kv -> kv.getKey().startsWith("10") || kv.getKey().startsWith("20"))
        .map(kv -> kv.getValue())
        .filter(bs -> playerId.equals(bs.playerId().orElse("")))
        .reduce(
            (a, b) ->
                new Player(
                    a.fullName(),
                    a.firstName(),
                    a.lastName(),
                    a.playerId(),
                    a.teamId(),
                    a.teamCode(),
                    a.imageUrl(),
                    a.inPlay(),
                    addNumbers(a.reEntry(), b.reEntry()),
                    Optional.of(
                        String.join(
                            "/",
                            Stream.concat(
                                    Arrays.stream(a.position().orElse("").split("/")),
                                    Arrays.stream(b.position().orElse("").split("/")))
                                .collect(Collectors.toSet()))),
                    addNumbers(a.plateAppearances(), b.plateAppearances()),
                    addNumbers(a.atBats(), b.atBats()),
                    addNumbers(a.runs(), b.runs()),
                    addNumbers(a.hits(), b.hits()),
                    addNumbers(a.runsbattedIn(), b.runsbattedIn()),
                    addNumbers(a.walks(), b.walks()),
                    addNumbers(a.strikeouts(), b.strikeouts()),
                    addNumbers(a.doubles(), b.doubles()),
                    addNumbers(a.triples(), b.triples()),
                    addNumbers(a.homeruns(), b.homeruns()),
                    addNumbers(a.sacFlies(), b.sacFlies()),
                    addNumbers(a.hitByPitch(), b.hitByPitch()),
                    addNumbers(a.stolenBases(), b.stolenBases()),
                    addNumbers(a.caughtStealing(), b.caughtStealing()),
                    a.seasonStats(),
                    addNumbers(a.leftOnBase(), b.leftOnBase()),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    addNumbers(a.putOuts(), b.putOuts()),
                    addNumbers(a.assists(), b.assists()),
                    addNumbers(a.errors(), b.errors()),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()));
  }

  private static List<PlayData> plateAppearances(final Player batter, final Play play) {

    if (batter.playerId().isEmpty()) {
      LOG.error("Cannot fetch plate appearances for batter without ID. {}", batter);
      return Collections.emptyList();
    }

    final String batterId = batter.playerId().get();
    List<PlayData> batterPlays =
        play.playData().stream()
            .filter(
                pd ->
                    (pd.batter().orElse("").startsWith("10")
                            || pd.batter().orElse("").startsWith("20"))
                        && batterId.equals(
                            play.boxScore().get(pd.batter().orElse("")).playerId().orElse("")))
            .sorted(
                (a, b) -> {
                  int atBatCompare =
                      NumberUtils.toInt(b.atBat().orElse("0"))
                          - NumberUtils.toInt((a.atBat().orElse("0")));
                  if (atBatCompare != 0) {
                    return atBatCompare;
                  }
                  return Long.valueOf(
                          NumberUtils.toLong(b.timestamp().orElse("0"))
                              - NumberUtils.toLong(a.timestamp().orElse("0")))
                      .intValue();
                })
            .collect(Collectors.toList());
    List<PlayData> plateApperances =
        batterPlays.stream()
            .filter(
                bp ->
                    bp.text()
                        .orElse("")
                        .toLowerCase()
                        .startsWith(batter.fullName().orElse("").toLowerCase()))
            .collect(Collectors.toList());
    return plateApperances;
  }

  public static String batterNarrative(final Player batter, final Play play) {
    List<PlayData> plateApperances = plateAppearances(batter, play);
    if (batter.plateAppearances().map(pa -> NumberUtils.toInt(pa)).orElse(0) > 0) {
      return summaryOfGame(batter, plateApperances);
    }
    if (batter.seasonStats().isPresent()) {
      return summaryOfSeason(batter.seasonStats().get());
    }
    return "";
  }

  public static String batterNarrativeTitle(final Player batter) {
    return batter.plateAppearances().map(pa -> NumberUtils.toInt(pa)).orElse(0) > 0
        ? "In This Game"
        : "This Season";
  }

  private static String summaryOfGame(final Player batter, final List<PlayData> plateApperances) {

    int plateAppearances = batter.plateAppearances().map(x -> NumberUtils.toInt(x)).orElse(0);
    int atBats = batter.atBats().map(x -> NumberUtils.toInt(x)).orElse(0);
    int hits = batter.hits().map(x -> NumberUtils.toInt(x)).orElse(0);
    int homeruns = batter.homeruns().map(x -> NumberUtils.toInt(x)).orElse(0);
    int triples = batter.triples().map(x -> NumberUtils.toInt(x)).orElse(0);
    int doubles = batter.doubles().map(x -> NumberUtils.toInt(x)).orElse(0);
    int strikeouts = batter.strikeouts().map(x -> NumberUtils.toInt(x)).orElse(0);
    int walks = batter.walks().map(x -> NumberUtils.toInt(x)).orElse(0);
    int hitByPitch = batter.hitByPitch().map(x -> NumberUtils.toInt(x)).orElse(0);
    int stolenBases = batter.stolenBases().map(x -> NumberUtils.toInt(x)).orElse(0);
    int runsbattedIn = batter.runsbattedIn().map(x -> NumberUtils.toInt(x)).orElse(0);
    int runs = batter.runs().map(x -> NumberUtils.toInt(x)).orElse(0);

    if (plateAppearances == 0) {
      return "First plate appearance";
    }

    List<String> summary = new ArrayList<String>();
    if (atBats > 0) {
      summary.add(String.format("%s for %s", hits, atBats));

      if (hits > 0) {
        if (homeruns > 1) {
          summary.add(homeruns + " HOME RUNS");
        } else if (homeruns == 1) {
          summary.add("HOME RUN");
        }

        if (triples > 1) {
          summary.add(triples + " TRIPLES");
        } else if (triples == 1) {
          summary.add("TRIPLE");
        }

        if (doubles > 1) {
          summary.add(doubles + " DOUBLES");
        } else if (doubles == 1) {
          summary.add("DOUBLE");
        }

        int singles = hits - homeruns - triples - doubles;

        if (singles > 1) {
          summary.add(singles + " SINGLES");
        } else if (singles == 1) {
          summary.add("SINGLE");
        }
      } else {
        long sacFlies =
            plateApperances.stream()
                .filter(pa -> pa.text().orElse("").toLowerCase().contains("sacrifice fly"))
                .count();
        if (sacFlies > 1) {
          summary.add(sacFlies + " SAC FLIES");
        } else if (sacFlies == 1) {
          summary.add("SAC FLY");
        }

        long sacHits =
            plateApperances.stream()
                .filter(pa -> pa.text().orElse("").toLowerCase().contains("sacrifice hit"))
                .count();
        if (sacHits > 1) {
          summary.add(sacHits + " SAC HITS");
        } else if (sacHits == 1) {
          summary.add("SAC HIT");
        }

        if (strikeouts > 1) {
          summary.add(strikeouts + " STRIKEOUTS");
        } else if (strikeouts == 1) {
          summary.add("STRIKEOUT");
        }

        long flyOuts =
            plateApperances.stream()
                .filter(pa -> pa.text().orElse("").toLowerCase().contains("flies out"))
                // Don't count sac fly as fly out.
                .filter(pa -> !pa.text().orElse("").toLowerCase().contains("sacrifice fly"))
                .count();
        if (flyOuts > 1) {
          summary.add(flyOuts + " FLYOUTS");
        } else if (flyOuts == 1) {
          summary.add("FLYOUT");
        }

        long groundOuts =
            plateApperances.stream()
                .filter(pa -> pa.text().orElse("").toLowerCase().contains("grounds out"))
                .count();
        if (groundOuts > 1) {
          summary.add(groundOuts + " GROUNDOUTS");
        } else if (groundOuts == 1) {
          summary.add("GROUNDOUT");
        }

        long gdp =
            plateApperances.stream()
                .filter(
                    pa -> pa.text().orElse("").toLowerCase().contains("hits into a double play"))
                .count();
        if (gdp > 1) {
          summary.add(gdp + " GDP");
        } else if (gdp == 1) {
          summary.add("GDP");
        }
      }
    }

    if (walks > 1) {
      summary.add(walks + " WALKS");
    } else if (walks == 1) {
      summary.add("WALK");
    }

    if (hitByPitch > 0) {
      summary.add("HIT BY PITCH");
    }

    long roe =
        plateApperances.stream()
            .filter(pa -> pa.text().orElse("").matches(".*reaches on [ a-z]*error.*"))
            .count();
    if (roe > 0) {
      summary.add("REACH ON ERROR");
    }

    long fc =
        plateApperances.stream()
            .filter(pa -> pa.text().orElse("").toLowerCase().contains("reaches on fielders choice"))
            .count();
    if (fc > 0) {
      summary.add("FIELDER'S CHOICE");
    }

    if (stolenBases > 1) {
      summary.add(stolenBases + " STOLEN BASES");
    } else if (stolenBases == 1) {
      summary.add("STOLEN BASE");
    }

    if (summary.size() > 4) {
      summary = summary.subList(0, 4);
    }

    if (runsbattedIn > 1) {
      summary.add(runsbattedIn + " RBI");
    } else if (runsbattedIn == 1) {
      summary.add("RBI");
    }

    if (runs > 1) {
      summary.add(runs + " RUNS");
    } else if (runs == 1) {
      summary.add("RUN");
    }
    if (summary.size() > 0) {
      return String.join(", ", summary);
    }

    return "";
  }

  private static String summaryOfSeason(final PlayerSeasonStats stats) {
    final Integer plateAppearances =
        stats.plateAppearances().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer atBats = stats.atBats().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer hits = stats.hits().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer walks = stats.walks().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer doubles = stats.doubles().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer triples = stats.triples().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer homeruns = stats.homeruns().map(x -> NumberUtils.toInt(x)).orElse(0);
    final Integer hitByPitch = stats.hitByPitch().map(x -> NumberUtils.toInt(x)).orElse(0);

    if (plateAppearances == 0) {
      return "First plate appearance";
    }

    final List<String> summary = new ArrayList<String>();

    if (atBats > 0) {
      summary.add(String.format("%d for %d", hits, atBats));
    }

    if (homeruns > 0) {
      summary.add(String.format("%d HOMERUN%s", homeruns, homeruns > 1 ? "S" : ""));
    }

    if (summary.size() > 1) {
      return String.format(
          "%s   %s", summary.get(0), String.join(", ", summary.subList(1, summary.size())));
    }

    if (doubles + triples + homeruns > 1) {
      summary.add(String.format("%d EXTRA BASE HITS", doubles + triples + homeruns));
    }

    if (summary.size() > 1) {
      return String.format(
          "%s   %s", summary.get(0), String.join(", ", summary.subList(1, summary.size())));
    }

    if (walks > 0) {
      summary.add(String.format("%d WALK%s", walks, walks > 1 ? "S" : ""));
    }

    if (hitByPitch > 1) {
      summary.add(String.format("HIT BY PITCH %d TIMES", hitByPitch));
    }

    return String.join(", ", summary);
  }

  private static Optional<String> addNumbers(Optional<String> maybeA, Optional<String> maybeB) {
    int a = maybeA.map(x -> NumberUtils.toInt(x)).orElse(0);
    int b = maybeB.map(x -> NumberUtils.toInt(x)).orElse(0);
    return Optional.of("%d".formatted(a + b));
  }
}
