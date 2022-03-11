package org.sundbybergheat.baseballstreaming.clients;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStats;
import org.sundbybergheat.baseballstreaming.models.stats.BatterStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStats;
import org.sundbybergheat.baseballstreaming.models.stats.PitcherStatsImpl;
import org.sundbybergheat.baseballstreaming.models.stats.SeriesStats;

public class CareerStatsTools {
  public static BatterStats getCareerBattingStats(final Collection<SeriesStats> stats) {
    final List<BatterStats> batting =
        stats.stream()
            .filter(s -> s.batting().isPresent())
            .map(s -> s.batting().get())
            .collect(Collectors.toList());

    final int games = batting.stream().map(b -> b.games()).reduce((a, b) -> a + b).orElse(0);
    final int atBats = batting.stream().map(b -> b.atBats()).reduce((a, b) -> a + b).orElse(0);
    final int runs = batting.stream().map(b -> b.runs()).reduce((a, b) -> a + b).orElse(0);
    final int hits = batting.stream().map(b -> b.hits()).reduce((a, b) -> a + b).orElse(0);
    final int doubles = batting.stream().map(b -> b.doubles()).reduce((a, b) -> a + b).orElse(0);
    final int triples = batting.stream().map(b -> b.triples()).reduce((a, b) -> a + b).orElse(0);
    final int homeruns = batting.stream().map(b -> b.homeruns()).reduce((a, b) -> a + b).orElse(0);
    final int rbi = batting.stream().map(b -> b.runsBattedIn()).reduce((a, b) -> a + b).orElse(0);
    final int totalBases =
        batting.stream().map(b -> b.totalBases()).reduce((a, b) -> a + b).orElse(0);
    final int walks = batting.stream().map(b -> b.walks()).reduce((a, b) -> a + b).orElse(0);
    final int hitByPitch =
        batting.stream().map(b -> b.hitByPitch()).reduce((a, b) -> a + b).orElse(0);
    final int strikeouts =
        batting.stream().map(b -> b.strikeouts()).reduce((a, b) -> a + b).orElse(0);
    final int gdp =
        batting.stream().map(b -> b.groundoutDoublePlay()).reduce((a, b) -> a + b).orElse(0);
    final int sacFly =
        batting.stream().map(b -> b.sacrificeFlies()).reduce((a, b) -> a + b).orElse(0);
    final int sacHits =
        batting.stream().map(b -> b.sacrificeHits()).reduce((a, b) -> a + b).orElse(0);
    final int stolenBases =
        batting.stream().map(b -> b.stolenBases()).reduce((a, b) -> a + b).orElse(0);
    final int cs = batting.stream().map(b -> b.caughtStealing()).reduce((a, b) -> a + b).orElse(0);
    final int singles = hits - doubles - triples - homeruns;
    final double slugging = (singles + 2.0 * doubles + 3.0 * triples + 4.0 * homeruns) / atBats;
    final double onBasePercentage =
        (double) (hits + walks + hitByPitch) / (atBats + walks + hitByPitch + sacFly);

    final String avg =
        atBats > 0 ? String.format("%.3f", (double) hits / atBats).replace("0.", ".") : ".000";
    final String slg = atBats > 0 ? String.format("%.3f", slugging).replace("0.", ".") : ".000";
    final String obp =
        (atBats + walks + hitByPitch + sacFly) > 0
            ? String.format("%.3f", onBasePercentage).replace("0.", ".")
            : ".000";
    final String ops = String.format("%.3f", onBasePercentage + slugging).replace("0.", ".");

    return BatterStatsImpl.builder()
        .playerId("N/A")
        .teamId("N/A")
        .games(games)
        .atBats(atBats)
        .runs(runs)
        .hits(hits)
        .doubles(doubles)
        .triples(triples)
        .homeruns(homeruns)
        .runsBattedIn(rbi)
        .totalBases(totalBases)
        .walks(walks)
        .hitByPitch(hitByPitch)
        .strikeouts(strikeouts)
        .groundoutDoublePlay(gdp)
        .sacrificeFlies(sacFly)
        .sacrificeHits(sacHits)
        .stolenBases(stolenBases)
        .caughtStealing(cs)
        .battingAverage(avg)
        .slugging(slg)
        .onBasePercentage(obp)
        .onBasePercentagePlusSlugging(ops)
        .build();
  }

  private static double getInningsPitchedValue(final String inningsPitched) {
    String[] split = inningsPitched.split("\\.");
    return Double.parseDouble(split[0]) + (Double.parseDouble(split[1]) / 3.0);
  }

