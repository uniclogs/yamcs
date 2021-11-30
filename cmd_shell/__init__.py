import os
import socket as s

YAMCS_USERNAME = os.getenv('YAMCS_USERNAME', 'admin')
YAMCS_PASSWORD = os.getenv('YAMCS_PASSWORD', 'admin')
YAMCS_INSTANCE = os.getenv('YAMCS_INSTANCE', 'oresat0')
YAMCS_HOST = os.getenv('YAMCS_HOST', 'localhost')
YAMCS_PORT = os.getenv('YAMCS_PORT', '8090')
YAMCS_URL = f'{YAMCS_HOST}:{YAMCS_PORT}'

BUFFER_SIZE = 4096
SOCKET_HOST = '127.0.0.1'

UPLINK_PORT = 10025
UPLINK_ADDR = (SOCKET_HOST, UPLINK_PORT)
UPLINK_SOCKET = s.socket(s.AF_INET, s.SOCK_DGRAM)

DOWNLINK_PORT = 10016
DOWNLINK_ADDR = (SOCKET_HOST, DOWNLINK_PORT)
DOWNLINK_SOCKET = s.socket(s.AF_INET, s.SOCK_DGRAM)
DOWNLINK_SOCKET.bind(DOWNLINK_ADDR)
