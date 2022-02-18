package org.sundbybergheat.baseballstreaming;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import okhttp3.OkHttpClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sundbybergheat.baseballstreaming.clients.FilesClient;
import org.sundbybergheat.baseballstreaming.clients.StatsClient;
import org.sundbybergheat.baseballstreaming.clients.WBSCPlayClient;
import org.sundbybergheat.baseballstreaming.services.FilesService;
import org.sundbybergheat.baseballstreaming.services.PlayByPlayService;
import org.sundbybergheat.baseballstreaming.services.RunMode;

/** Hello world! */
public class App {
  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) throws IOException {

    Options options = new Options();

    options.addOption(
        "t", "target", true, "Target directory for the output. E.g., '~/obs/resources'");
    options.addOption(
        "S",
        "stats-base-url",
        true,
        "Base url where to fetch stats from. Defaults to 'https://www.wbsceurope.org'");
    options.addOption(
        "P",
        "plays-base-url",
        true,
        "Base url where to fetch plays from. Defaults to 'https://game.wbsc.org'");
    options.addOption("s", "series", true, "Series ID. E.g., '2021-juniorserien-baseboll'");
    options.addOption("g", "game", true, "Game ID. E.g., '84917'");
    options.addOption("c", "config-file", true, "Path to config file");
    options.addOption(
        "m", "run-mode", true, "Run mode. Either 'live' or 'replay'. Defaults to 'live'");
    options.addOption("h", "help", false, "Print this help section");

    HelpFormatter formatter = new HelpFormatter();

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      formatter.printHelp("baseball-streaming", options);
      return;
    }

    if (cmd.hasOption("h")) {
      formatter.printHelp("baseball-streaming", options);
      return;
    }

    String target = null;
    String seriesId = null;
    String gameId = null;
    String statsBaseUrl = null;
    String playsBaseUrl = null;
    String runMode = RunMode.live.toString();
    if (cmd.hasOption("c")) {
      Properties properties = new Properties();
      String fileName = cmd.getOptionValue("c");
      try (FileInputStream fis = new FileInputStream(fileName)) {
        properties.load(fis);
      } catch (IOException e) {
        System.err.println(e.getMessage());
        formatter.printHelp("baseball-streaming", options);
        return;
      }

      target = properties.getProperty("target");
      seriesId = properties.getProperty("series");
      gameId = properties.getProperty("game");
      statsBaseUrl = properties.getProperty("stats-base-url", "https://www.wbsceurope.org");
      playsBaseUrl = properties.getProperty("plays-base-url", "https://game.wbsc.org");
      runMode = properties.getProperty("run-mode", RunMode.live.toString());
    } else {
      target = cmd.getOptionValue("t");
      seriesId = cmd.getOptionValue("s");
      gameId = cmd.getOptionValue("g");
      statsBaseUrl = cmd.getOptionValue("S", "https://www.wbsceurope.org");
      playsBaseUrl = cmd.getOptionValue("P", "https://game.wbsc.org");
      runMode = cmd.getOptionValue("m", RunMode.live.toString());
    }

    if (target == null) {
      System.err.println("Missing required config/option: target");
      formatter.printHelp("baseball-streaming", options);
      return;
    }
    if (seriesId == null) {
      System.err.println("Missing required config/option: series");
      formatter.printHelp("baseball-streaming", options);
      return;
    }
    if (gameId == null) {
      System.err.println("Missing required config/option: game");
      formatter.printHelp("baseball-streaming", options);
      return;
    }
    if (!RunMode.live.toString().equals(runMode) && !RunMode.replay.toString().equals(runMode)) {
      System.err.println(
          String.format(
              "Illegal run-mode value: expected '%s' or '%s'", RunMode.live, RunMode.replay));
      return;
    }

    OkHttpClient okHttpClient = new OkHttpClient();
    FilesClient filesClient = new FilesClient(target);
    StatsClient statsClient = new StatsClient(okHttpClient, statsBaseUrl);
    FilesService filesService = new FilesService(filesClient, statsClient, seriesId);
    WBSCPlayClient wbscPlayClient = new WBSCPlayClient(okHttpClient, playsBaseUrl);

    filesService.initResources();

    PlayByPlayService playByPlayService =
        new PlayByPlayService(filesService, wbscPlayClient, gameId, RunMode.valueOf(runMode), 2000);

    playByPlayService.run();
  }
}
