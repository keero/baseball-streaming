import json
import re
from time import sleep
from tkinter import font
from turtle import color
import PySimpleGUI as sg
import subprocess
import threading
import sys
import os
from shutil import copyfile


HEAT_GAMES_2022 = {
    '2022-04-30 Elitserien Sundsvall @ Sundbyberg Game #1': {'game_id': '91830', 'series_id': '2022-elitserien-baseboll'},
    '2022-04-30 Elitserien Sundsvall @ Sundbyberg Game #2': {'game_id': '91831', 'series_id': '2022-elitserien-baseboll'},
    '2022-05-07 Regionserien Skellefteå @ Sundbyberg Game #1': {'game_id': '95336', 'series_id': '2022-regionserien-baseboll'},
    '2022-05-07 Regionserien Skellefteå @ Sundbyberg Game #2': {'game_id': '95337', 'series_id': '2022-regionserien-baseboll'},
    '2022-05-07 Elitserien Sundbyberg @ Karlskoga Game #1': {'game_id': '91839', 'series_id': '2022-elitserien-baseboll'},
    '2022-05-07 Elitserien Sundbyberg @ Karlskoga Game #2': {'game_id': '91840', 'series_id': '2022-elitserien-baseboll'},
    '2022-05-08 Juniorserien Sundbyberg @ Karlskoga Game #1': {'game_id': '91737', 'series_id': '2022-juniorserien-baseboll'},
    '2022-05-08 Juniorserien Sundbyberg @ Karlskoga Game #2': {'game_id': '91739', 'series_id': '2022-juniorserien-baseboll'},
    '2022-05-10 Elitserien Stockholm @ Sundbyberg': {'game_id': '91841', 'series_id': '2022-elitserien-baseboll'},
    '2022-05-15 Regionserien Enköping @ Sundbyberg Game #1': {'game_id': '95358', 'series_id': '2022-regionserien-baseboll'},
    '2022-05-15 Regionserien Enköping @ Sundbyberg Game #2': {'game_id': '95359', 'series_id': '2022-regionserien-baseboll'},
    '2022-05-17 Elitserien Stockholm @ Sundbyberg': {'game_id': '91849', 'series_id': '2022-elitserien-baseboll'},
    '2022-05-18 Regionserien Stockholm @ Sundbyberg': {'game_id': '95361', 'series_id': '2022-regionserien-baseboll'},
    '2022-06-04 Elitserien Karlskoga @ Sundbyberg Game #1': {'game_id': '91875', 'series_id': '2022-elitserien-baseboll'},
    '2022-06-04 Elitserien Karlskoga @ Sundbyberg Game #2': {'game_id': '91876', 'series_id': '2022-elitserien-baseboll'},
    '2022-06-05 Juniorserien Göteborg @ Sundbyberg Game #1': {'game_id': '91758', 'series_id': '2022-juniorserien-baseboll'},
    '2022-06-05 Juniorserien Göteborg @ Sundbyberg Game #2': {'game_id': '91759', 'series_id': '2022-juniorserien-baseboll'},
    '2022-06-15 Juniorserien Stockholm @ Sundbyberg': {'game_id': '91764', 'series_id': '2022-juniorserien-baseboll'},
    '2022-06-18 Elitserien Karlskoga @ Sundbyberg Game #1': {'game_id': '91883', 'series_id': '2022-elitserien-baseboll'},
    '2022-06-18 Elitserien Karlskoga @ Sundbyberg Game #2': {'game_id': '91884', 'series_id': '2022-elitserien-baseboll'},
    '2022-06-19 Juniorserien Karlskoga @ Sundbyberg Game #1': {'game_id': '91768', 'series_id': '2022-juniorserien-baseboll'},
    '2022-06-19 Juniorserien Karlskoga @ Sundbyberg Game #2': {'game_id': '91769', 'series_id': '2022-juniorserien-baseboll'},

    '2022-07-04 U18 EM Switzerland @ Turkey Game #1': {'game_id': '98582', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-04 U18 EM Poland @ Sweden Game #2': {'game_id': '98583', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-05 U18 EM Turkey @ Great Britain Game #3': {'game_id': '98584', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-05 U18 EM Sweden @ Switzerland Game #4': {'game_id': '98585', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-06 U18 EM Poland @ Switzerland Game #5': {'game_id': '98586', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-06 U18 EM Great Britain @ Sweden Game #6': {'game_id': '98587', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-07 U18 EM Turkey @ Poland Game #7': {'game_id': '98588', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-07 U18 EM Switzerland @ Great Britain Game #8': {'game_id': '98589', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-08 U18 EM Great Britain @ Poland Game #9': {'game_id': '98590', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-08 U18 EM Sweden @ Turkey Game #10': {'game_id': '98591', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},
    '2022-07-09 U18 EM Final': {'game_id': '98592', 'series_id': '2022-u-18-european-baseball-championship-qualifier'},

    '2022-07-23 Elitserien Sölvesborg @ Sundbyberg Game #1': {'game_id': '91889', 'series_id': '2022-elitserien-baseboll'},
    '2022-07-23 Elitserien Sölvesborg @ Sundbyberg Game #2': {'game_id': '91890', 'series_id': '2022-elitserien-baseboll'},
    '2022-07-24 Juniorserien Sölvesborg @ Sundbyberg Game #1': {'game_id': '91783', 'series_id': '2022-juniorserien-baseboll'},
    '2022-07-24 Juniorserien Sölvesborg @ Sundbyberg Game #2': {'game_id': '91784', 'series_id': '2022-juniorserien-baseboll'},
    '2022-08-06 Regionserien Alby @ Sundbyberg Game #1': {'game_id': '95490', 'series_id': '2022-regionserien-baseboll'},
    '2022-08-06 Regionserien Alby @ Sundbyberg Game #2': {'game_id': '95491', 'series_id': '2022-regionserien-baseboll'},
    '2022-08-11 Juniorserien Stockholm @ Sundbyberg': {'game_id': '91798', 'series_id': '2022-juniorserien-baseboll'},
    '2022-08-16 Elitserien Stockholm @ Sundbyberg': {'game_id': '91912', 'series_id': '2022-elitserien-baseboll'},
    '2022-08-19 Elitserien Stockholm @ Sundbyberg': {'game_id': '91907', 'series_id': '2022-elitserien-baseboll'},
    '2022-08-24 Regionserien Stockholm @ Sundbyberg': {'game_id': '95539', 'series_id': '2022-regionserien-baseboll'},
    '2022-08-27 Elitserien Semifinal 1 Leksand @ Sundbyberg': {'game_id': '101175', 'series_id': '2022-elitserien-baseboll'},
    '2022-08-27 Elitserien Semifinal 2 Leksand @ Sundbyberg': {'game_id': '101176', 'series_id': '2022-elitserien-baseboll'},
    '2022-08-28 Elitserien Semifinal 3 Leksand @ Sundbyberg': {'game_id': '101177', 'series_id': '2022-elitserien-baseboll'},
}

def resource_path(relative_path):
    base_path = getattr(sys, '_MEIPASS', os.path.dirname(
        os.path.abspath(__file__)))
    return os.path.join(base_path, relative_path)


def runCommand(cmd):
    global streamer_process
    print("Streamer starting...")
    print(' '.join(cmd))
    if hasattr(subprocess, 'STARTUPINFO'):
        startupinfo = subprocess.STARTUPINFO()
        startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
        streamer_process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, startupinfo=startupinfo)
    else:
        streamer_process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    for line in streamer_process.stdout:
        print(line.decode(errors='ignore').rstrip())

