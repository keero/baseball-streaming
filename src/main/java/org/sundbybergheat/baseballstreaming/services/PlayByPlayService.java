package org.sundbybergheat.baseballstreaming.services;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.clients.WBSCPlayClient;
import org.sundbybergheat.baseballstreaming.models.stats.StatsException;
import org.sundbybergheat.baseballstreaming.models.wbsc.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.PlayData;
import org.sundbybergheat.baseballstreaming.models.wbsc.WBSCException;

public class PlayByPlayService implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(PlayByPlayService.class);

  private final FilesService filesService;
  private final WBSCPlayClient client;
  private final String gameId;
  private final RunMode runMode;
  private final int updateInterval;

  private int currentPlay = 0;
  private int lastPlay = 0;

  public PlayByPlayService(
      final FilesService filesService,
      final WBSCPlayClient client,
      final String gameId,
      final RunMode runMode,
      final int updateInterval) {
    this.filesService = filesService;
    this.client = client;
    this.gameId = gameId;
    this.runMode = runMode;
    this.updateInterval = updateInterval;
  }

  @Override
  public void run() {
    String playText = "";
    while (!playText.startsWith("GAME OVER")) {
      try {
        Thread.sleep(updateInterval);
        if (runMode.equals(RunMode.live)) {
          int latestPlay = client.getLatestPlay(gameId);
          if (latestPlay == currentPlay) {
            LOG.info("No new play recorded yet (latest play # is {}).", latestPlay);
            continue;
          }
          currentPlay = latestPlay;
        } else {
          if (currentPlay == lastPlay) {
            lastPlay = client.getLatestPlay(gameId);
            currentPlay = 0;
            LOG.info(
                "Replaying game {} with {} plays, updating with {} millisecond interval.",
                gameId,
                lastPlay,
                updateInterval);
          }
          currentPlay += 1;
        }
        Optional<Play> play = client.getPlay(gameId, currentPlay);
        if (play.isEmpty()) {
          LOG.warn("Play # {} could not be found.", currentPlay);
          continue;
        }
        filesService.updatePlay(play.get());
        PlayData playData =
            play.get().playData().stream()
                .sorted((a, b) -> Integer.valueOf(b.atBat()) - Integer.valueOf(a.atBat()))
                .findFirst()
                .get();
        playText = playData.text();
        LOG.info(
            "Play # {} ({}): {}",
            currentPlay,
            Instant.ofEpochMilli(playData.timestamp()),
            playText);
      } catch (IOException | WBSCException | InterruptedException | StatsException e) {
        LOG.error("Something went wrong. {}", e.getMessage());
      }
    }
    LOG.info("Game over!");
    LOG.info("Thank you for using baseball-streaming!");
    LOG.info("We hope to see you again in the next game.");
  }
}
