#!/usr/bin/env python3
import socket
from time import ctime
from cryptography.fernet import Fernet, InvalidToken


# Get the params
with open('secret', 'r') as file:
    KEY = Fernet(file.read())
KEY_SIZE = 140
ADDR = ('127.0.0.1', 9842)
EXPECTED_SYMBOL = 'ORESAT0'


# Create the socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(ADDR)


# Wait for messages
while True:
    cmd, recv_ok = sock.recvfrom(512)
    print(f'[{ctime()}]: received raw cmd: {cmd} ({len(cmd)} bytes)')
    callsign = cmd[0:7].decode('utf-8')
    buffer = list(map(lambda x: int(x), cmd[7:15]))
    enc_auth_key = cmd[15:(15 + KEY_SIZE)]
    payload = cmd[(15 + KEY_SIZE):].decode('utf-8')
    print(f'\tCallsign: `{callsign}`\n\tBuffer: {buffer}\n\tAuth Key: `{enc_auth_key}`\n\tPayload: `{payload}`')

    try:
        auth_key = KEY.decrypt(enc_auth_key).decode('utf-8')
        __parts = auth_key.split(' ')
        symbol = __parts[0]
        timestamp = ' '.join(__parts[1:])
        is_authenticated = 'AUTHENTICATED' if(symbol == EXPECTED_SYMBOL) else 'UNAUTHENTICATED'
        print(f'[{is_authenticated}]: Symbol: {symbol} sent at {timestamp}')
    except InvalidToken as e:
        print(f'[UNAUTHENTICATED]: bad auth: `{enc_auth_key}`')
