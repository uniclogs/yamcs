import os

YAMCS_USERNAME = os.getenv('YAMCS_USERNAME', 'admin')
YAMCS_PASSWORD = os.getenv('YAMCS_PASSWORD', 'admin')
YAMCS_INSTANCE = os.getenv('YAMCS_INSTANCE', 'oresat0')
YAMCS_HOST = os.getenv('YAMCS_HOST', 'localhost')
YAMCS_PORT = os.getenv('YAMCS_PORT', '8090')
YAMCS_URL = f'{YAMCS_HOST}:{YAMCS_PORT}'
