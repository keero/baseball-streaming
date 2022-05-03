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


sg.theme('SystemDefault')

layout = [[sg.Text('Output directory for OBS resources', size=(30, 1), justification='right'),
           sg.Input(default_text='/home/martinkero/Documents/personal/obs/resources', size=80, key='target_dir'),
           sg.FolderBrowse()],
          [sg.Text('Quick select series', size=(30, 1), justification='right'),
           sg.Button(button_text='Elitserien'),
           sg.Button(button_text='Juniorserien'),
           sg.Button(button_text='Regionserien')],
          [sg.Text('', size=(30, 1), justification='right'),
           sg.Button(button_text='Softbollserien'),
           sg.Button(button_text='Juniorserien Softboll')],
          [sg.Text('Series ID', size=(30, 1), justification='right'),
           sg.Input(default_text='2022-elitserien-baseboll', size=30, key='series_id')],
          [sg.Text('Game ID', size=(30, 1), justification='right'),
           sg.Input(default_text='91830', size=6, key='game_id')],
          [sg.Text('',size=(30, 1)),
           sg.Radio('Live', key='live', group_id=1, default=True),
           sg.Radio('Replay', key='replay', group_id=1)],
          [sg.Submit(button_text='Start streaming', button_color='Green'),
           sg.Cancel(button_text='Stop', button_color='Red'),
           sg.Cancel(button_text='Exit'),
           sg.Cancel(button_text='Clear')],
          [sg.Text('Log:')],
          [sg.Output(size=(400, 30), expand_x=True, expand_y=True, key='output_area', echo_stdout_stderr=True)]]

window = sg.Window('Baseball Streaming', layout, resizable=True, size=(1024,768),
                   icon=resource_path('256x256.png'))

streamer_thread = None
while True:
    if window.was_closed():
        if streamer_thread:
            stop_streaming = True
            streamer_thread.join()
        sys.exit(0)
    event, values = window.read()
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
        streamer_thread = threading.Thread(target=lambda: runCommand(cmd))
        streamer_thread.start()
