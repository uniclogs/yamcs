#!/usr/bin/env python3
"""Send C3 beacon to Yamcs from SCS

Python library dependencies:
    serial
"""

import sys
import socket
from serial import Serial, SerialException

TTY = '/dev/serial/by-id/usb-SCS_SCS_Tracker___DSP_TNC_PT2HJ743-if00-port0'
BEACON_LEN = 242


def _readline(ser: Serial):
    eol = b'\r\n'
    leneol = len(eol)
    line = bytearray()
    while True:
        c = ser.read(1)
        if c:
            line += c
            if line[-leneol:] == eol:
                break
        else:
            break
    return bytes(line)


def send_tm(ser: Serial):
    tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    # CSC extracts the header and send it as a seperate message decode
    # This is to make fake header
    dest = "SPACE "
    dest_ssid = 0
    src = "KJ7SAT"
    src_ssid = 0
    control = 0
    sid = 0
    packet_header = dest.encode() + dest_ssid.to_bytes(1, 'little') + \
        src.encode() + src_ssid.to_bytes(1, 'little') + \
        control.to_bytes(1, 'little') + sid.to_bytes(1, 'little')

    while True:
        message = b''
        while len(message) < BEACON_LEN:
            try:
                line = _readline(ser)
            except SerialException as exc:
                print('Device error: {}\n'.format(exc))
                break

            # deal with message that have \r\n in the middle of it
            if line.startswith('{{z'.encode()):
                # found a new aprs header start new message
                message = line
            else:
                message += line

        if len(message) < BEACON_LEN:
            continue  # line is header

        # merge header and payload together
        # remove the \r\n
        packet = packet_header + message[:len(message)-2]

        tm_socket.sendto(packet, ('127.0.0.1', 10015))


if __name__ == '__main__':
    ser = Serial(TTY, 38400, timeout=15.0)

    try:
        send_tm(ser)
    except KeyboardInterrupt:
        sys.exit(0)
