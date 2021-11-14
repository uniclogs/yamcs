'''Common helper functions'''

from enum import IntEnum


def str2int(inp: str) -> int:
    '''Convert string into int. Supports hex and decimal.'''

    ret = None

    try:
        ret = int(inp, 16)
    except ValueError:
        pass

    if not ret:
        try:
            ret = int(inp)
        except ValueError:
            pass

    if not ret:
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