def getRoster(target_dir, side):
    with open('%s/roster_%s.json' % (target_dir, side), 'rb') as f:
        roster_json = json.load(f)
        f.close()
        roster = {}
        for player in roster_json:
            roster[player['name']] = player['id']
        return roster

def updateRosterLists(target_dir):
    for side in ['home', 'away']:
        roster = getRoster(target_dir, side)
        pitcher_key = 'override-%s-pitcher' % side
        batter_key = 'override-%s-batter' % side
        window[pitcher_key].update(value=window[pitcher_key].get(), values=list(roster))
        window[batter_key].update(value=window[batter_key].get(), values=list(roster))


def updatePlay(target_dir):
    global window
    with open('%s/current_play.json' % target_dir, 'rb') as f:
        current_play = json.load(f)
        f.close()
        window['play-number'].update(str(current_play['playNumber']))
        window['count'].update('%d - %d' % (current_play['situation']['balls'], current_play['situation']['strikes']))
        window['outs'].update(str(current_play['situation']['outs']))
        bases = '%c%c%c' % ('x' if current_play['situation']['runner1'] > 0 else 'o', 'x' if current_play['situation']['runner2'] > 0 else 'o', 'x' if current_play['situation']['runner3'] > 0 else 'o')
        window['bases'].update(bases)
        window['score'].update('%d - %d' % (current_play['linescore']['awaytotals']['R'], current_play['linescore']['hometotals']['R']))
        window['inning'].update(current_play['situation']['currentinning'])
        window['pitcher'].update(current_play['situation']['pitcher'])
        window['batter'].update(current_play['situation']['batter'])

