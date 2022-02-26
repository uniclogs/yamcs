
import struct
from time import time

from yamcs.core.exceptions import YamcsError

from .file_transfer import file_upload
from .common import get_yamcs_downlink_response


def c3_help() -> None:
    '''Print help message for C3 commands'''

    print('C3 commands:')
    print('  c3 softreset')
    print('  c3 hardreset')
    print('  c3 factoryreset')


def c3_cmd(conn, inp: str) -> None:
    '''Run a C3 command'''

    cmd = None

    if inp.lower() == 'help':
        c3_help()
        return

    if inp.lower() == 'softreset':
        cmd = '/OreSat0/C3SoftReset'
    elif inp.lower() == 'hardreset':
        cmd = '/OreSat0/C3HardReset'
    elif inp.lower() == 'factoryreset':
        cmd = '/OreSat0/C3FactoryReset'
    else:
        raise ValueError('not a valid c3 command')

    try:
        command = conn.issue(cmd)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print('  Yamcs response:', ack.status)
    except YamcsError as exc:
        print(exc)


def fw_help() -> None:
    '''Print help message for C3 firmware commands'''

    print('Firmware commands:')
    print('  fw flash CRC32 FILEPATH')
    print('  fw bank BANK')
    print('  fw verify BANK')
    print('')
    print('CRC is the file crc (hex must have the leading "0x")')
    print('FILEPATH is filepath on the C3 wanted')
    print('BANK is C3 bank to use, can be 0 or 1')


def fw_cmd(conn, inp: str) -> None:
    '''Run a C3 firmware command'''

    inps = inp.split(' ')
    cmd = None
    args = None

    if inps[0].lower() == 'help':
        fw_help()
        return

    if inps[0].lower() == 'flash':
        cmd = '/OreSat0/C3FwFlash'
        if inps[1][:2] == '0x':
            crc32 = int(inps[1], 16)
        else:
            crc32 = int(inps[1])
        # yamcs wants a 32 signed int for some reason, this is a stupid fix
        crc32 = struct.unpack('i', struct.pack('I', crc32))[0]
        args = {'CRC32': crc32, 'Filepath': inps[2]}
    elif inps[0].lower() == 'bank':
        cmd = '/OreSat0/C3FwBank'
        bank = int(inps[1])
        if bank not in [0, 1]:
            raise ValueError('bank not set to 0 or 1')
        args = {'Bank': bank}
    elif inps[0].lower() == 'verify':
        cmd = '/OreSat0/C3FwVerify'
        bank = int(inps[1])
        if bank not in [0, 1]:
            raise ValueError('bank not set to 0 or 1')
        args = {'Bank': bank}
    else:
        raise ValueError('not a valid firmware command')

    try:
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print('  Yamcs response:', ack.status)
    except YamcsError as exc:
        print(exc)


def fs_help() -> None:
    '''Print help message for C3 filesystem commands'''

    print('Filesystem commands:')
    print('  fs format')
    print('  fs unmount')
    print('  fs remove C3_FILEPATH')
    print('  fs crc C3_FILEPATH')
    print('  fs upload LOCAL_FILEPATH TIMEOUT RETRY START RATE_LIMIT')
    print('')
    print('C3_FILEPATH is filepath on the C3')
    print('LOCAL_FILEPATH is filepath on the local system')
    print('TIMEOUT is how long to listen (in seconds) on EDL downlink for an'
          ' ack before giving up. Set to 0 to ignore downlink replies.')
    print('RETRY is the number send and ack retries before giving up. Set to 0'
          ' retry forever.')
    print('START is the segement to start on when uploading. Set to 0 to'
          ' upload whole file')
    print('RATE_LIMIT is the delay between successful packets being sent')


def fs_cmd(conn, inp: str) -> None:
    '''Run a C3 filesystem command'''

    inps = inp.split(' ')
    cmd = None
    args = None

    if inps[0].lower() == 'help':
        fs_help()
        return

    if inps[0].lower() == 'format':
        cmd = '/OreSat0/C3FsFormat'
    elif inps[0].lower() == 'unmount':
        cmd = '/OreSat0/C3FsUnmount'
    elif inps[0].lower() == 'crc':
        cmd = '/OreSat0/C3FsCRC'
        args = {'Filepath': inps[1]}
    elif inps[0].lower() == 'remove':
        cmd = '/OreSat0/C3FsRemove'
        args = {'Filepath': inps[1]}
    elif inps[0].lower() == 'upload':
        try:
            file_upload(inps[1], int(inps[2]), float(inps[3]), int(inps[4]), float(inps[5]))
        except KeyboardInterrupt:
            pass
        return
    else:
        raise ValueError('not a valid filesystem command')

    try:
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print('  Yamcs response:', ack.status)

        # Get downlink response
        response = get_yamcs_downlink_response()
        if inps[0].lower() in 'crc':
            message = struct.unpack('<I', response)[0]
        else:
            message = struct.unpack('<i', response)[0]
        print('  Downlink packet:',  hex(message))
    except YamcsError as exc:
        print(exc)


def rtc_help() -> None:
    '''Print help message for C3 RTC commands'''

    print('RTC commands:')
    print('  rtc settime COARSE_TIME')
    print('')
    print('COARSE_TIME can be number of seconds from 1970-01-01T00:00:00.000' +
          ' or \'local\' to get it from the local system clock')


def rtc_cmd(conn, inp: str) -> None:
    '''Run a C3 filesystem command'''

    inps = inp.split(' ')
    cmd = None
    args = None

    if inps[0].lower() == 'help':
        rtc_help()
        return

    if inps[0].lower() == 'settime':
        cmd = '/OreSat0/C3RTCTime'
        if inps[1].lower() == 'local':
            time_s = int(time())
        else:
            time_s = int(inps[1])
        args = {'CoarseTime': time_s}
    else:
        raise ValueError('not a valid filesystem command')

    try:
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print('  Yamcs response:', ack.status)
    except YamcsError as exc:
        print(exc)
