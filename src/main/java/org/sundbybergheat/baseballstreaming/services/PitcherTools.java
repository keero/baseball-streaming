package org.sundbybergheat.baseballstreaming.services;

import org.apache.commons.lang3.math.NumberUtils;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Player;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayerSeasonStats;

public class PitcherTools {
  public static String pitcherNarrative(final Player pitcher, final int gameLength) {

    double inningsPitchedVal = inningsVal(pitcher.inningsPitched().orElse("0.0"));
    if (inningsPitchedVal > 2.0) {
      return summaryOfGame(pitcher);
    }
    return pitcher
        .seasonStats()
        .map(ss -> summaryOfSeason(ss, gameLength))
        .orElse("No stats available yet.");
  }

  public static String pitcherNarrativeTitle(final Player pitcher) {
    double inningsPitchedVal = inningsVal(pitcher.inningsPitched().orElse("0.0"));
    if (inningsPitchedVal > 2.0) {
      return "In This Game";
    }
    return "This Season";
  }

  public static String inningsVal(final double inningsPitched) {
    Double whole = Math.floor(inningsPitched);
    Long rest = Math.round((inningsPitched - whole) * 3.0);
    return "%d.%d".formatted(whole.intValue(), rest);
  }

  public static double inningsVal(String inningsPitched) {
    String[] split = inningsPitched.split("\\.");
    double inningsPitchedVal = Double.parseDouble(split[0]) + Double.parseDouble(split[1]) / 3.0;
    return inningsPitchedVal;
  }

  private static String summaryOfSeason(final PlayerSeasonStats stats, final int gameLength) {
    final Integer earnedRuns = NumberUtils.toInt(stats.earnedRuns().orElse("0"));
    final Integer outs = NumberUtils.toInt(stats.outs().orElse("0"));
    final Integer walks = NumberUtils.toInt(stats.pitcherWalks().orElse("0"));
    final Integer strikeouts = NumberUtils.toInt(stats.pitcherStrikeouts().orElse("0"));

    if (earnedRuns == 0 && outs == 0 && walks == 0 && strikeouts == 0) {
      return "First appearance";
    }

    double innings = outs / 3.0;
    double era = earnedRuns / (innings / gameLength);
    double k9 = (strikeouts / innings) * 9.0;
    return "%s IP  %s ERA  %s K/9  %d BB"
        .formatted(
            inningsVal(innings),
            String.format("%.2f", era).replace(",", "."),
            String.format("%.2f", k9).replace(",", "."),
            walks);
  }

  private static String summaryOfGame(Player pitcher) {
    return "%s IP  %s H  %s R  %s ER  %s BB  %s K"
        .formatted(
            pitcher.inningsPitched(),
            pitcher.pitcherHits(),
            pitcher.pitcherRuns(),
            pitcher.earnedRuns(),
            pitcher.pitcherWalks(),
            pitcher.pitcherStrikeouts());
  }
}
