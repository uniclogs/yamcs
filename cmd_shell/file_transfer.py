import os
import struct
from . import UPLINK_SOCKET, \
              UPLINK_ADDR, \
              DOWNLINK_SOCKET, \
              BUFFER_SIZE

SEGMENT_LEN = 128
USLP_HEADER_LEN = 8
FILENAME_MAX_LEN = 32
FILE_OFFSET_LEN = 4
SEGMENT_DATA_LEN = 4


def file_upload(filepath: str,
                timeout: float = 0.0,
                retry: int = 0,
                start: int = 0) -> None:
    '''Upload file to OreSat in segments

    Segment definition: 8 bytes for USLP header, 32 bytes for filename buffer,
    4 bytes for file offset, 4 bytes for length, then the payload. Everything
    in little endian.

    Parameters
    ----------
    filepath: str
        Filepath to local file to upload to OreSat.
    timeout: float
        Timeout (in seconds) waiting for a response on the downlink before
        resending last segment again. If set to 0.0 the the respose will not
        be looked for.
    retry: int
        Maximum times to retry to resend the same segment before giving up. If
        set to 0 the segment will not be retried.
    start: int
        The segment to start on. Set to 0 todo the whole file.
    '''

    filename = os.path.basename(filepath)

    if len(filename) > FILENAME_MAX_LEN:
        raise ValueError('Filename exceeds max length of 32')

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

    print('  total segments to send:', len(segments))
    if start >= len(segments):
        raise ValueError('start is greater than the number of segments')

    old_timeout = DOWNLINK_SOCKET.gettimeout()
    DOWNLINK_SOCKET.settimeout(timeout)

    i = start
    offset = 0
    for seg in segments[start:]:
        fails = 0

        uslp_header = b'\xC4\xF5\x38\x02' + SEGMENT_LEN.to_bytes(2, 'little') + b'\x00\xE5'
        offset_bytes = offset.to_bytes(4, 'little')
        seg_len = len(seg).to_bytes(4, 'little')
        packet = uslp_header + filename_bytes + offset_bytes + seg_len + seg

        if timeout == 0:
            UPLINK_SOCKET.sendto(packet, UPLINK_ADDR)
            print('  send segment', i)

        # why doesn't python have do-while loops?
        while timeout != 0:
            # keep sending until successful or retry limit is hit
            if retry != 0 and fails >= retry:  # loop forever if set 0
                DOWNLINK_SOCKET.settimeout(old_timeout)
                raise Exception('retry failed ' + str(retry) + ' time(s)')

            UPLINK_SOCKET.sendto(packet, UPLINK_ADDR)
            print('  send segment', i)

            try:
                data_raw, _ = DOWNLINK_SOCKET.recvfrom(BUFFER_SIZE)
            except Exception:
                print('  fail', fails, ': reply timeout')
                fails += 1
                continue

            try:
                reply = struct.unpack('<i', data_raw[8:])
            except Exception:
                print('  fail', fails, ': struct unpack failed')
                fails += 1
                continue

            if reply[0] != len(seg):
                print('  fail ', fails, ': reply len failed')
                fails += 1
            else:
                print('  seg', i, 'ack')
                break  # segment sent was successful

        i += 1
        offset += len(seg)

    DOWNLINK_SOCKET.settimeout(old_timeout)
