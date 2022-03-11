package org.sundbybergheat.baseballstreaming.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.models.stats.AllStats;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStats;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;
import org.sundbybergheat.baseballstreaming.models.wbsc.BoxScore;
import org.sundbybergheat.baseballstreaming.models.wbsc.BoxScoreImpl;
import org.sundbybergheat.baseballstreaming.models.wbsc.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.PlayData;

public class BatterTools {
  private static final Logger LOG = LoggerFactory.getLogger(BatterTools.class);

  public static BoxScore aggregatedBoxScore(final String playerId, final Play play) {

    return play.boxScore().entrySet().stream()
        .filter(kv -> kv.getKey().startsWith("10") || kv.getKey().startsWith("20"))
        .map(kv -> kv.getValue())
        .filter(bs -> playerId.equals(bs.playerId()))
        .reduce(
            (a, b) ->
                BoxScoreImpl.builder()
                    .firstName(a.firstName())
                    .lastName(a.lastName())
                    .name(a.name())
                    .teamId(a.teamId())
                    .teamCode(a.teamCode())
                    .image(a.image())
                    .inPlay(a.inPlay())
                    .position(
                        String.join(
                            "/",
                            Stream.concat(
                                    Arrays.stream(a.position().orElse("").split("/")),
                                    Arrays.stream(b.position().orElse("").split("/")))
                                .collect(Collectors.toSet())))
                    .reentry(a.reentry() + b.reentry())
                    .playerId(a.playerId())
                    .assists(a.assists() + b.assists())
                    .atBats(a.atBats().orElse(0) + b.atBats().orElse(0))
                    .caughtStealing(a.caughtStealing().orElse(0) + b.caughtStealing().orElse(0))
                    .doubles(a.doubles().orElse(0) + b.doubles().orElse(0))
                    .errors(a.errors() + b.errors())
                    .hits(a.hits().orElse(0) + b.hits().orElse(0))
                    .homeruns(a.homeruns().orElse(0) + b.homeruns().orElse(0))
                    .leftOnBase(a.leftOnBase().orElse(0) + b.leftOnBase().orElse(0))
                    .putOuts(a.putOuts() + b.putOuts())
                    .runs(a.runs().orElse(0) + b.runs().orElse(0))
                    .runsBattedIn(a.runsBattedIn().orElse(0) + b.runsBattedIn().orElse(0))
                    .stolenBases(a.stolenBases().orElse(0) + b.stolenBases().orElse(0))
                    .strikeOuts(a.strikeOuts().orElse(0) + b.strikeOuts().orElse(0))
                    .triples(a.triples().orElse(0) + b.triples().orElse(0))
                    .walks(a.walks().orElse(0) + b.walks().orElse(0))
                    .build())
        .get();
  }

  public static String batterNarrative(
      final BoxScore batter,
      final Map<String, AllStats> stats,
      final String seriesId,
      final Play play) {
    List<PlayData> batterPlays =
        play.playData().stream()
            .filter(
                pd ->
                    (pd.batter().startsWith("10") || pd.batter().startsWith("20"))
                        && batter.playerId().equals(play.boxScore().get(pd.batter()).playerId()))
            .sorted(
                (a, b) -> {
                  int atBatCompare = Integer.parseInt(b.atBat()) - Integer.parseInt(a.atBat());
                  if (atBatCompare != 0) {
                    return atBatCompare;
                  }
                  return Long.valueOf(b.timestamp() - a.timestamp()).intValue();
                })
            .collect(Collectors.toList());
    List<PlayData> plateApperances =
        batterPlays.stream()
            .filter(bp -> bp.text().toLowerCase().startsWith(batter.name().toLowerCase()))
            .collect(Collectors.toList());
    if (plateApperances.size() > 0) {
      return summaryOfGame(batter, plateApperances);
    }
    return summaryOfSeries(stats, batter.playerId(), seriesId);
  }

