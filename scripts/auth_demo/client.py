#!/usr/bin/env python3
from time import ctime
from datetime import datetime
from rsa import PublicKey, encrypt
from socket import socket, AF_INET, SOCK_DGRAM
from common import SAT_ADDR, MISC_ADDR, verify_response, addr_to_str


# Create the socket(s) mocking RF Tx
sock = socket(AF_INET, SOCK_DGRAM)

# Grab the public key
with open('id_rsa.pub', 'r') as file:
    public_key = PublicKey.load_pkcs1(file.read(), "PEM")

while True:
    # Generate secret message
    print('\nGenerating new secret message...')
    secret_message = encrypt(bytes(f"ORESAT0 {datetime.utcnow()}", 'utf-8'), public_key)

    # Simualte broadcasting a message OTA
    #   NOTE: two sockets are needed for simulating both the satellite AND an
    #   unintended "malicious listener" receiving our OTA messages
    input('Press any key to send a message...')
    print(f'\n[{ctime()}]: Sending block-encoded message of {len(secret_message)} bytes to [{addr_to_str(*SAT_ADDR)}, {addr_to_str(*MISC_ADDR)}]:\n{secret_message}\n')
    sock.sendto(secret_message, SAT_ADDR)
    sock.sendto(secret_message, MISC_ADDR)

    # Wait for the server to acknowledge
    response, recv_ok = sock.recvfrom(4096)

    # Verify and print the response from the server
    (signature, message) = response.split(b' ', maxsplit=1)
    print(verify_response(signature, message, public_key))