def overrideScore(values):
    global window
    home_roster = getRoster(values['target_dir'], 'home')
    away_roster = getRoster(values['target_dir'], 'away')
    data = {
        'play': '' if values['or-permanent'] else window['play-number'].DisplayText,
        'count': {
            'balls': values['or-ball-count'],
            'strikes': values['or-strike-count']
        },
        'outs': values['or-outs'],
        'bases': values['or-bases'],
        'score': {
            'home': values['or-score-home'],
            'away': values['or-score-away']
        },
        'inning': values['or-inning'],
        'awaypitcher': '' if not values['override-away-pitcher'] else str(away_roster[values['override-away-pitcher']]),
        'homepitcher': '' if not values['override-home-pitcher'] else str(home_roster[values['override-home-pitcher']]),
        'awaybatter': '' if not values['override-away-batter'] else str(away_roster[values['override-away-batter']]),
        'homebatter': '' if not values['override-home-batter'] else str(home_roster[values['override-home-batter']]),
    }
    with open('%s/override.json' % values['target_dir'], 'w') as f:
        json.dump(data, f)

def deleteOverride(target_dir):
    path = '%s/override.json' % target_dir
    if os.path.exists(path):
        os.remove(path)

def getCurrentOverride(target_dir):
    path = '%s/override.json' % target_dir
    if os.path.exists(path):
        with open('%s/override.json' % target_dir, 'rb') as f:
            override = json.load(f)
            f.close()
            return override
    return None

def updateOverrideStatusText(target_dir):
    global window
    new_status_text = 'Inactive'
    override = getCurrentOverride(target_dir)
    if override:
        current_play = int(window['play-number'].DisplayText)
        override_play = -1 if not override['play'] else int(override['play'])
        if override_play == -1:
            new_status_text = 'Active'
        elif current_play > override_play:
            new_status_text = 'Expired (was valid until play # %d)' % override_play
        else:
            new_status_text = 'Active (expires after play # %d)' % override_play
    window['override-status-text'].update(new_status_text)

def findPlayer(roster, id):
    for player in roster:
        if str(roster[player]) == id:
            return player
    return ''

def fillCurrentOverride(target_dir):
    override = getCurrentOverride(target_dir)
    if not override:
        return
    home_roster = getRoster(target_dir, 'home')
    away_roster = getRoster(target_dir, 'away')
    print(override)
    print(away_roster)
    window['or-ball-count'].update(override['count']['balls'])
    window['or-strike-count'].update(override['count']['strikes'])
    window['or-outs'].update(override['outs'])
    window['or-bases'].update(override['bases'])
    window['or-score-home'].update(override['score']['home'])
    window['or-score-away'].update(override['score']['away'])
    window['or-inning'].update(override['inning'])
    window['override-away-pitcher'].update(value=findPlayer(away_roster, override['awaypitcher']))
    window['override-away-batter'].update(value=findPlayer(away_roster, override['awaybatter']))
    window['override-home-pitcher'].update(value=findPlayer(home_roster, override['homepitcher']))
    window['override-home-batter'].update(value=findPlayer(home_roster, override['homebatter']))

sg.theme('SystemDefault')

