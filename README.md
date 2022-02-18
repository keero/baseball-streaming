# baseball-streaming
Java CLI application for fetching live data from WBSC game data (MyBallClub) and continuously write text and image files to disk.
The produced files can then be consumed by, for instance, OBS Studio to produce overlays for a live stream.


## Download

> **Make sure you have Java JDK 11 installed on your computer. OpenJDK 11 for windows may be downloaded [here](https://docs.microsoft.com/en-us/java/openjdk/download)**

Download the desired release jar from [the release page](https://github.com/keero/baseball-streaming/releases)


## Run the application

Run the application by opening a terminal (or powershell if you do Windows) and run:

```bash
java -jar <path-to-the-downloaded-jar> -h
```

(replace `<path-to-the-downloaded-jar>` with the path to the jar file you downloaded before)
This will show you the options expected by the appliaction. You may either create a config file (more on that further down) or provide the expected options directly to the application as option arguments.

The `-t` option specifies the output directory where the output text and image files will be rendered.
The `-g` option specifies which game you are streaming. This is typically a 5 digit number such as `84908`
The `-s` option specifies the series in which the game is played. This is typically in the form `<year>-<series>-<sport>` such as `2021-elitserien-baseboll`.

Assuming you want to stream the game with id `84908` in `2021-elitserien-baseboll` to the directory `C:/obs-studio/resources`, then you issue the follwoing command:

```bash
java -jar <path-to-the-downloaded-jar> -t C:/obs-studio/resources -s 2021-elitserien-baseboll -g 84908
```

If you don't know the game id and series you can find the on the game page at for instance https://www.wbsceurope.org/ or https://stats.baseboll-softboll.se/

For instance the Swedish 2021 Championship final game can be found @ [https://www.wbsceurope.org/en/events/**2021-elitserien-baseboll**/schedule-and-results/box-score/**84908**](https://www.wbsceurope.org/en/events/2021-elitserien-baseboll/schedule-and-results/box-score/84908)

The series and game id can be found in the URL (marked with bold above).

The application will output what is currently happening live. If you want to test it out with no actual game being played at the moment you can use the `replay` mode by including the option `-m replay` in the command.

### The text and image files produced in the target directory

```
.
├── away_errors.txt
├── away_hits.txt
├── away_score.txt
├── away_team.txt
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
│   ├── avg.txt
│   ├── batting.txt
│   ├── firstname.txt
│   ├── flag.png
│   ├── fullname.txt
│   ├── hr.txt
│   ├── image.png
│   ├── lastname.txt
│   ├── ops.txt
│   ├── pos.txt
│   ├── rbi.txt
│   └── stats_for_series.txt
├── current_pitcher
│   ├── count.txt
│   ├── era.txt
│   ├── firstname.txt
│   ├── flag.png
│   ├── fullname.txt
│   ├── games.txt
│   ├── hits.txt
│   ├── hrs-allowed.txt
│   ├── image.png
│   ├── innings.txt
│   ├── lastname.txt
│   ├── strikeouts.txt
│   ├── walks.txt
│   └── wins-losses.txt
├── home_errors.txt
├── home_hits.txt
├── home_score.txt
├── home_team.txt
├── inhole_batter
│   ├── avg.txt
│   ├── batting.txt
│   ├── firstname.txt
│   ├── flag.png
│   ├── fullname.txt
│   ├── hr.txt
│   ├── image.png
│   ├── lastname.txt
│   ├── ops.txt
│   ├── pos.txt
│   ├── rbi.txt
│   └── stats_for_series.txt
├── inning_half.txt
├── inning_text.txt
├── inning.txt
├── lineups
│   ├── away
│   │   ├── 1
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 2
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 3
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 4
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 5
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 6
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 7
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 8
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   ├── 9
│   │   │   ├── avg.txt
│   │   │   ├── batting.txt
│   │   │   ├── firstname.txt
│   │   │   ├── flag.png
│   │   │   ├── fullname.txt
│   │   │   ├── hr.txt
│   │   │   ├── image.png
│   │   │   ├── lastname.txt
│   │   │   ├── ops.txt
│   │   │   ├── pos.txt
│   │   │   ├── rbi.txt
│   │   │   └── stats_for_series.txt
│   │   └── pitcher
│   │       ├── count.txt
│   │       ├── era.txt
│   │       ├── firstname.txt
│   │       ├── flag.png
│   │       ├── fullname.txt
│   │       ├── games.txt
│   │       ├── hits.txt
│   │       ├── hrs-allowed.txt
│   │       ├── image.png
│   │       ├── innings.txt
│   │       ├── lastname.txt
│   │       ├── strikeouts.txt
│   │       ├── walks.txt
│   │       └── wins-losses.txt
│   └── home
│       ├── 1
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 2
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 3
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 4
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 5
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 6
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 7
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 8
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       ├── 9
│       │   ├── avg.txt
│       │   ├── batting.txt
│       │   ├── firstname.txt
│       │   ├── flag.png
│       │   ├── fullname.txt
│       │   ├── hr.txt
│       │   ├── image.png
│       │   ├── lastname.txt
│       │   ├── ops.txt
│       │   ├── pos.txt
│       │   ├── rbi.txt
│       │   └── stats_for_series.txt
│       └── pitcher
│           ├── count.txt
│           ├── era.txt
│           ├── firstname.txt
│           ├── flag.png
│           ├── fullname.txt
│           ├── games.txt
│           ├── hits.txt
│           ├── hrs-allowed.txt
│           ├── image.png
│           ├── innings.txt
│           ├── lastname.txt
│           ├── strikeouts.txt
│           ├── walks.txt
│           └── wins-losses.txt
├── ondeck_batter
│   ├── avg.txt
│   ├── batting.txt
│   ├── firstname.txt
│   ├── flag.png
│   ├── fullname.txt
│   ├── hr.txt
│   ├── image.png
│   ├── lastname.txt
│   ├── ops.txt
│   ├── pos.txt
│   ├── rbi.txt
│   └── stats_for_series.txt
├── outs
│   ├── 0.png
│   ├── 1.png
│   └── 2.png
├── outs.png
└── team_resources
    ├── flags
    │   ├── 1234.png
    │   ├── 5678.png
    │   └── default.png
    └── player_images
        ├── 1234-12345.png
        ├── 5678-67890.png
        ├── default.png
        .
        .
        .
```

If you want to use your own images for bases, outs, team flags, or player images; then just replace the files in the `bases/`, `outs/`, `team_resources/...` directories (e.g., copy your own bases empty image to `bases/ooo.png` and so forth).

## Building from source code

TBD

## Contributing

TBD