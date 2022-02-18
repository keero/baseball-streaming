package org.sundbybergheat.baseballstreaming.services;

import java.util.ArrayList;
import java.util.List;
import org.sundbybergheat.baseballstreaming.models.wbsc.BoxScore;
import org.sundbybergheat.baseballstreaming.models.wbsc.Play;

public class LineupTools {

  public static BoxScore getAwayPitcher(final Play play) {
    return getPitcher(play, "190");
  }

  public static BoxScore getHomePitcher(final Play play) {
    return getPitcher(play, "290");
  }

  private static BoxScore getPitcher(final Play play, final String prefix) {
    return play.boxScore().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(prefix))
        .sorted((a, b) -> Integer.parseInt(b.getKey()) - Integer.parseInt(a.getKey()))
        .map(entry -> entry.getValue())
        .findFirst()
        .orElseThrow();
  }

  public static List<BoxScore> getAwayLineup(final Play play) {
    return getLineup(play, 101);
  }

  public static List<BoxScore> getHomeLineup(final Play play) {
    return getLineup(play, 201);
  }

  private static List<BoxScore> getLineup(final Play play, final int prefixBase) {
    List<BoxScore> result = new ArrayList<BoxScore>(9);
    for (int prefix = prefixBase; prefix < prefixBase + 9; prefix += 1) {
      final String strPrefix = Integer.toString(prefix);
      final String curr =
          play.boxScore().keySet().stream()
              .filter(key -> key.startsWith(strPrefix))
              .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
              .findFirst()
              .orElseThrow();
      result.add(play.boxScore().get(curr));
    }
    return result;
  }
}
