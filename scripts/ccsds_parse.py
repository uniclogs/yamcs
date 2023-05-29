import struct
from spacepackets.cfdp.pdu import Pdu
class CcsdsPacket:
    def __init__(self, raw_packet):
        self.raw_packet = raw_packet
        self._parse_header()

    def _parse_header(self):
        ccsds_header = struct.unpack('>HHH', self.raw_packet[:6])

        self.ccsds_version = (ccsds_header[0] & 0xE000) >> 13
        self.ccsds_type = (ccsds_header[0] & 0x1000) >> 12
        self.ccsds_secondary_header_flag = (ccsds_header[0] & 0x0800) >> 11
        self.ccsds_apid = ccsds_header[0] & 0x07FF
        self.ccsds_sequence_flags = (ccsds_header[1] & 0xC000) >> 14
        self.ccsds_sequence_number = ccsds_header[1] & 0x3FFF
        self.ccsds_data_length = ccsds_header[2]

    def print_header_info(self):
        print(f'CCSDS Version: {self.ccsds_version}')
        print(f'CCSDS Type: {self.ccsds_type}')
        print(f'CCSDS Secondary Header Flag: {self.ccsds_secondary_header_flag}')
        print(f'CCSDS APID: {self.ccsds_apid}')
        print(f'CCSDS Sequence Flags: {self.ccsds_sequence_flags}')
        print(f'CCSDS Sequence Number: {self.ccsds_sequence_number}')
        print(f'CCSDS Data Length: {self.ccsds_data_length}')

    def get_data_payload(self):
        # The CCSDS header is 6 bytes, so the data starts after that
        # Note: This does not account for a secondary header, if present
        data_start = 6

        # The data length field is the number of bytes of user data,
        # not including the 6-byte primary header or the 1-byte length field itself,
        # so we add 7 to get the end of the data
        data_end = data_start + self.ccsds_data_length + 1

        # Extract and return the data
        return self.raw_packet[data_start:data_end]