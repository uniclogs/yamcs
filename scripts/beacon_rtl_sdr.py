#!/usr/bin/env python3
"""Send C3 beacon to Yamcs with rtl-sdr

Linux package dependencies:
    rtl-sdr direwolf

Python library dependencies:
    kiss
"""

import time
from subprocess import Popen
import subprocess
import sys
import socket
import kiss

# start the rtl_fm and direwolf commands
rtl_fm_args = ["rtl_fm", "-Mfm", "-f436.5M", "-p48.1", "-s96000", "-g30", "-"]
direwolf_args = ["direwolf", "-t0", "-r96000", "-D1", "-B9600", "-"]
rtl_fm_cmd = Popen(rtl_fm_args, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
direwolf_cmd = Popen(direwolf_args, stdin=rtl_fm_cmd.stdout, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)


def send_packet(x):
    # get the TNC command. We only support 0x00 data from
    cmd = x[0]
    if cmd != 0:
        print("Unknown command: {}".format(str(cmd)))
        return

    # send fixed message
    tm_socket.sendto(x, ('127.0.0.1', 10015))


def read_kiss_forever():
    # wait just a sec for the rtl_fm and direwolf to start
    time.sleep(0.5)
    k = kiss.TCPKISS("localhost", 8001)
    k.start()  # start the TCP TNC connection
    k.read(callback=send_packet)  # set the TNC read callback


if __name__ == '__main__':
    try:
        read_kiss_forever()
    except KeyboardInterrupt:
        print("killing rtl_fm and direwolf...")
        rtl_fm_cmd.terminate()
        direwolf_cmd.terminate()
        sys.exit()
