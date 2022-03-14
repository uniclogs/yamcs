#!/usr/bin/env python3
"""Generate C3 beacons and send to Yamcs

Python library dependencies:
    bitstring
"""

import binascii
import os
import socket
from time import sleep, time
from argparse import ArgumentParser

import bitstring

TOTAL_LEN = 252
APRS_HEADER_LEN = 16
TIME_LEN = 4
CRC_LEN = 4

TIME_OFFSET = 10

parser = ArgumentParser(description="OLM file transfer")
parser.add_argument("-p", "--print", dest="print", action="store_true",
                    help="print messages to stdout")
args = parser.parse_args()

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

tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

while True:
    time_int_byte = int(time()).to_bytes(TIME_LEN, byteorder='little')
    temp = TOTAL_LEN - APRS_HEADER_LEN - TIME_OFFSET - TIME_LEN - CRC_LEN
    packet_data = bytearray(os.urandom(TIME_OFFSET)) + time_int_byte + bytearray(os.urandom(temp))
    calc_crc = binascii.crc32(packet_data)
    packet = packet_header + packet_data + calc_crc.to_bytes(CRC_LEN, 'little')

    if args.print:
        print('')
        print(f'Time: {int(time())}')
        print(f'Packet Len: {len(packet)}')
        print(f'Packet: {packet}')

    tm_socket.sendto(packet, ('127.0.0.1', 10015))

    sleep(1)
