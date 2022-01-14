#!/usr/bin/env python3
import datetime as dt
from time import ctime
from rsa.pkcs1 import DecryptionError
from common import SAT_ADDR, addr_to_str
from rsa import PrivateKey, decrypt, sign
from socket import socket, AF_INET, SOCK_DGRAM

# Vars
TIMESTAMP_RANGE = dt.timedelta(minutes=10)

# Create the socket(s) mocking RF Rx
sock = socket(AF_INET, SOCK_DGRAM)
sock.bind(SAT_ADDR)

# Grab the private key
expected_symbol = "ORESAT0"
with open('id_rsa', 'r') as file:
    private_key = PrivateKey.load_pkcs1(file.read(), "PEM")

# Create the message table for keeping track of potentially replayed messages
message_table = {}

while True:
    # Wait for messages from anywhere
    message, recv_ok = sock.recvfrom(4096)

    try:
        # Attempt to decode the message
        message = decrypt(message, private_key).decode('utf-8')
        (symbol, timestamp) = message.split(' ', maxsplit=1)

        # Timestamp processing
        try:
            now = dt.datetime.utcnow()
            print(now)
            ts = dt.fromisoformat(timestamp)


            print(f'ts: {ts}')
        except ValueError:
            raise DecryptionError()

        # If the timestamp "salt" is already in the message table
        #   OR the decoded message contains the expected symbol
        #   THEN raise a decryption error in order to jump to the
        #        unauthorization handling
        if(timestamp in message_table or symbol != expected_symbol):
            raise DecryptionError()

        # Otherwise add the message to the message index
        #   and begin formulating a response
        message_table[timestamp] = message

        # Log some stuff
        print(f'[{ctime()}]: Received message from {addr_to_str(*recv_ok)} with symbol `{symbol}` sent on {timestamp}')

        # Sign, seal, and deliver
        message = b'OK'
        payload = sign(message, private_key, 'SHA-256') + b' ' + message
        print(f'[{ctime()}]: Sending a {len(payload)} byte OK-response to {addr_to_str(*recv_ok)}...')
        sock.sendto(payload, recv_ok)
    except DecryptionError:
        # If for any reason the message is invalid,
        #   sign and send an unauthorized response
        message = b'UNAUTHORIZED'
        payload = sign(message, private_key, 'SHA-256') + b' ' + message
        sock.sendto(payload, recv_ok)
