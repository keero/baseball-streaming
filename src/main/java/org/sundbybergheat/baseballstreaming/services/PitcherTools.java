package org.sundbybergheat.baseballstreaming.services;

import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;
import org.sundbybergheat.baseballstreaming.models.wbsc.BoxScore;

public class PitcherTools {
  public static String pitcherNarrative(final BoxScore pitcher, final SeriesStats stats) {

    double inningsPitchedVal = inningsVal(pitcher.inningsPitched().orElse("0.0"));
    if (inningsPitchedVal > 2.0) {
      return summaryOfGame(pitcher);
    }
    return summaryOfSeries(stats);
  }

  public static String pitcherNarrativeTitle(final BoxScore pitcher) {
    double inningsPitchedVal = inningsVal(pitcher.inningsPitched().orElse("0.0"));
    if (inningsPitchedVal > 2.0) {
      return "In This Game";
    }
    return "This Season";
  }

  private static double inningsVal(String inningsPitched) {
    String[] split = inningsPitched.split("\\.");
    double inningsPitchedVal = Double.parseDouble(split[0]) + Double.parseDouble(split[1]) / 3.0;
    return inningsPitchedVal;
  }

  private static String summaryOfSeries(final SeriesStats seriesStats) {
    if (seriesStats.pitching().isEmpty()) {
      return "First appearance";
    }

    PitcherStats stats = seriesStats.pitching().get();
    double innings = inningsVal(stats.inningsPitched());
    double whip = (stats.hitsAllowed() + stats.walksAllowed()) / innings;
    double k9 = (stats.strikeouts() / innings) * 9.0;
    return String.format(
        "%s IP  %s ERA  %s WHIP  %s K/9",
        stats.inningsPitched(),
        stats.era(),
        String.format("%.2f", whip).replace(",", "."),
        String.format("%.2f", k9).replace(",", "."));
  }

  private static String summaryOfGame(BoxScore pitcher) {
    return String.format(
        "%s IP  %d H  %d R  %d ER  %d BB  %s K",
        pitcher.inningsPitched().orElse("0.0"),
        pitcher.pitcherHits().orElse(0),
        pitcher.pitcherRuns().orElse(0),
        pitcher.earnedRuns().orElse(0),
        pitcher.pitcherWalks().orElse(0),
        pitcher.pitcherStrikeouts().orElse(0));
  }
}
