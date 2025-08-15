# baseball-streaming
Java CLI application for fetching live data from WBSC game data (Ballclubz) and continuously write text and image files to disk.
The produced files can then be consumed by, for instance, OBS Studio to produce overlays for a live stream.

> **Version >= 2.0.0 only relies on game data ([example](https://game.wbsc.org/gamedata/158979/play123.json)), no stats are scraped from websites anylonger.**

## Download

> **Make sure you have Java JDK 21 installed on your computer. OpenJDK 21 for windows may be downloaded [here](https://docs.microsoft.com/en-us/java/openjdk/download)**

Download the desired release jar from [the release page](https://github.com/keero/baseball-streaming/releases)


## Run the application

Run the application by opening a terminal (or powershell if you do Windows) and run:

```bash
java -jar <path-to-the-downloaded-jar> -h
```

(replace `<path-to-the-downloaded-jar>` with the path to the jar file you downloaded before)
This will show you the options expected by the appliaction.

```
usage: baseball-streaming
 -c,--config-file <arg>      Path to config file
 -d,--delay <arg>            Delay (milliseconds) between fetching plays.
                             Defaults to 500 for live and 3000 for replay.
 -g,--game <arg>             Game ID. E.g., '84917'
 -h,--help                   Print this help section
 -m,--run-mode <arg>         Run mode. Either 'live' or 'replay'. Defaults
                             to 'live'
 -P,--plays-base-url <arg>   Base url where to fetch plays from. Defaults
                             to 'https://game.wbsc.org'
 -t,--target <arg>           Target directory for the output. E.g.,
                             '~/obs/resources'
```


You may either create a config file (more on that further down) or provide the expected options directly to the application as option arguments.

The `-t` option specifies the output directory where the output text and image files will be rendered.
The `-g` option specifies which game you are streaming. This is typically a 5 digit number such as `84908`

Assuming you want to stream the game with id `84908` to the directory `C:/obs-studio/resources`, then you issue the follwoing command:

```bash
java -jar <path-to-the-downloaded-jar> -t C:/obs-studio/resources -g 84908
```

If you don't know the game id you can find the on the game page at for instance https://www.wbsceurope.org/ or https://stats.baseboll-softboll.se/

For instance the Swedish 2021 Championship final game can be found @ [https://www.wbsceurope.org/en/events/2021-elitserien-baseboll/schedule-and-results/box-score/**84908**](https://www.wbsceurope.org/en/events/2021-elitserien-baseboll/schedule-and-results/box-score/84908)

The game id can be found in the URL (marked with bold above).

The application will output what is currently happening live. If you want to test it out with no actual game being played at the moment you can use the `replay` mode by including the option `-m replay` in the command.

## The text and image files produced in the target directory

A batter directory consists of the following files:
```
.
├── avg.txt
├── batting-title.txt
├── batting.txt
├── firstname.txt
├── flag.png
├── fullname.txt
├── hr.txt
├── image.png
├── lastname.txt
├── ops.txt
├── pos.txt
├── stats_for_series.txt
└── team_color.png
```

A pitcher directory consists of the following files:
```
.
├── count.txt
├── era.txt
├── firstname.txt
├── flag.png
├── fullname.txt
├── image.png
├── innings.txt
├── lastname.txt
├── pitching-title.txt
├── pitching.txt
├── stats_for_series.txt
├── strikeouts.txt
├── team_color.png
└── walks.txt
```

The complete output directory is structured as follows:
```
.
├── away_color.png
├── away_errors.txt
├── away_flag.png
├── away_hits.txt
├── away_score.txt
├── away_team.txt
├── balls.txt
├── bases
│   ├── ooo.png
│   ├── oox.png
│   ├── oxo.png
│   ├── oxx.png
│   ├── xoo.png
│   ├── xox.png
│   ├── xxo.png
│   └── xxx.png
├── bases.png
├── count.txt
├── current_batter
│   └── <see batter directory content above>
├── ondeck_batter
│   └── <see batter directory content above>
├── inhole_batter
│   └── <see batter directory content above>
├── current_pitcher
│   └── <see pitcher directory content above>
├── home_color.png
├── home_errors.txt
├── home_flag.png
├── home_hits.txt
├── home_score.txt
├── home_team.txt
├── inning_half.txt
├── inning_text.txt
├── inning.txt
├── lineups
│   ├── away
│   │   ├── 1
│   │   │   └── <see batter directory content above>
│   │   ├── 2
│   │   │   └── <see batter directory content above>
│   │   ├── 3
│   │   │   └── <see batter directory content above>
│   │   ├── 4
│   │   │   └── <see batter directory content above>
│   │   ├── 5
│   │   │   └── <see batter directory content above>
│   │   ├── 6
│   │   │   └── <see batter directory content above>
│   │   ├── 7
│   │   │   └── <see batter directory content above>
│   │   ├── 8
│   │   │   └── <see batter directory content above>
│   │   ├── 9
│   │   │   └── <see batter directory content above>
│   │   ├── [P or DH]
│   │   │   └── <see batter directory content above>
│   │   ├── C
│   │   │   └── <see batter directory content above>
│   │   ├── 1B
│   │   │   └── <see batter directory content above>
│   │   ├── 2B
│   │   │   └── <see batter directory content above>
│   │   ├── 3B
│   │   │   └── <see batter directory content above>
│   │   ├── SS
│   │   │   └── <see batter directory content above>
│   │   ├── LF
│   │   │   └── <see batter directory content above>
│   │   ├── CF
│   │   │   └── <see batter directory content above>
│   │   ├── RF
│   │   │   └── <see batter directory content above>
│   │   └── pitcher
│   │       └── <see pitcher directory content above>
│   └── home
│       ├── 1
│       │   └── <see batter directory content above>
│       ├── 2
│       │   └── <see batter directory content above>
│       ├── 3
│       │   └── <see batter directory content above>
│       ├── 4
│       │   └── <see batter directory content above>
│       ├── 5
│       │   └── <see batter directory content above>
│       ├── 6
│       │   └── <see batter directory content above>
│       ├── 7
│       │   └── <see batter directory content above>
│       ├── 8
│       │   └── <see batter directory content above>
│       ├── 9
│       │   └── <see batter directory content above>
│       ├── [P or DH]
│       │   └── <see batter directory content above>
│       ├── C
│       │   └── <see batter directory content above>
│       ├── 1B
│       │   └── <see batter directory content above>
│       ├── 2B
│       │   └── <see batter directory content above>
│       ├── 3B
│       │   └── <see batter directory content above>
│       ├── SS
│       │   └── <see batter directory content above>
│       ├── LF
│       │   └── <see batter directory content above>
│       ├── CF
│       │   └── <see batter directory content above>
│       ├── RF
│       │   └── <see batter directory content above>
│       └── pitcher
│           └── <see pitcher directory content above>
├── outs
│   ├── 0.png
│   ├── 1.png
│   └── 2.png
├── outs.png
├── strikes.txt
└── team_resources
    ├── colors
    │   ├── ALB.png
    │   ├── BRO.png
    │   ├── CIT.png
    │   ├── default_away.png
    │   ├── default_home.png
    │   ├── ENK.png
    │   ├── ENS.png
    │   ├── EXP.png
    │   ├── FRS.png
    │   ├── GBR.png
    │   ├── GEF.png
    │   ├── GOT.png
    │   ├── KGA.png
    │   ├── LDC.png
    │   ├── LEK.png
    │   ├── MAL.png
    │   ├── MBI.png
    │   ├── NIC.png
    │   ├── ORE.png
    │   ├── POL.png
    │   ├── RAT.png
    │   ├── SKE.png
    │   ├── SKO.png
    │   ├── SOD.png
    │   ├── STO.png
    │   ├── SUI.png
    │   ├── SUN.png
    │   ├── SVL.png
    │   ├── SWE.png
    │   ├── TRA.png
    │   ├── TUR.png
    │   ├── UME.png
    │   ├── UPP.png
    │   ├── VIF.png
    │   └── VIL.png
    ├── flags
    │   └── default.png
    └── player_images
        ├── <teamId>-<playerId>.png
        └── default.png
```

If you want to use your own images for bases, outs, team flags, or player images; then just replace the files in the `bases/`, `outs/`, `team_resources/...` directories (e.g., copy your own bases empty image to `bases/ooo.png` and so forth).

## Building from source code

TBD

## Contributing

TBD