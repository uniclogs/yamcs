
from enum import IntEnum

from yamcs.core.exceptions import YamcsError

from common import str2intenum


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
    print('  opd scan OPD_ADDR')
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
    cmd = None
    args = None

    if inps[0].lower() == 'help':
        opd_help()
        return

    if inps[0].lower() == 'sysenable':
        cmd = '/OreSat0/OPDSysenable'
    elif inps[0].lower() == 'sysdisable':
        cmd = '/OreSat0/OPDSysdisable'
    elif inps[0].lower() == 'scan':
        cmd = '/OreSat0/OPDScan'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name, 'Reset': True}
    elif inps[0].lower() == 'enable':
        cmd = '/OreSat0/OPDPower'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name, 'Command': 'On'}
    elif inps[0].lower() == 'disable':
        cmd = '/OreSat0/OPDPower'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name, 'Command': 'Off'}
    elif inps[0].lower() == 'reset':
        cmd = '/OreSat0/OPDReset'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name}
    elif inps[0].lower() == 'status':
        cmd = '/OreSat0/OPDStatus'
        opd_id = str2intenum(inps[1], OPDAddr)
        args = {'OPDAddr': opd_id.name}
    else:
        raise ValueError('not a valid opd command')

    try:
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print(ack.status)
    except YamcsError as exc:
        print(exc)
