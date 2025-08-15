package org.sundbybergheat.baseballstreaming.clients;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;
import org.sundbybergheat.baseballstreaming.models.wbsc.WBSCException;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.PlayWrapper;

public class WBSCPlayClient {
  private static final Logger LOG = LoggerFactory.getLogger(WBSCPlayClient.class);

  private static final String HTTP_CLIENT_USER_AGENT =
      "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36";

  private static final String LATEST_URL = "%s/gamedata/%s/latest.json?_=%d";
  private static final String PLAY_URL = "%s/gamedata/%s/play%d.json?_=%d";
  private final OkHttpClient client;
  private final String baseUrl;

  public WBSCPlayClient(final OkHttpClient client, final String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  public Optional<Integer> getLatestPlay(final String gameId) throws IOException, WBSCException {

    String uri = String.format(LATEST_URL, baseUrl, gameId, Instant.now().toEpochMilli());

    Request request =
        new okhttp3.Request.Builder()
            .url(uri)
            .addHeader("user-agent", HTTP_CLIENT_USER_AGENT)
            .get()
            .build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body = response.body().byteString().utf8();
      response.close();
      return Optional.of(Integer.parseInt(body));
    }
    if (response.code() == 404) {
      // Game is not ready yet
      response.close();
      return Optional.empty();
    }
    String responseString = response.toString();
    response.close();
    throw new WBSCException(String.format("Unexpected response from WBSC: %s", responseString));
  }

  public Optional<PlayWrapper> optimisticGetPlay(final String gameId, final int play)
      throws IOException, WBSCException {
    for (int i = 2; i > 0; i -= 1) {
      try {
        Optional<PlayWrapper> maybePlay = getPlay(gameId, play + i);
        if (maybePlay.isPresent()) {
          LOG.info("Successfully obtained play {} ahead of {}.", play + i, play);
          return maybePlay;
        }
      } catch (IOException | WBSCException e) {
        // Do nothing
      }
    }
    return getPlay(gameId, play);
  }

  public Optional<PlayWrapper> getPlay(final String gameId, final int play)
      throws IOException, WBSCException {
    String uri = String.format(PLAY_URL, baseUrl, gameId, play, Instant.now().toEpochMilli());

    Request request =
        new okhttp3.Request.Builder()
            .url(uri)
            .addHeader("user-agent", HTTP_CLIENT_USER_AGENT)
            .get()
            .build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body = response.body().byteString().utf8();
      response.close();
      return Optional.of(new PlayWrapper(play, JsonMapper.fromJson(body, Play.class)));
    }

    if (response.code() == 404) {
      response.close();
      return Optional.empty();
    }

    String responseString = response.toString();
    response.close();

    throw new WBSCException(String.format("Unexpected response from WBSC: %s", responseString));
  }
}
