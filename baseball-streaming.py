import PySimpleGUI as sg
import subprocess
import threading
import sys
import os


def resource_path(relative_path):
    base_path = getattr(sys, '_MEIPASS', os.path.dirname(
        os.path.abspath(__file__)))
    return os.path.join(base_path, relative_path)


def runCommand(cmd):
    print("Streamer starting...")
    print(' '.join(cmd))
    startupinfo = subprocess.STARTUPINFO()
    startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
    p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, startupinfo=startupinfo)
    for line in p.stdout:
        global stop_streaming
        if stop_streaming:
            p.kill()
            break
        print(line.decode().rstrip())


sg.theme('SystemDefault')

layout = [[sg.Text('Output directory for OBS resources', size=(30, 1), justification='right'), sg.Input(size=80, key='target_dir'), sg.FolderBrowse()],
          [sg.Text('Series ID', size=(30, 1), justification='right'),
           sg.Input(default_text='2022-elitserien-baseboll', size=20, key='series_id')],
          [sg.Text('Game ID', size=(30, 1), justification='right'),
           sg.Input(size=6, key='game_id')],
          [sg.Radio('Live', key='live', group_id=1, default=True),
           sg.Radio('Replay', key='replay', group_id=1)],
          [sg.Submit(button_text='Start streaming', button_color='Green'),
           sg.Cancel(button_text='Stop', button_color='Red'),
           sg.Cancel(button_text='Exit'),
           sg.Cancel(button_text='Clear')],
          [sg.Text('Log:')],
          [sg.Output(size=(160, 30), key='output_area', echo_stdout_stderr=True)]]

window = sg.Window('Baseball Streamer', layout,
                   icon=resource_path('Sundbyberg-logo-1972-1.png'))

streamer_thread = None
while True:
    if window.was_closed():
        if streamer_thread:
            stop_streaming = True
            streamer_thread.join()
        sys.exit(0)
    event, values = window.read()
    if event == 'Clear':
        window['output_area'].update('')
    if event == 'Exit':
        if streamer_thread:
            stop_streaming = True
            streamer_thread.join()
        sys.exit(0)
    if event == 'Stop':
        if streamer_thread:
            stop_streaming = True
            streamer_thread.join()
        print('Streamer stopped!')
    if event == 'Start streaming':
        if streamer_thread:
            stop_streaming = True
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
        streamer_thread = threading.Thread(target=lambda: runCommand(cmd))
        stop_streaming = False
        streamer_thread.start()
