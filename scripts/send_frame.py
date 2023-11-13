import socket
from time import sleep

raw = bytes(open("bcn_pkt.raw", "rb").read())
tm_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

while True:
    sleep(3)
    tm_socket.sendto(raw, ("127.0.0.1", 10015))
