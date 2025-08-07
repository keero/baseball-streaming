package org.sundbybergheat.baseballstreaming.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sundbybergheat.baseballstreaming.clients.FilesClient;
import org.sundbybergheat.baseballstreaming.clients.FilesClient.ImageIOHandler;
import org.sundbybergheat.baseballstreaming.models.JsonMapper;
import org.sundbybergheat.baseballstreaming.models.wbsc.play.Play;

public class FilesServiceTest {

  @Test
  void apa(@TempDir Path tempDir) throws IOException {
    ImageIOHandler imageIOHandler = mock(ImageIOHandler.class);
    when(imageIOHandler.read(any(URL.class)))
        .thenReturn(ImageIO.read(IOUtils.resourceToURL("/wbsc/default-player.jpg")));
    String playJson = IOUtils.resourceToString("/wbsc/first_play.json", StandardCharsets.UTF_8);
    FilesClient client = new FilesClient(tempDir.toString(), imageIOHandler);
    FilesService service = new FilesService(client);
    Play play = JsonMapper.fromJson(playJson, Play.class);
    service.updatePlay(play);
  }
}
