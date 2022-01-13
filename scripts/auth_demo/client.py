#!/usr/bin/env python3
import socket
import random
import string
from time import ctime
from cryptography.fernet import Fernet

# Get the params
with open('secret', 'r') as file:
    KEY = Fernet(file.read())
KEY_SIZE = 140
ADDR = ('127.0.0.1', 9842)

# Encrypt the message
key_phrase = f'ORESAT0 {ctime()}'
# cmd_auth_key = str(KEY.encrypt(key_phrase.encode()))[2:-1] # An actual encrypted auth key
cmd_auth_key = ''.join(random.choice(string.ascii_letters) for _ in range(KEY_SIZE)) # Low effort random string for auth key
print(f'Generated cmd auth: `{cmd_auth_key}` ({len(cmd_auth_key)} bytes)')

# Create the complete "command" string
cmd = bytes(f'ORESAT0\0\0\0\0\0\0\0\0{cmd_auth_key}\0non-encrypted payload data...', 'utf-8')

# Send the message
print(f'[{ctime()}]: sending cmd: {cmd} ({len(cmd)} bytes)')
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.sendto(cmd, ADDR)
