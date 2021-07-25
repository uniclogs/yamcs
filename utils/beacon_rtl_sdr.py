#!/usr/bin/python3
"""Print parsed C3 beacon from SCS"""

import kiss
import time
from subprocess import Popen
import subprocess
import sys
import logging
import bitstring
import socket

# start the rtl_fm and direwolf commands
rtl_fm_args = ["rtl_fm", "-Mfm", "-f436.5M", "-p48.1", "-s96000", "-g30", "-"]
direwolf_args = ["direwolf", "-t0", "-r96000", "-D1", "-B9600", "-"]
rtl_fm_cmd = Popen(rtl_fm_args, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL)
direwolf_cmd = Popen(direwolf_args, stdin=rtl_fm_cmd.stdout, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)


def parse_packet(x):
    # get the TNC command. We only support 0x00 data from
    cmd = x[0]
    if cmd != 0:
        logging.error("unknown command: " + str(cmd))
        return

    # decode the 14 byte address fields with the callsigned and SSIDs. The
    # field is encoded shifted 1 but to the left, so shift it it to the right>
    addr = x[1:15]
    addr = (bitstring.BitArray(addr) >> 1).bytes

    # readd the control and PID bytes
    addr = addr + x[15:16] + x[16:17]

    # send fixed message
    tm_socket.sendto(addr + x[17:], ('127.0.0.1', 10015))


def read_kiss_forever():
    # wait just a sec for the rtl_fm and direwolf to start
    time.sleep(0.5)
    k = kiss.TCPKISS("localhost", 8001)
    k.start()  # start the TCP TNC connection
    k.read(callback=parse_packet)  # set the TNC read callback


try:
    read_kiss_forever()
except KeyboardInterrupt:
    print("killing rtl_fm and direwolf...")
    rtl_fm_cmd.terminate()
    direwolf_cmd.terminate()
    sys.exit()
