#!/usr/bin/env python3
from common import SAT_ADDR, MISC_ADDR
from socket import socket, AF_INET, SOCK_DGRAM
from time import ctime
from rsa import PublicKey, encrypt


# Create the socket(s) mocking RF Tx
sock = socket(AF_INET, SOCK_DGRAM)

# Grab the public key
with open('id_rsa.pub', 'r') as file:
    pub_key = PublicKey.load_pkcs1(file.read(), "PEM")

while True:
    # Generate secret message
    print('\nGenerating new secret message...')
    secret_message = encrypt(bytes(f"ORESAT0 {ctime()}", 'utf-8'), pub_key)

    input('Press any key to send a message...')
    print(f'\n[{ctime()}]: Sending block-encoded message of {len(secret_message)} bytes:\n{secret_message}')
    sock.sendto(secret_message, SAT_ADDR)
    sock.sendto(secret_message, MISC_ADDR)