left = [[sg.Text('Output directory for OBS resources', size=(30, 1), justification='right'),
           sg.Input(default_text='/home/martinkero/Documents/personal/obs/resources', size=80, key='target_dir'),
           sg.FolderBrowse()],
          [sg.Text('Select game', size=(30, 1), justification='right'),
           sg.Combo(list(HEAT_GAMES_2022.keys()), key='game_selector', enable_events=True, readonly=True)],
          [sg.Text('', size=(30, 1), justification='right'),
           sg.Text('or enter IDs manually below')],
          [sg.Text('Series ID', size=(30, 1), justification='right'),
           sg.Input(default_text='2022-elitserien-baseboll', size=30, key='series_id')],
          [sg.Text('Game ID', size=(30, 1), justification='right'),
           sg.Input(default_text='91830', size=6, key='game_id')],
          [sg.Text('',size=(30, 1)),
           sg.Radio('Live', key='live', group_id=1, default=True),
           sg.Radio('Replay', key='replay', group_id=1)],
          [sg.Text('',size=(30, 1)),
           sg.Radio('All Stats', key='allstats', group_id=2, default=True),
           sg.Radio('Series Stats Only', key='seriesstats', group_id=2)],
          [sg.Submit(button_text='Start streaming', button_color='Green'),
           sg.Cancel(button_text='Stop', button_color='Red'),
           sg.Cancel(button_text='Exit'),
           sg.Cancel(button_text='Clear')],
          [sg.Text('Log:')],
          [sg.Output(size=(130, 300), expand_x=True, expand_y=True, key='output_area', echo_stdout_stderr=True)]]

official = sg.Frame('Official', [
           [sg.Text('@ play #:', size=(8,1), justification='right'), sg.Text('', key='play-number', size=(25,1))],
           [sg.Text('Count:', size=(8,1), justification='right'), sg.Text('', key='count', size=(25,1))],
           [sg.Text('Outs:', size=(8,1), justification='right'), sg.Text('', key='outs', size=(25,1))],
           [sg.Text('Bases:', size=(8,1), justification='right'), sg.Text('', key='bases', size=(25,1))],
           [sg.Text('Score:', size=(8,1), justification='right'), sg.Text('', key='score', size=(25,1))],
           [sg.Text('Inning:', size=(8,1), justification='right'), sg.Text('', key='inning', size=(25,1))],
           [sg.Text('Pitcher:', size=(8,1), justification='right'), sg.Text('', key='pitcher', size=(25,1))],
           [sg.Text('Batter:', size=(8,1), justification='right'), sg.Text('', key='batter', size=(25,1))],
           ], font="arial 12", border_width=1, key='official-score-frame')

manual_ovveride = sg.Frame('Manual Override', [
            [sg.Text('Status: '), sg.Text('', key='override-status-text')],
            [sg.Text('TTL:', size=(12,1), justification='right'), sg.Radio('Next play', key='or-next-play', default=True, group_id=3), sg.Radio('Permanent', key='or-permanent', group_id=3)],
            [sg.Text('Count:', size=(12,1), justification='right'), sg.Input(size=2, key='or-ball-count', justification='center'), sg.Text('-'), sg.Input(size=2, key='or-strike-count', justification='center')],
            [sg.Text('Outs:', size=(12,1), justification='right'), sg.Input(size=2, key='or-outs', justification='center')],
            [sg.Text('Bases:', size=(12,1), justification='right'), sg.Input(size=1, key='or-bases', justification='center'),
                sg.Button(size=(1,1), button_color='black on #eeeeee', button_text='1', font='arial 10 bold',),
                sg.Button(size=(1,1), button_color='black on #eeeeee', button_text='2', font='arial 10 bold',),
                sg.Button(size=(1,1), button_color='black on #eeeeee', button_text='3', font='arial 10 bold',),
                sg.Checkbox('Include',),
                ],
            [sg.Text('Score:', size=(12,1), justification='right'), sg.Input(size=2, key='or-score-away', justification='center'), sg.Text('-'), sg.Input(size=2, key='or-score-home', justification='center')],
            [sg.Text('Inning:', size=(12,1), justification='right'), sg.Input(size=5, key='or-inning', tooltip='ex: bot 3', justification='center')],
            [sg.Text('Home Pitcher:', size=(12,1), justification='right'), sg.Combo([''], key='override-home-pitcher', size=30)],
            [sg.Text('Away Pitcher:', size=(12,1), justification='right'), sg.Combo([''], key='override-away-pitcher', size=30)],
            [sg.Text('Home Batter:', size=(12,1), justification='right'), sg.Combo([''], key='override-home-batter', size=30)],
            [sg.Text('Away Batter:', size=(12,1), justification='right'), sg.Combo([''], key='override-away-batter', size=30)],
            [
             sg.Button(button_text='Clear', button_color='Gray', key='clear-override'),
             sg.Button(button_text='Get Current Override', button_color='Gray', key='get-override'),],[
             sg.Button(button_text='Start Override', button_color='Red', key='override-score'),
             sg.Button(button_text='Stop Override', button_color='Green', key='stop-override-score'),
            ],
           ], font='arial 12', border_width=1, key='override-score-frame')

