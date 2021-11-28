
from time import time

from yamcs.core.exceptions import YamcsError

from file_transfer import file_upload


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
        print(ack.status)
    except YamcsError as exc:
        print(exc)


def fw_help() -> None:
    '''Print help message for C3 firmware commands'''

    print('Firmware commands:')
    print('  fw flash CRC32 FILEPATH')
    print('  fw bank BANK')
    print('  fw verify BANK')
    print('')
    print('CRC is the file crc')
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
        cmd = '/OreSat0/C3FwFalsh'
        crc32 = int(inps[1])
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
        print(ack.status)
    except YamcsError as exc:
        print(exc)


def fs_help() -> None:
    '''Print help message for C3 filesystem commands'''

    print('Filesystem commands:')
    print('  fs format')
    print('  fs unmount')
    print('  fs remove C3_FILEPATH')
    print('  fs crc C3_FILEPATH')
    print('  fs upload LOCAL_FILEPATH')
    print('')
    print('C3_FILEPATH is filepath on the C3')
    print('LOCAL_FILEPATH is filepath on the local system')


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
        file_upload(inps[1])
        return
    else:
        raise ValueError('not a valid filesystem command')

    try:
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print(ack.status)
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
        print(ack.status)
    except YamcsError as exc:
        print(exc)
