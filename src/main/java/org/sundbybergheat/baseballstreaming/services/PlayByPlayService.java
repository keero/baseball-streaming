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
  private final int delay;

  private int currentPlay = 0;
  private int lastPlay = 0;

  public PlayByPlayService(
      final FilesService filesService,
      final WBSCPlayClient client,
      final String gameId,
      final RunMode runMode,
      final int delay) {
    this.filesService = filesService;
    this.client = client;
    this.gameId = gameId;
    this.runMode = runMode;
    this.delay = delay;
  }

  @Override
  public void run() {
    String playText = "";
    boolean waitingForGameToStart = false;
    while (!playText.startsWith("GAME OVER")) {
      try {
        Thread.sleep(delay);
        Optional<Integer> maybeLatestPlay = client.getLatestPlay(gameId);
        if (maybeLatestPlay.isEmpty()) {
          if (runMode.equals(RunMode.replay)) {
            LOG.error("Game {} not found, nothing to replay. Exiting.", gameId);
            return;
          }

          if (!waitingForGameToStart) {
            LOG.info("No game info available yet. Waiting for game {} to start.", gameId);
            waitingForGameToStart = true;
          }
          continue;
        }
        Optional<Play> play = Optional.empty();
        if (runMode.equals(RunMode.live)) {
          int nextKnownPlay = Math.max(maybeLatestPlay.get(), currentPlay);
          play = client.optimisticGetPlay(gameId, nextKnownPlay);
          int actualNextPlay = play.map(p -> p.playNumber()).orElse(currentPlay);
          if (currentPlay == actualNextPlay) {
            // No new play found
            continue;
          }
          currentPlay = play.map(p -> p.playNumber()).orElse(currentPlay);
        } else {
          if (currentPlay == lastPlay) {
            lastPlay = maybeLatestPlay.get();
            currentPlay = 0;
            LOG.info(
                "Replaying game {} with {} plays, updating with {} millisecond interval.",
                gameId,
                lastPlay,
                delay);
          }
          currentPlay += 1;
          play = client.getPlay(gameId, currentPlay);
        }
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
