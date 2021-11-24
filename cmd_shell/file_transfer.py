
import os
import socket
import struct

UPLINK_IP_ADDR = 10016
DOWNLINK_IP_ADDR = 10025
SEGMENT_LEN = 1024
USLP_HEADER_LEN = 8
FILENAME_MAX_LEN = 32
FILE_OFFSET_LEN = 4
SEGMENT_DATA_LEN = 4


def file_upload(filepath: str, timeout: float = 1.0, retry: int = 5) -> None:
    '''Upload file to OreSat in segments

    Segment definition: 8 bytes for USLP header, 32 bytes for filename buffer,
    4 bytes for file offset, 4 bytes for length, then the payload. Everything
    in little endian.

    Parameters
    ----------
    filepath: str
        Filepath to local file to upload to OreSat.
    timeout: float
        Timeout before resending last segment in seconds.
    retry: int
        Maximum times to retry to resend the same segment before giving up.
    '''

    filename = os.path.basename(filepath)

    if len(filename) > FILENAME_MAX_LEN:
        raise ValueError('Filename exceeds max length of 32')

    downlink_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    downlink_socket.bind(('127.0.0.1', DOWNLINK_IP_ADDR))
    uplink_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    filename_bytes = filename.encode('utf-8') + b'\x00' * (FILENAME_MAX_LEN -
                                                           len(filename))

    read_len = SEGMENT_LEN - (USLP_HEADER_LEN + FILENAME_MAX_LEN +
                              FILE_OFFSET_LEN + SEGMENT_DATA_LEN)

    segments = []
    with open(filepath, 'rb') as fptr:
        segment = fptr.read(read_len)
        while len(segment):
            segments.append(segment)
            segment = fptr.read(read_len)

    i = 0
    offset = 0
    for seg in segments:
        fails = 0

        uslp_header = b'\xC4\xF5\x38\x02' + SEGMENT_LEN.to_bytes(2, 'little') + b'\x00\xE5'
        offset_bytes = offset.to_bytes(4, 'little')
        seg_len = len(seg).to_bytes(4, 'little')
        packet = uslp_header + filename_bytes + offset_bytes + seg_len + seg

        while True:  # keep sending until successful or retry limit is hit
            if retry == fails:
                raise Exception('retry failed 5 times')

            uplink_socket.sendto(packet, ('127.0.0.1', UPLINK_IP_ADDR))
            print('send segment', i)
            downlink_socket.settimeout(timeout)
            try:
                data_raw, _ = downlink_socket.recvfrom(4096)
            except Exception:
                print('fail', fails, ': reply timeout')
                fails += 1
                continue

            try:
                reply = struct.unpack('I', data_raw)
            except Exception:
                print('fail', fails, ': struct unpack failed')
                fails += 1
                continue

            if reply[0] != len(packet):
                print('fail ', fails, ': reply len failed')
                fails += 1
            else:
                break  # segment sent was successful

        i += 1
        offset += len(packet)
