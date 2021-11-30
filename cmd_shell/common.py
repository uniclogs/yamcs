'''Common helper functions'''

from enum import IntEnum
from . import DOWNLINK_SOCKET, BUFFER_SIZE


def get_yamcs_downlink_response(socket = DOWNLINK_SOCKET) -> bytes:
    '''
    This fetches the asynchronous downlink response from Yamcs via the following steps:
        1. Create a socket with a unique address
        2. Announce to Yamcs on it's primary downlink port that the shell exists
        3. Relay the shell's address to Yamcs
        4. Wait for a response from Yamcs (usually a 12b bytestring)
        5. Automatically strip the first 8 bytes off the response (first 8 bytes are always USLP headers)
    '''
    message, sender = socket.recvfrom(BUFFER_SIZE)
    return bytes(list(message)[8:])


def str2int(inp: str) -> int:
    '''Convert string into int. Supports hex and decimal.'''

    ret = None

    try:
        ret = int(inp, 16)
    except ValueError:
        pass

    if ret is None:
        try:
            ret = int(inp)
        except ValueError:
            pass

    if ret is None:
        raise ValueError('not a valid int str')

    return ret


def str2intenum(inp: str, int_enum: IntEnum) -> IntEnum:
    '''Convert string or int into enum'''

    ret = None

    # int
    try:
        inp_int = str2int(inp)
        ret = int_enum(inp_int)
    except ValueError:
        pass

    # str
    if not ret:
        try:
            ret = int_enum[inp]
        except KeyError:
            pass

    if not ret:
        raise ValueError('invalid input: ' + inp)

    return ret
