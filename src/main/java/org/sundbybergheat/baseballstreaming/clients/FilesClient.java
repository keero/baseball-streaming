package org.sundbybergheat.baseballstreaming.clients;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesClient {
  private static final Logger LOG = LoggerFactory.getLogger(FilesClient.class);

  private final String resourceBasePath;
  private final ImageIOHandler imageIOHandler;

  public FilesClient(final String resourceBasePath, final ImageIOHandler imageIOHandler) {
    this.resourceBasePath = resourceBasePath;
    this.imageIOHandler = imageIOHandler;
  }

  public String getResourceBasePath() {
    return resourceBasePath;
  }

  public void copyFileFromResource(final String resourcePath, final String to) throws IOException {
    copyFileFromURL(IOUtils.resourceToURL(resourcePath), to);
  }

  public void copyFileFromURL(final URL url, final String to) throws IOException {
    File target =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(to)));
    if (!target.exists()) {
      FileUtils.copyURLToFile(url, target, 30000, 30000);
      target.setLastModified(Instant.now().toEpochMilli());
    }
  }

  public void copyImageFromURL(final URL url, final String to) throws IOException {
    try {
      BufferedImage in = imageIOHandler.read(url);
      if (in != null) {
        writePng(in, to);
      } else {
        LOG.warn("Unable to parse content of image at {}", url);
      }
    } catch (RuntimeException e) {
      LOG.warn("Unable to parse content of image at {}", url, e);
    }
  }

  public void writePng(final BufferedImage image, final String to) throws IOException {
    File target =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(to)));
    if (!target.exists()) {
      FileUtils.createParentDirectories(target);
      imageIOHandler.write(image, "png", target);
    }
  }

  public void copyFile(final String from, final String to) throws IOException {
    File source =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(from)));
    File target =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(to)));

    if (!FileUtils.contentEquals(source, target)) {
      FileUtils.copyFile(source, target, StandardCopyOption.REPLACE_EXISTING);
      target.setLastModified(Instant.now().toEpochMilli());
    }
  }

  public void writeStringToFile(final String path, final String content) throws IOException {

    File file =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(path)));

    if (!file.exists()
        || !FileUtils.readFileToString(file, StandardCharsets.UTF_8).equals(content)) {
      FileUtils.write(file, content, StandardCharsets.UTF_8, false);
      file.setLastModified(Instant.now().toEpochMilli());
    }
  }

  public boolean fileExists(final String path) {
    return new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(path)))
        .exists();
  }

  public static class ImageIOHandler {
    public BufferedImage read(URL url) throws IOException {
      return ImageIO.read(url);
    }

    public boolean write(RenderedImage im, String formatName, File output) throws IOException {
      return ImageIO.write(im, formatName, output);
    }
  }
}
