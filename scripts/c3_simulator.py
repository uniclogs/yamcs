#!/usr/bin/env python3
import socket as s

# Globals
SOCKET_HOST = "127.0.0.1"
BUFFER_SIZE = 4096

# Uplink socket stuff
UPLINK_PORT = 10025
UPLINK_ADDR = (SOCKET_HOST, UPLINK_PORT)
uplink_socket = s.socket(s.AF_INET, s.SOCK_DGRAM)
uplink_socket.bind(UPLINK_ADDR)

# Downlink socket stuff
DOWNLINK_PORT = 10016
DOWNLINK_ADDR = (SOCKET_HOST, DOWNLINK_PORT)
downlink_socket = s.socket(s.AF_INET, s.SOCK_DGRAM)

while True:
    # Mock receiving commands
    # uplink_socket.settimeout(10)
    message, sender = uplink_socket.recvfrom(BUFFER_SIZE)
    print(f"[FROM {sender}]: `{message}`")

    # Skip empty packets
    if len(message) == 0:
        print("Client sent an empty message! Skipping...")
        continue

    # Send the response
    response = b"\x00" * 8 + len(message).to_bytes(4, "little")
    print(f"[TO {DOWNLINK_ADDR}]: `{response}`")
    downlink_socket.sendto(response, DOWNLINK_ADDR)
