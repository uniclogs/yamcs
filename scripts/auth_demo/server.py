#!/usr/bin/env python3
from common import SAT_ADDR
from time import ctime
from rsa import PrivateKey, decrypt
from socket import socket, AF_INET, SOCK_DGRAM

# Create the socket
sock = socket(AF_INET, SOCK_DGRAM)
sock.bind(SAT_ADDR)

# Fetch the private key
expected_symbol = "ORESAT0"
with open('id_rsa', 'r') as file:
    private_key = PrivateKey.load_pkcs1(file.read(), "PEM")

# Listen for messages
while True:
    message, recv_ok = sock.recvfrom(4096)
    message = decrypt(message, private_key).decode('utf-8')
    (symbol, timestamp) = message.split(' ', maxsplit=1)

    is_valid = 'OK' if(symbol == expected_symbol) else 'UNAUTHORIZED'

    print(f'[{ctime()} @ tcp://{recv_ok[0]}:{recv_ok[1]}]: received {is_valid} message from {symbol} sent on {timestamp}')
