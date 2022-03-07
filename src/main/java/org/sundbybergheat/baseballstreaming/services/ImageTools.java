package org.sundbybergheat.baseballstreaming.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;

public class ImageTools {

  private static TypeReference<Map<String, String>> MAP_TYPE =
      new TypeReference<Map<String, String>>() {};

  public static Map<String, BufferedImage> getTeamColors()
      throws JsonMappingException, JsonProcessingException, IOException {

    final Map<String, String> teamColors =
        new ObjectMapper()
            .readValue(
                IOUtils.resourceToString("/team_colors.json", StandardCharsets.UTF_8), MAP_TYPE);
    return teamColors.entrySet().stream()
        .map(
            entry -> {
              BufferedImage imageOut = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
              imageOut.setRGB(0, 0, Integer.parseInt(entry.getValue(), 16));
              return new SimpleEntry<String, BufferedImage>(entry.getKey(), imageOut);
            })
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
