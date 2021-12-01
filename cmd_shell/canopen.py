import struct
from enum import IntEnum
from typing import NamedTuple
from yamcs.core.exceptions import YamcsError
from .common import str2int, str2intenum, get_yamcs_downlink_response


class ObjTypeData(NamedTuple):
    description: str
    write_cmd: str


OBJ_TYPES = {
    'b': ObjTypeData('boolean', '/OreSat0/SDOWriteBool'),
    'i8': ObjTypeData('8-bit signed int', '/OreSat0/SDOWriteInt8'),
    'i16': ObjTypeData('16-bit signed int', '/OreSat0/SDOWriteInt16'),
    'i32': ObjTypeData('32-bit signed int', '/OreSat0/SDOWriteInt32'),
    'u8': ObjTypeData('8-bit unsigned int', '/OreSat0/SDOWriteUint8'),
    'u16': ObjTypeData('16-bit unsigned int', '/OreSat0/SDOWriteUint16'),
    'u32': ObjTypeData('32-bit unsigned int', '/OreSat0/SDOWriteUint32'),
}


class NodeId(IntEnum):
    '''All valid CANopen node IDs. Names are in the format Yamcs expects'''

    C3 = 0x01
    Battery = 0x04
    Solar0 = 0x0C
    Solar1 = 0x0F
    Solar2 = 0x14
    Solar3 = 0x18
    StarTracker = 0x2C
    GPS = 0x34
    ACS = 0x38
    DxWiFi = 0x4C


def node_help():
    '''Print help message for node commands'''

    print('node commands:')
    print('  node enable NODE_ID')
    print('  node disable NODE_ID')
    print('  node status NODE_ID')
    print('')
    print('NODE_ID values:')
    for i in NodeId:
        print('  {} {}'.format(i.name, hex(i.value)))


def node_cmd(conn, inp: str) -> None:
    '''Run a node command'''

    inps = inp.split(' ')
    cmd = None
    cmd_str = inps[0].lower()
    args = None

    if cmd_str == 'help':
        node_help()
        return
    elif cmd_str == 'enable':
        cmd = '/OreSat0/NodeEnable'
        node_id = str2intenum(inps[1], NodeId)
        args = {'NodeId': node_id.name, 'Enable': 'Enable'}
    elif cmd_str == 'disable':
        cmd = '/OreSat0/NodeEnable'
        node_id = str2intenum(inps[1], NodeId)
        args = {'NodeId': node_id.name, 'Enable': 'Disable'}
    elif cmd_str == 'status':
        cmd = '/OreSat0/NodeStatus'
        node_id = str2intenum(inps[1], NodeId)
        args = {'NodeId': node_id.name}
    else:
        raise ValueError('not a valid node command')

    try:
        # Issue the command via Yamcs client
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print('  Yamcs response:', ack.status)

        # Determine the response data type
        unpack_formats = {
            'enable': '<i',
            'disable': '<i',
        }

        # Get downlink response
        if(unpack_formats.get(cmd_str) is not None):
            response = get_yamcs_downlink_response()
            unpack_format = unpack_formats.get(cmd_str, '<p')
            message = struct.unpack(unpack_format, response)[0]
            print('  Downlink packet:',  hex(message))
    except YamcsError as exc:
        print(exc)


def sdo_help():
    '''Print help message for sdo commands'''

    print('SDO commands:')
    print('  sdo write NODE_ID INDEX SUBINDEX TYPE VALUE')
    print('')
    print('NODE_ID values:')
    for i in NodeId:
        print('  {} {}'.format(i.name, hex(i.value)))
    print('TYPE values:')
    for i in OBJ_TYPES:
        print('  {}: {}'.format(i, OBJ_TYPES[i].description))


def sdo_cmd(conn, inp: str) -> None:
    '''Run a node command'''

    inps = inp.split(' ')
    cmd = None
    cmd_str = inps[0].lower()
    args = None
    value = None

    if cmd_str == 'help':
        node_help()
        return

    node_id = str2intenum(inps[1], NodeId)
    index = str2int(inps[2])
    subindex = str2int(inps[3])
    obj_type = inps[4]

    if cmd_str in ['w', 'write']:
        cmd = OBJ_TYPES[obj_type].write_cmd
    else:
        raise ValueError('not a valid node command')

    # check for valid index and subindex
    if index < 0 or index > 0x8000:
        raise ValueError('invalid index, must be >= 0 and <= 0x8000')
    if subindex < 0 or subindex > 0x127:
        raise ValueError('invalid subindex, must be >= 0 and <= 0x127')

    # parse value
    if obj_type == 'b':
        value = inps[5].lower() in ['t', 'true']
    elif obj_type in ['i8', 'i16', 'i32', 'u8', 'u16', 'u32']:
        value = str2int(inps[5])
    if value is None:
        raise ValueError('not a valid value')

    args = {
        'NodeId': node_id.name,
        'ODIndex': index,
        'ODSubIndex': subindex,
        'Value': value
    }

    try:
        # Issue the command via Yamcs client
        command = conn.issue(cmd, args=args)
        ack = command.await_acknowledgment('Acknowledge_Sent')
        print('  Yamcs response:', ack.status)

        # Get downlink response
        response = get_yamcs_downlink_response()
        message = struct.unpack('<I', response)
        print('  Downlink packet:',  hex(message))
    except YamcsError as exc:
        print(exc)
