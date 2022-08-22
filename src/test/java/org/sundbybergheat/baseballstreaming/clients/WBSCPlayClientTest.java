package org.sundbybergheat.baseballstreaming.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sundbybergheat.baseballstreaming.models.wbsc.Play;
import org.sundbybergheat.baseballstreaming.models.wbsc.WBSCException;

public class WBSCPlayClientTest {

  OkHttpClient mockClient;
  Call mockCall;

  @BeforeEach
  void init() {
    mockClient = mock(OkHttpClient.class);
    mockCall = mock(Call.class);
    when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
  }

  @Test
  void shouldGetLatestPlay() throws IOException, WBSCException {

    when(mockCall.execute())
        .thenReturn(
            new Response.Builder()
                .request(new Request.Builder().url("https://game.wbsc.org").build())
                .code(200)
                .protocol(Protocol.HTTP_1_0)
                .message("arg0")
                .body(ResponseBody.create("123", MediaType.parse("plain/text")))
                .build());

    WBSCPlayClient wbscPlayClient = new WBSCPlayClient(mockClient, "https://game.wbsc.org/");

    int latestPlay = wbscPlayClient.getLatestPlay("83353").orElseThrow();

    assertEquals(123, latestPlay);
  }

  @Test
  void shouldGetPlay() throws IOException, WBSCException {
    String resourceToString = IOUtils.resourceToString("/wbsc/playXX.json", StandardCharsets.UTF_8);
    when(mockCall.execute())
        .thenReturn(
            new Response.Builder()
                .request(new Request.Builder().url("https://game.wbsc.org").build())
                .code(200)
                .protocol(Protocol.HTTP_1_0)
                .message("arg0")
                .body(ResponseBody.create(resourceToString, MediaType.parse("application/json")))
                .build());

    WBSCPlayClient wbscPlayClient = new WBSCPlayClient(mockClient, "https://game.wbsc.org/");

    Optional<Play> play = wbscPlayClient.getPlay("83353", 1);

    assertTrue(play.isPresent());
  }
}