  public static PitcherStats getCareerPitchingStats(final Collection<SeriesStats> stats) {
    final List<PitcherStats> pitching =
        stats.stream()
            .filter(s -> s.pitching().isPresent())
            .map(s -> s.pitching().get())
            .collect(Collectors.toList());

    final int wins = pitching.stream().map(p -> p.wins()).reduce((a, b) -> a + b).orElse(0);
    final int losses = pitching.stream().map(p -> p.losses()).reduce((a, b) -> a + b).orElse(0);

    final double inningsPitchedVal =
        pitching.stream()
            .map(p -> getInningsPitchedValue(p.inningsPitched()))
            .reduce((a, b) -> a + b)
            .orElse(0.0);
    final int inningsPitchedIntVal = (int) inningsPitchedVal;
    final int inningsPitchedDecimalVal =
        (int) Math.round((inningsPitchedVal - inningsPitchedIntVal) * 3);

    final String inningsPitched =
        String.format("%d.%d", inningsPitchedIntVal, inningsPitchedDecimalVal);
    final int earnedRunsTimesGameLength =
        pitching.stream()
            .map(p -> p.gameLength() * p.earnedRunsAllowed())
            .reduce((a, b) -> a + b)
            .orElse(0);

    final double eraVal =
        inningsPitchedVal > 0.0 ? earnedRunsTimesGameLength / inningsPitchedVal : 0.0;
    final String era = String.format("%.2f", eraVal);

    final int appearances =
        pitching.stream().map(p -> p.appearances()).reduce((a, b) -> a + b).orElse(0);
    final int gamesStarted =
        pitching.stream().map(p -> p.gamesStarted()).reduce((a, b) -> a + b).orElse(0);
    final int saves = pitching.stream().map(p -> p.saves()).reduce((a, b) -> a + b).orElse(0);
    final int completeGames =
        pitching.stream().map(p -> p.completeGames()).reduce((a, b) -> a + b).orElse(0);
    final int shutouts = pitching.stream().map(p -> p.shutouts()).reduce((a, b) -> a + b).orElse(0);
    final int hitsAllowed =
        pitching.stream().map(p -> p.hitsAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int runsAllowed =
        pitching.stream().map(p -> p.runsAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int earnedRunsAllowed =
        pitching.stream().map(p -> p.earnedRunsAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int walksAllowed =
        pitching.stream().map(p -> p.walksAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int strikeouts =
        pitching.stream().map(p -> p.strikeouts()).reduce((a, b) -> a + b).orElse(0);
    final int doublesAllowed =
        pitching.stream().map(p -> p.doublesAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int triplesAllowed =
        pitching.stream().map(p -> p.triplesAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int homerunsAllowed =
        pitching.stream().map(p -> p.homerunsAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int atBats = pitching.stream().map(p -> p.atBats()).reduce((a, b) -> a + b).orElse(0);
    final String opponentBattingAverage =
        atBats > 0
            ? String.format("%.3f", (double) hitsAllowed / atBats).replace("0.", ".")
            : ".000";
    final int wildPitches =
        pitching.stream().map(p -> p.wildPitches()).reduce((a, b) -> a + b).orElse(0);
    final int hitByPitch =
        pitching.stream().map(p -> p.hitByPitch()).reduce((a, b) -> a + b).orElse(0);
    final int balks = pitching.stream().map(p -> p.balks()).reduce((a, b) -> a + b).orElse(0);
    final int sacrificeFliesAllowed =
        pitching.stream().map(p -> p.sacrificeFliesAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int sacrificeHitsAllowed =
        pitching.stream().map(p -> p.sacrificeHitsAllowed()).reduce((a, b) -> a + b).orElse(0);
    final int groundOuts =
        pitching.stream().map(p -> p.groundOuts()).reduce((a, b) -> a + b).orElse(0);
    final int flyOuts = pitching.stream().map(p -> p.flyOuts()).reduce((a, b) -> a + b).orElse(0);

    return PitcherStatsImpl.builder()
        .playerId("N/A")
        .teamId("N/A")
        .gameLength(-1)
        .wins(wins)
        .losses(losses)
        .era(era)
        .appearances(appearances)
        .gamesStarted(gamesStarted)
        .saves(saves)
        .completeGames(completeGames)
        .shutouts(shutouts)
        .inningsPitched(inningsPitched)
        .hitsAllowed(hitsAllowed)
        .runsAllowed(runsAllowed)
        .earnedRunsAllowed(earnedRunsAllowed)
        .walksAllowed(walksAllowed)
        .strikeouts(strikeouts)
        .doublesAllowed(doublesAllowed)
        .triplesAllowed(triplesAllowed)
        .homerunsAllowed(homerunsAllowed)
        .atBats(atBats)
        .opponentBattingAverage(opponentBattingAverage)
        .wildPitches(wildPitches)
        .hitByPitch(hitByPitch)
        .balks(balks)
        .sacrificeFliesAllowed(sacrificeFliesAllowed)
        .sacrificeHitsAllowed(sacrificeHitsAllowed)
        .groundOuts(groundOuts)
        .flyOuts(flyOuts)
        .build();
  }
}