right = [[sg.Text('Score',font='Arial 18 bold')],
    [sg.Column([[official]], vertical_alignment='top'), sg.Column([[manual_ovveride]], vertical_alignment='top')]]


layout3 = [[sg.Column(left, vertical_alignment='top'), sg.VerticalSeparator() ,sg.Column(right, vertical_alignment='top')]]

window = sg.Window('Baseball Streaming', layout3, resizable=True, size=(1920,1080),
                   icon=resource_path('256x256.png'))

streamer_thread = None
stop_play_updater = True
while True:
    if window.was_closed():
        if streamer_thread:
            if streamer_process:
                streamer_process.terminate()
            streamer_thread.join()
        sys.exit(0)
    event, values = window.read(timeout=500)
    updatePlay(values['target_dir'])
    updateRosterLists(values['target_dir'])
    updateOverrideStatusText(values['target_dir'])
    if event == 'game_selector':
        window['series_id'].update(HEAT_GAMES_2022[values['game_selector']]['series_id'])
        window['game_id'].update(HEAT_GAMES_2022[values['game_selector']]['game_id'])
    if event == 'Elitserien':
        window['series_id'].update('2022-elitserien-baseboll')
    if event == 'Juniorserien':
        window['series_id'].update('2022-juniorserien-baseboll')
    if event == 'Regionserien':
        window['series_id'].update('2022-regionserien-baseboll')
    if event == 'Softbollserien':
        window['series_id'].update('2022-softbollserien')
    if event == 'Juniorserien Softboll':
        window['series_id'].update('2022-juniorserien-softboll-2022')
    if event == 'Clear':
        window['output_area'].update('')
    if event == 'Exit':
        if streamer_thread:
            if streamer_process:
                streamer_process.terminate()
            streamer_thread.join()
        sys.exit(0)
    if event == 'Stop':
        if streamer_thread:
            if streamer_process:
                streamer_process.terminate()
            streamer_thread.join()
        print("Streamer stopped.")
    if event == 'Start streaming':
        if streamer_thread:
            if streamer_process:
                streamer_process.terminate()
            streamer_thread.join()
        if values['live']:
            mode = 'live'
        else:
            mode = 'replay'
        if not values['target_dir']:
            print(
                "Output directory must be set to a valid path, use browse button to select it.")
            continue
        if not values['series_id']:
            print('Series ID must be set to a value. E.g., 2022-elitserien-baseboll')
            continue
        if not values['game_id']:
            print('Game ID must be set to a value. E.g., 91830')
            continue
        cmd = ['java', '-jar', resource_path('baseball-streaming.jar'),
               '-m', mode,
               '-t', values['target_dir'],
               '-s', values['series_id'],
               '-g', values['game_id']]
        if values['seriesstats']:
            cmd.append('-l')
        streamer_thread = threading.Thread(target=lambda: runCommand(cmd))
        streamer_thread.start()
    if event == 'override-score':
        overrideScore(values)
    if event == 'clear-override':
        window['or-ball-count'].update('')
        window['or-strike-count'].update('')
        window['or-outs'].update('')
        window['or-bases'].update('')
        window['or-score-away'].update('')
        window['or-score-home'].update('')
        window['or-inning'].update('')
        window['override-home-pitcher'].update(value='')
        window['override-home-batter'].update(value='')
        window['override-away-pitcher'].update(value='')
        window['override-away-batter'].update(value='')
    if event == 'stop-override-score':
        deleteOverride(values['target_dir'])
    if event == 'get-override':
        fillCurrentOverride(values['target_dir'])

    #     manual_ovveride.Font = 'arial 12'
    #     official.Font = 'arial 12 bold'
    #     window.refresh()
