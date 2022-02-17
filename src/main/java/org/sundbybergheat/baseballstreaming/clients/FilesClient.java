package org.sundbybergheat.baseballstreaming.clients;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesClient {
  private static final Logger LOG = LoggerFactory.getLogger(FilesClient.class);

  private final String resourceBasePath;

  public FilesClient(final String resourceBasePath) {
    this.resourceBasePath = resourceBasePath;
  }

  public void copyFileFromResource(final String resourcePath, final String to) throws IOException {
    copyFileFromURL(IOUtils.resourceToURL(resourcePath), to);
  }

  public void copyFileFromURL(final URL url, final String to) throws IOException {
    File target =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(to)));
    if (!target.exists()) {
      FileUtils.copyURLToFile(url, target, 5000, 5000);
    }
  }

  public void copyFile(final String from, final String to) throws IOException {
    File source =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(from)));
    File target =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(to)));

    if (!FileUtils.contentEquals(source, target)) {
      FileUtils.copyFile(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  public void writeStringToFile(final String path, final String content) throws IOException {

    File file =
        new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(path)));

    if (!file.exists()
        || !FileUtils.readFileToString(file, StandardCharsets.UTF_8).equals(content)) {
      FileUtils.write(file, content, StandardCharsets.UTF_8, false);
    }
  }

  public boolean fileExists(final String path) {
    return new File(FilenameUtils.concat(resourceBasePath, FilenameUtils.separatorsToSystem(path)))
        .exists();
  }
}
