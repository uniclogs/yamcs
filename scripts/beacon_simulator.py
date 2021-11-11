#!/usr/bin/env python3
"""Generate C3 beacons and send to Yamcs

Python library dependencies:
    bitstring
"""

import binascii
import os
import socket
import sys
from threading import Thread
from time import sleep, time
from argparse import ArgumentParser

import bitstring

parser = ArgumentParser(description="OLM file transfer")
parser.add_argument("-p", "--print", dest="print", action="store_true",
                    help="print messages to stdout")
args = parser.parse_args()


def send_tm(simulator):
    tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    dest = "SPACE "
    dest_ssid = 0
    src = "KJ7SAT"
    src_ssid = 0
    control = 0
    pid = 0

    packet_header = dest.encode() + dest_ssid.to_bytes(1, 'little') + \
        src.encode() + src_ssid.to_bytes(1, 'little') + \
        control.to_bytes(1, 'little') + pid.to_bytes(1, 'little')

    packet_header = (bitstring.BitArray(packet_header) << 1).bytes

    simulator.tm_counter = 1
    while True:
        time_int_byte = int(time()).to_bytes(4, byteorder='big')
        packet_data = bytearray(os.urandom(9)) + time_int_byte + \
            bytearray(os.urandom(236))
        calc_crc = binascii.crc32(packet_data)
        packet = packet_header + packet_data + calc_crc.to_bytes(4, 'little')

        if args.print:
            print("")
            print(packet)

        tm_socket.sendto(packet, ('127.0.0.1', 10015))
        simulator.tm_counter += 1
        simulator.last_crc = calc_crc

        sleep(1)


def receive_tc(simulator):
    tc_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    tc_socket.bind(('127.0.0.1', 10025))
    while True:
        data, _ = tc_socket.recvfrom(4096)
        simulator.last_tc = data
        simulator.tc_counter += 1


class Simulator():

    def __init__(self):
        self.tm_counter = 0
        self.tc_counter = 0
        self.tm_thread = None
        self.tc_thread = None
        self.last_tc = None
        self.last_crc = 0

    def start(self):
        self.tm_thread = Thread(target=send_tm, args=(self,))
        self.tm_thread.daemon = True
        self.tm_thread.start()
        self.tc_thread = Thread(target=receive_tc, args=(self,))
        self.tc_thread.daemon = True
        self.tc_thread.start()

    def print_status(self):
        cmdhex = None
        if self.last_tc:
            cmdhex = binascii.hexlify(self.last_tc).decode('ascii')
        return 'Sent: {} packets. Received: {} commands. Last command: {}. Last CRC32: {}'.format(
                         self.tm_counter, self.tc_counter, cmdhex, self.last_crc)


if __name__ == '__main__':
    simulator = Simulator()
    simulator.start()

    try:
        prev_status = None
        while True:
            status = simulator.print_status()
            if status != prev_status:
                sys.stdout.write('\r')
                sys.stdout.write(status)
                sys.stdout.flush()
                prev_status = status
            sleep(0.5)
    except KeyboardInterrupt:
        sys.stdout.write('\n')
        sys.stdout.flush()
