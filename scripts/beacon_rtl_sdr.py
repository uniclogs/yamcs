#!/usr/bin/env python3
"""Send C3 beacon to Yamcs with rtl-sdr

Linux package dependencies:
    rtl-sdr direwolf

Python library dependencies:
    kiss
"""

import sys
from time import sleep
from subprocess import Popen, DEVNULL, PIPE
from socket import socket, AF_INET, SOCK_DGRAM
from argparse import ArgumentParser

import kiss

# start the rtl_fm and direwolf commands
rtl_fm_args = ["rtl_fm", "-Mfm", "-f436.5M", "-p48.1", "-s96000", "-g30", "-"]
direwolf_args = ["direwolf", "-t0", "-r96000", "-D1", "-B9600", "-"]

parser = ArgumentParser(description="OLM file transfer")
parser.add_argument(
    "-p",
    "--print",
    dest="print",
    action="store_true",
    help="print messages to stdout",
)
args = parser.parse_args()


def send_packet(message):
    # get the TNC command. We only support 0x00 data from
    cmd = message[0]
    if cmd != 0:
        print("Unknown command: {}".format(str(cmd)))
        return

    # remove added TNC command
    packet = message[1:]

    if args.print:
        print(packet)

    tm_socket.sendto(packet, ("127.0.0.1", 10015))


rtl_fm_cmd = Popen(rtl_fm_args, stdout=PIPE, stderr=DEVNULL)
direwolf_cmd = Popen(
    direwolf_args, stdin=rtl_fm_cmd.stdout, stdout=DEVNULL, stderr=DEVNULL
)
tm_socket = socket(AF_INET, SOCK_DGRAM)
sleep(0.5)  # wait just a sec for the rtl_fm and direwolf to start
k = kiss.TCPKISS("localhost", 8001)
k.start()  # start the TCP TNC connection

try:
    k.read(callback=send_packet)  # set the TNC read callback
except KeyboardInterrupt:
    print("killing rtl_fm and direwolf...")
    rtl_fm_cmd.terminate()
    direwolf_cmd.terminate()
    sys.exit()
