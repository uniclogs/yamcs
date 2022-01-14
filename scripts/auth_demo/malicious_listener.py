#!/usr/bin/env python3
from rsa import PublicKey, encrypt
from time import ctime, sleep
from socket import socket, AF_INET, SOCK_DGRAM
from common import SAT_ADDR, MISC_ADDR, verify_response, addr_to_str


# Grab the public key
with open('id_rsa.pub', 'r') as file:
    public_key = PublicKey.load_pkcs1(file.read(), "PEM")

# Create the socket
sock = socket(AF_INET, SOCK_DGRAM)
sock.bind(MISC_ADDR)

# Listen for messages
while True:
    # Simualte receiving a broadcasted message from the client
    message, recv_ok = sock.recvfrom(4096)

    # Log some stuff then wait
    print(f'[{ctime()}]: Got message with {len(message)} bytes!')
    print('Waiting a bit before attempting replay attack...')
    sleep(3)

    # Attempt to replay lask known message
    print(f'Resending message: {message}')
    sock.sendto(message, SAT_ADDR)

    # Wait for the server to acknowledge
    response, recv_ok = sock.recvfrom(4096)

    # Verify and print the response from the server
    (signature, message) = response.split(b' ', maxsplit=1)
    print(verify_response(signature, message, public_key))

    # Attempt to send an encrypted message
    dupe_message = encrypt(bytes(f'ORESAT0 Thu Jan 13 16:59:06 2022', 'utf-8'), public_key)
    print(f'[{ctime()}]: Attemting to send dummy message to {addr_to_str(*SAT_ADDR)}: {dupe_message}')
    sock.sendto(dupe_message, SAT_ADDR)

    # Wait for the server to acknowledge
    response, recv_ok = sock.recvfrom(4096)

    # Verify and print the response from the server
    (signature, message) = response.split(b' ', maxsplit=1)
    print(verify_response(signature, message, public_key))
