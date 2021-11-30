
import struct
from enum import IntEnum
from yamcs.core.exceptions import YamcsError
from .common import str2intenum, get_yamcs_downlink_response


class OPDAddr(IntEnum):
    '''All valid CANopen opd IDs. Names are in the format Yamcs expects'''

    Battery = 0x18
    GPS = 0x19
    ACS = 0x1A
    DxWiFi = 0x1B
    StarTracker = 0x1C


def opd_help():
    '''Print help message for opd commands'''

    print('opd commands:')
    print('  opd sysenable')
    print('  opd sysdisable')
    print('  opd scan')
    print('  opd enable OPD_ADDR')
    print('  opd disable OPD_ADDR')
    print('  opd reset OPD_ADDR')
    print('  opd status OPD_ADDR')
    print('')
    print('OPD_ADDR values:')
    for i in OPDAddr:
        print('  {} {}'.format(i.name, hex(i.value)))


def opd_cmd(conn, inp: str) -> None:
    '''Run a opd command'''

    inps = inp.split(' ')
    cmd_str = inps[0].lower()
    cmd = None
    args = None

    if cmd_str == 'help':
        opd_help()
        return
    elif cmd_str == 'sysenable':
        cmd = '/OreSat0/OPDSysenable'
    elif cmd_str == 'sysdisable':
        cmd = '/OreSat0/OPDSysdisable'
    elif cmd_str == 'scan':
        cmd = '/OreSat0/OPDScan'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name, 'Reset': True}
    elif cmd_str == 'enable':
        cmd = '/OreSat0/OPDPower'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name, 'Command': 'On'}
    elif cmd_str == 'disable':
        cmd = '/OreSat0/OPDPower'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name, 'Command': 'Off'}
    elif cmd_str == 'reset':
        cmd = '/OreSat0/OPDReset'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name}
    elif cmd_str == 'status':
        cmd = '/OreSat0/OPDStatus'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name}
    else:
        raise ValueError('not a valid opd command')

    try:
        # Issue the command via Yamcs client
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print(f'[FROM (Yamcs Client)]: `{ack.status}`')

        # Determine the response data type
        unpack_formats = {
            'sysenable': '<I',
            'sysdisable': '<I',
            'scan': '<I',
            'enable': '<i',
            'disable': '<i',
            'reset': '<i'
        }

        # Get downlink response
        response = get_yamcs_downlink_response()
        unpack_format = unpack_formats.get(cmd_str, '<p')
        message = struct.unpack(data_type_unpack_format, response)[0]
        print(f'EDL: {message}')
    except YamcsError as exc:
        print(exc)
