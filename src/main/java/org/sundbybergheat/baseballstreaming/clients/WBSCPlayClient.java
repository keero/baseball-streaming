package org.sundbybergheat.baseballstreaming.clients;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;
import org.sundbybergheat.baseballstreaming.models.wbsc.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.WBSCException;

public class WBSCPlayClient {

  private static final String LATEST_URL = "%s/gamedata/%s/latest.json?_=%d";
  private static final String PLAY_URL = "%s/gamedata/%s/play%d.json?_=%d";
  private final OkHttpClient client;
  private final String baseUrl;

  public WBSCPlayClient(final OkHttpClient client, final String baseUrl) {
    this.client = client;
    this.baseUrl = baseUrl;
  }

  public int getLatestPlay(final String gameId) throws IOException, WBSCException {

    String uri = String.format(LATEST_URL, baseUrl, gameId, Instant.now().toEpochMilli());

    Request request = new okhttp3.Request.Builder().url(uri).get().build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body = response.body().byteString().utf8();
      response.close();
      return Integer.parseInt(body);
    }
    String responseString = response.toString();
    response.close();
    throw new WBSCException(String.format("Unexpected response from WBSC: %s", responseString));
  }

  public Optional<Play> getPlay(final String gameId, final int play)
      throws IOException, WBSCException {
    String uri = String.format(PLAY_URL, baseUrl, gameId, play, Instant.now().toEpochMilli());

    Request request = new okhttp3.Request.Builder().url(uri).get().build();

    Response response = client.newCall(request).execute();

    if (response.isSuccessful()) {
      String body = response.body().byteString().utf8();
      response.close();
      return Optional.of(JsonMapper.fromJson(body, Play.class));
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