  public static String batterNarrative(
      final BoxScore batter, final SeriesStats stats, final Play play) {
    List<PlayData> batterPlays =
        play.playData().stream()
            .filter(
                pd ->
                    (pd.batter().startsWith("10") || pd.batter().startsWith("20"))
                        && batter.playerId().equals(play.boxScore().get(pd.batter()).playerId()))
            .sorted(
                (a, b) -> {
                  int atBatCompare = Integer.parseInt(b.atBat()) - Integer.parseInt(a.atBat());
                  if (atBatCompare != 0) {
                    return atBatCompare;
                  }
                  return Long.valueOf(b.timestamp() - a.timestamp()).intValue();
                })
            .collect(Collectors.toList());
    List<PlayData> plateApperances =
        batterPlays.stream()
            .filter(bp -> bp.text().toLowerCase().startsWith(batter.name().toLowerCase()))
            .collect(Collectors.toList());
    if (plateApperances.size() > 0) {
      return summaryOfGame(batter, plateApperances);
    }
    return summaryOfSeries(stats);
  }

  private static String summaryOfGame(final BoxScore batter, final List<PlayData> plateApperances) {
    List<String> summary = new ArrayList<String>();
    if (batter.atBats().orElse(0) > 0) {
      summary.add(String.format("%d for %d", batter.hits().orElse(0), batter.atBats().orElse(0)));

      if (batter.hits().orElse(0) > 0) {
        if (batter.homeruns().orElse(0) > 1) {
          summary.add(batter.homeruns().get() + " HOME RUNS");
        } else if (batter.homeruns().orElse(0) == 1) {
          summary.add("HOME RUN");
        }

        if (batter.triples().orElse(0) > 1) {
          summary.add(batter.triples().get() + " TRIPLES");
        } else if (batter.triples().orElse(0) == 1) {
          summary.add("TRIPLE");
        }

        if (batter.doubles().orElse(0) > 1) {
          summary.add(batter.doubles().get() + " DOUBLES");
        } else if (batter.doubles().orElse(0) == 1) {
          summary.add("DOUBLE");
        }

        int singles =
            batter.hits().orElse(0)
                - batter.homeruns().orElse(0)
                - batter.triples().orElse(0)
                - batter.doubles().orElse(0);

        if (singles > 1) {
          summary.add(singles + " SINGLES");
        } else if (singles == 1) {
          summary.add("SINGLE");
        }
      } else {
        long sacFlies =
            plateApperances.stream()
                .filter(pa -> pa.text().toLowerCase().contains("sacrifice fly"))
                .count();
        if (sacFlies > 1) {
          summary.add(sacFlies + " SAC FLIES");
        } else if (sacFlies == 1) {
          summary.add("SAC FLY");
        }

        long sacHits =
            plateApperances.stream()
                .filter(pa -> pa.text().toLowerCase().contains("sacrifice hit"))
                .count();
        if (sacHits > 1) {
          summary.add(sacHits + " SAC HITS");
        } else if (sacHits == 1) {
          summary.add("SAC HIT");
        }

        if (batter.strikeOuts().orElse(0) > 1) {
          summary.add(batter.strikeOuts().get() + " STRIKEOUTS");
        } else if (batter.strikeOuts().orElse(0) == 1) {
          summary.add("STRIKEOUT");
        }

        long flyOuts =
            plateApperances.stream()
                .filter(pa -> pa.text().toLowerCase().contains("flies out"))
                .count();
        if (flyOuts > 1) {
          summary.add(flyOuts + " FLYOUTS");
        } else if (flyOuts == 1) {
          summary.add("FLYOUT");
        }

        long groundOuts =
            plateApperances.stream()
                .filter(pa -> pa.text().toLowerCase().contains("grounds out"))
                .count();
        if (groundOuts > 1) {
          summary.add(groundOuts + " GROUNDOUTS");
        } else if (groundOuts == 1) {
          summary.add("GROUNDOUT");
        }

        long gdp =
            plateApperances.stream()
                .filter(pa -> pa.text().toLowerCase().contains("hits into a double play"))
                .count();
        if (gdp > 1) {
          summary.add(gdp + " GDP");
        } else if (gdp == 1) {
          summary.add("GDP");
        }
      }
    }

    if (batter.walks().orElse(0) > 1) {
      summary.add(batter.walks().get() + " WALKS");
    } else if (batter.walks().orElse(0) == 1) {
      summary.add("WALK");
    }

    long hbp =
        plateApperances.stream()
            .filter(pa -> pa.text().toLowerCase().contains("hit by pitch"))
            .count();

    if (hbp > 0) {
      summary.add("HIT BY PITCH");
    }

    long roe =
        plateApperances.stream()
            .filter(pa -> pa.text().matches(".*reaches on [ a-z]*error.*"))
            .count();
    if (roe > 0) {
      summary.add("REACH ON ERROR");
    }

    long fc =
        plateApperances.stream()
            .filter(pa -> pa.text().toLowerCase().contains("reaches on fielders choice"))
            .count();
    if (fc > 0) {
      summary.add("FIELDER'S CHOICE");
    }

    if (batter.stolenBases().orElse(0) > 1) {
      summary.add(batter.stolenBases().get() + " STOLEN BASES");
    } else if (batter.stolenBases().orElse(0) == 1) {
      summary.add("STOLEN BASE");
    }

    if (summary.size() > 4) {
      summary = summary.subList(0, 4);
    }

    if (batter.runsBattedIn().orElse(0) > 1) {
      summary.add(batter.runsBattedIn().get() + " RBI");
    } else if (batter.runsBattedIn().orElse(0) == 1) {
      summary.add("RBI");
    }

    if (batter.runs().orElse(0) > 1) {
      summary.add(batter.runs().get() + " RUNS");
    } else if (batter.runs().orElse(0) == 1) {
      summary.add("RUN");
    }
    return String.join(", ", summary);
  }

