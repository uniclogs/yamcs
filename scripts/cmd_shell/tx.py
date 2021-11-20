
from yamcs.core.exceptions import YamcsError


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
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print(ack.status)
    except YamcsError as exc:
        print(exc)
