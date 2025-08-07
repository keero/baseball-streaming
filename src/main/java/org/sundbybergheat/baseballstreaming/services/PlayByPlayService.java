package org.sundbybergheat.baseballstreaming.services;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.clients.WBSCPlayClient;
import org.sundbybergheat.baseballstreaming.models.wbsc.WBSCException;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayData;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayWrapper;

public class PlayByPlayService implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(PlayByPlayService.class);

  private final FilesService filesService;
  private final WBSCPlayClient client;
  private final String gameId;
  private final RunMode runMode;
  private final int delay;

  private int currentPlay = 0;
  private int latestHandledPlay = 0;
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
        Optional<PlayWrapper> play = Optional.empty();
        if (runMode.equals(RunMode.live)) {
          int nextPlay = Math.max(maybeLatestPlay.get(), currentPlay);
          play = client.optimisticGetPlay(gameId, nextPlay);
          if (currentPlay == play.map(p -> p.number()).orElse(currentPlay)) {
            continue;
          }
          currentPlay = play.map(p -> p.number()).orElse(currentPlay);
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
        filesService.updatePlay(play.get().play());
        PlayData playData =
            play.get().play().playData().stream()
                .sorted(
                    (a, b) ->
                        b.atBat().map(ab -> Integer.valueOf(ab)).orElse(0)
                            - a.atBat().map(ab -> Integer.valueOf(ab)).orElse(0))
                .findFirst()
                .get();
        playText = playData.text().orElse("");
        LOG.info(
            "Play # {} ({}): {}",
            currentPlay,
            playData.timestamp().map(t -> Instant.ofEpochMilli(Long.parseLong(t))).orElse(null),
            playText);
      } catch (IOException | WBSCException | InterruptedException e) {
        LOG.error("Something went wrong. {}", e.getMessage());
      }
    }
    LOG.info("Game over!");
    LOG.info("Thank you for using baseball-streaming!");
    LOG.info("We hope to see you again in the next game.");
  }
}
