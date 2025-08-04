package org.sundbybergheat.baseballstreaming.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayData;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Player;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayerSeasonStats;

public class BatterTools {
  public static Player aggregatedBoxScore(final String playerId, final Play play) {

    return play.boxScore().entrySet().stream()
        .filter(kv -> kv.getKey().startsWith("10") || kv.getKey().startsWith("20"))
        .map(kv -> kv.getValue())
        .filter(bs -> playerId.equals(bs.playerId()))
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
                    a.reEntry() + b.reEntry(),
                    String.join(
                        "/",
                        Stream.concat(
                                Arrays.stream(a.position().split("/")),
                                Arrays.stream(b.position().split("/")))
                            .collect(Collectors.toSet())),
                    a.plateAppearances() + b.plateAppearances(),
                    a.atBats() + b.atBats(),
                    a.runs() + b.runs(),
                    a.hits() + b.hits(),
                    a.runsbattedIn() + b.runsbattedIn(),
                    a.walks() + b.walks(),
                    a.strikeouts() + b.strikeouts(),
                    a.doubles() + b.doubles(),
                    a.triples() + b.triples(),
                    a.homeruns() + b.homeruns(),
                    a.sacFlies() + b.sacFlies(),
                    a.hitByPitch() + b.hitByPitch(),
                    a.stolenBases() + b.stolenBases(),
                    a.caughtStealing() + b.caughtStealing(),
                    a.seasonStats(),
                    a.leftOnBase() + b.leftOnBase(),
                    null,
                    null,
                    null,
                    null,
                    a.putOuts() + b.putOuts(),
                    a.assists() + b.assists(),
                    a.errors() + b.errors(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null))
        .get();
  }

  private static List<PlayData> plateAppearances(final Player batter, final Play play) {
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
                  return Long.valueOf(Long.parseLong(b.timestamp()) - Long.parseLong(a.timestamp()))
                      .intValue();
                })
            .collect(Collectors.toList());
    List<PlayData> plateApperances =
        batterPlays.stream()
            .filter(bp -> bp.text().toLowerCase().startsWith(batter.fullName().toLowerCase()))
            .collect(Collectors.toList());
    return plateApperances;
  }

  public static String batterNarrative(final Player batter, final Play play) {
    List<PlayData> plateApperances = plateAppearances(batter, play);
    if (Optional.ofNullable(batter.plateAppearances()).orElse(0) > 0) {
      return summaryOfGame(batter, plateApperances);
    }
    return summaryOfSeason(batter.seasonStats());
  }

  public static String batterNarrativeTitle(final Player batter) {
    return Optional.ofNullable(batter.plateAppearances()).orElse(0) > 0 ? "In This Game" : "This Season";
  }

  private static String summaryOfGame(final Player batter, final List<PlayData> plateApperances) {

    if (batter.plateAppearances() == 0) {
      return "First plate appearance";
    }

    List<String> summary = new ArrayList<String>();
    if (batter.atBats() > 0) {
      summary.add(String.format("%s for %s", batter.hits(), batter.atBats()));

      if (batter.hits() > 0) {
        if (batter.homeruns() > 1) {
          summary.add(batter.homeruns() + " HOME RUNS");
        } else if (batter.homeruns() == 1) {
          summary.add("HOME RUN");
        }

        if (batter.triples() > 1) {
          summary.add(batter.triples() + " TRIPLES");
        } else if (batter.triples() == 1) {
          summary.add("TRIPLE");
        }

        if (batter.doubles() > 1) {
          summary.add(batter.doubles() + " DOUBLES");
        } else if (batter.doubles() == 1) {
          summary.add("DOUBLE");
        }

        int singles = batter.hits() - batter.homeruns() - batter.triples() - batter.doubles();

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

        if (batter.strikeouts() > 1) {
          summary.add(batter.strikeouts() + " STRIKEOUTS");
        } else if (batter.strikeouts() == 1) {
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

    if (batter.walks() > 1) {
      summary.add(batter.walks() + " WALKS");
    } else if (batter.walks() == 1) {
      summary.add("WALK");
    }

    if (batter.hitByPitch() > 0) {
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

    if (batter.stolenBases() > 1) {
      summary.add(batter.stolenBases() + " STOLEN BASES");
    } else if (batter.stolenBases() == 1) {
      summary.add("STOLEN BASE");
    }

    if (summary.size() > 4) {
      summary = summary.subList(0, 4);
    }

    if (batter.runsbattedIn() > 1) {
      summary.add(batter.runsbattedIn() + " RBI");
    } else if (batter.runsbattedIn() == 1) {
      summary.add("RBI");
    }

    if (batter.runs() > 1) {
      summary.add(batter.runs() + " RUNS");
    } else if (batter.runs() == 1) {
      summary.add("RUN");
    }
    if (summary.size() > 0) {
      return String.join(", ", summary);
    }

    return "";
  }

  private static String summaryOfSeason(final PlayerSeasonStats stats) {
    final Integer plateAppearances = Integer.parseInt(stats.plateAppearances());
    final Integer atBats = Integer.parseInt(stats.atBats());
    final Integer hits = Integer.parseInt(stats.hits());
    final Integer walks = Integer.parseInt(stats.walks());
    final Integer doubles = Integer.parseInt(stats.doubles());
    final Integer triples = Integer.parseInt(stats.triples());
    final Integer homeruns = Integer.parseInt(stats.homeruns());
    final Integer hitByPitch = Integer.parseInt(stats.hitByPitch());

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
}
