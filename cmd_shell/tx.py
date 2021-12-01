import struct
from yamcs.core.exceptions import YamcsError
from .common import get_yamcs_downlink_response


def tx_help() -> None:
    '''Print help message for Tx commands'''

    print('Tx commands:')
    print('  enable')
    print('  disable')


def tx_cmd(conn, inp: str) -> None:
    '''Run a Tx command'''

    cmd = None

    if inp.lower() == 'help':
        tx_help()
        return

    if inp.lower() == 'enable':
        cmd = '/OreSat0/C3TxControl'
        args = {'Command': 'Enable'}
    elif inp.lower() == 'disable':
        cmd = '/OreSat0/C3TxControl'
        args = {'Command': 'Disable'}
    else:
        raise ValueError('not a valid tx command')

    try:
        # Issue the command via Yamcs client
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print(f'  Yamcs response: `{ack.status}`')

        # Get downlink response
        response = get_yamcs_downlink_response()
        message = struct.unpack('<B', response)[0]
        print(f'  Downlink packet: `{hex(message)}`')
    except YamcsError as exc:
        print(exc)