  private static String summaryOfSeries(
      final Map<String, AllStats> stats, final String playerId, final String seriesId) {

    return summaryOfSeries(stats.get(playerId).seriesStats().get(seriesId));
  }

  private static String summaryOfSeries(final SeriesStats seriesStats) {
    if (seriesStats.batting().isEmpty()) {
      return "";
    }

    BatterStats stats = seriesStats.batting().get();

    Integer atBats = stats.atBats();

    List<String> summary = new ArrayList<String>();
    Integer xBaseHits = stats.homeruns() + stats.triples() + stats.doubles();
    if (xBaseHits > 1 && xBaseHits.doubleValue() / atBats.doubleValue() > 0.2) {
      summary.add(String.format("%d extra base hits", xBaseHits));
    }

    Integer walksAndHitByPitch = stats.walks() + stats.hitByPitch();
    if (walksAndHitByPitch > 4 && walksAndHitByPitch.doubleValue() > atBats.doubleValue() * 0.5) {
      summary.add(
          String.format(
              "%salked/hit by pitch %d times", summary.isEmpty() ? "W" : "w", walksAndHitByPitch));
    }

    Integer stolenBases = stats.stolenBases();
    Integer plateAppearances = atBats + walksAndHitByPitch;
    if (stolenBases > 2 && stolenBases.doubleValue() / plateAppearances.doubleValue() > 0.2) {
      summary.add(String.format("%d stolen bases", stolenBases));
    }

    if (summary.size() > 0) {
      return String.join(", ", summary);
    }

    Long onBase = Math.round(Double.parseDouble(stats.onBasePercentage()) * 100.0);
    if (onBase > 30L) {
      return String.format("Gets on base %d%% of the time", onBase);
    }

    return String.format("%d at bats in %d games", atBats, stats.games());
  }
}
