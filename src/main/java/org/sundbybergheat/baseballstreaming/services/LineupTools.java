package org.sundbybergheat.baseballstreaming.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Player;

public class LineupTools {

  public static Player getAwayPitcher(final Play play) {
    return getPitcher(play, "190");
  }

  public static Player getHomePitcher(final Play play) {
    return getPitcher(play, "290");
  }

  private static Player getPitcher(final Play play, final String prefix) {
    return play.boxScore().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(prefix))
        .sorted((a, b) -> Integer.parseInt(b.getKey()) - Integer.parseInt(a.getKey()))
        .map(entry -> entry.getValue())
        .findFirst()
        .orElseThrow();
  }

  public static List<Player> getAwayLineup(final Play play) {
    return getLineup(play, 101);
  }

  public static List<Player> getHomeLineup(final Play play) {
    return getLineup(play, 201);
  }

  private static List<Player> getLineup(final Play play, final int prefixBase) {
    List<Player> result = new ArrayList<Player>(9);
    for (int prefix = prefixBase; prefix < prefixBase + 9; prefix += 1) {
      final String strPrefix = Integer.toString(prefix);
      final Optional<String> curr =
          play.boxScore().keySet().stream()
              .filter(key -> key.startsWith(strPrefix))
              .sorted((a, b) -> Integer.parseInt(b) - Integer.parseInt(a))
              .findFirst();
      curr.ifPresent(c -> result.add(play.boxScore().get(c)));
    }
    return result;
  }
}
