import binascii
import hashlib
import hmac
from enum import IntEnum, auto

from spacepackets.uslp.defs import UslpInvalidRawPacketOrFrameLen
from spacepackets.uslp.frame import TransferFrame, TransferFrameDataField, TfdzConstructionRules, \
    UslpProtocolIdentifier, VarFrameProperties, FrameType
from spacepackets.uslp.header import PrimaryHeader, SourceOrDestField, ProtocolCommandFlag, \
    BypassSequenceControlFlag

from spacepackets.cfdp.pdu.header import PduHeader
from spacepackets.cfdp.defs import PduType, Direction
from spacepackets.cfdp.conf import PduConfig
from spacepackets.cfdp.pdu.file_directive import FileDirectivePduBase, DirectiveType
from spacepackets.cfdp.pdu.metadata import MetadataPdu

class CFDPProtocals:
    '''The CFDP protocol identifiers. Based on CCSDS 727.0-B-5'''

    def __init__(self):
        pass

def crc16_bytes(data: bytes) -> bytes:
    '''Helper function for generating the crc16 of a message as bytes'''
    return binascii.crc_hqx(data, 0).to_bytes(2, 'little')


class UslpError(Exception):
    '''Error with UslpBase'''


class UslpBase:

    SPACECRAFT_ID = 0x4F53  # aka "OS" in ascii

    PRIMARY_HEADER_LEN = 7
    SEQ_NUM_LEN = 4
    DFH_LEN = 1
    HMAC_LEN = 32
    FECF_LEN = 2
    _TC_MIN_LEN = PRIMARY_HEADER_LEN + SEQ_NUM_LEN + DFH_LEN + HMAC_LEN + FECF_LEN

    FRAME_PROPS = VarFrameProperties(
        has_insert_zone=True,
        has_fecf=True,
        truncated_frame_len=0,
        insert_zone_len=SEQ_NUM_LEN,
        fecf_len=FECF_LEN,
    )

    def __init__(self, hmac_key: bytes, sequence_number: int):

        self._hmac_key = b'\x00'
        self.hmac_key = hmac_key
        self._seq_num = sequence_number

    def _gen_hmac(self, message: bytes) -> bytes:

        return hmac.digest(self._hmac_key, message, hashlib.sha3_256)

    def _parse_packet(self, packet: bytes, src_dest: SourceOrDestField) -> bytes:

        if len(packet) < self._TC_MIN_LEN:
            raise UslpError(f'EDL packet too short: {len(packet)}')

        crc16_raw = packet[-self.FECF_LEN:]
        crc16_raw_calc = crc16_bytes(packet[:-self.FECF_LEN])
        if crc16_raw_calc != crc16_raw:
            raise UslpError(f'invalid FECF: {crc16_raw} vs {crc16_raw_calc}')

        try:
            frame = TransferFrame.unpack(packet, FrameType.VARIABLE, self.FRAME_PROPS)
        except UslpInvalidRawPacketOrFrameLen:
            raise UslpError('USLP invalid packet or frame length')

        if frame.insert_zone > self.sequence_number_bytes:
            raise UslpError(f'invalid sequence number: {frame.insert_zone} vs '
                           f'{self.sequence_number_bytes}')

        payload = frame.tfdf.tfdz[:-self.HMAC_LEN]
        hmac_bytes = frame.tfdf.tfdz[-self.HMAC_LEN:]
        hmac_bytes_calc = self._gen_hmac(payload)

        if hmac_bytes != hmac_bytes_calc:
            raise UslpError(f'invalid HMAC {hmac_bytes.hex()} vs {hmac_bytes_calc.hex()}')

        return payload

    def _generate_packet(self, payload: bytes, src_dest: SourceOrDestField) -> bytes:

        # USLP transfer frame total length - 1
        frame_len = len(payload) + self._TC_MIN_LEN - 1

        frame_header = PrimaryHeader(
            scid=self.SPACECRAFT_ID,
            map_id=0,
            vcid=1, # CFDP Yamcs vcid, EDL on VC 0
            src_dest=src_dest,
            frame_len=frame_len,
            vcf_count_len=0,
            op_ctrl_flag=False,
            prot_ctrl_cmd_flag=ProtocolCommandFlag.USER_DATA,
            bypass_seq_ctrl_flag=BypassSequenceControlFlag.SEQ_CTRLD_QOS,
        )

        tfdz = payload + self._gen_hmac(payload)

        tfdf = TransferFrameDataField(
            tfdz_cnstr_rules=TfdzConstructionRules.VpNoSegmentation,
            uslp_ident=UslpProtocolIdentifier.MISSION_SPECIFIC_INFO_1_MAPA_SDU,
            tfdz=tfdz,
        )

        frame = TransferFrame(header=frame_header, tfdf=tfdf,
                              insert_zone=self.sequence_number_bytes)
        packet = frame.pack(frame_type=FrameType.VARIABLE)
        packet += crc16_bytes(packet)

        self._seq_num += 1
        self._seq_num %= 0xFF_FF_FF_FF

        return packet

    @property
    def sequence_number(self) -> int:

        return self._seq_num

    @property
    def sequence_number_bytes(self) -> bytes:

        return self._seq_num.to_bytes(self.SEQ_NUM_LEN, 'little')

    @property
    def hmac_key(self) -> bytes:

        return self._hmac_key

    @hmac_key.setter
    def hmac_key(self, value: bytes or bytearray):

        if not isinstance(value, bytes) and not isinstance(value, bytearray):
            raise UslpError('invalid HMAC key data type')

        self._hmac_key = bytes(value)


class UlspServer(UslpBase):

    def parse_request(self, packet: bytes) -> bytes:

        return self._parse_packet(packet, src_dest=SourceOrDestField.SOURCE)

    def generate_response(self, payload: bytes) -> bytes:

        return self._generate_packet(payload, src_dest=SourceOrDestField.DEST)


class UlspClient(UslpBase):

    def generate_request(self, payload: bytes) -> bytes:

        return self._generate_packet(payload, src_dest=SourceOrDestField.SOURCE)

    def parse_response(self, packet: bytes) -> bytes:

        return self._parse_packet(packet, src_dest=SourceOrDestField.DEST)

class FileWriter:
    def __init__(self, filename):
        pass

class CfdpClientTransfer:
    def __init__(self, raw_data_packet):
        self.state = None
        self.raw_data_packet = raw_data_packet
        self.packet_payload = UlspClient.parse_response(self.raw_data_packet)

    def check_origins(self, header):
        # currently as a test we are only sending to entity 2
        # configured in yamcs.oresat0.yaml
        if header.dest_entity_id.value == 2:
            # check if this is from yamcs ground station:
            if header.source_entity_id.value == 1:
                return True
        return False

    # todo: non hardcoded entity ids
    def should_process(self, header):
        # check direction of packet
        if header.pdu_conf.direction == Direction.TOWARD_FILE_RECEIVER:
            return self.check_origins(header)
        return False

    def handle_raw_packet(self):
        # peek at header to determine if file data or file directive
        cfdp_header = PduHeader.unpack(self.packet_payload)

        # if this is from the ground station and the packet is for us
        if self.should_process(cfdp_header):
            if cfdp_header.pdu_type == PduType.FILE_DATA:
                self.handle_file_data()
            elif cfdp_header.pdu_type == PduType.FILE_DIRECTIVE:
                self.handle_file_directive()
            else:
                # todo: throw error
                pass

    def handle_file_data(self):
        pass

    def handle_file_directive(self):
        directive_type = FileDirectivePduBase.unpack(self.packet_payload).directive_code

        if directive_type == DirectiveType.EOF_PDU:
            pass
        elif directive_type == DirectiveType.FINISHED_PDU:
            pass
        elif directive_type == DirectiveType.ACK_PDU:
            pass
        elif directive_type == DirectiveType.METADATA_PDU:
            pass
        elif directive_type == DirectiveType.NAK_PDU:
            pass
        elif directive_type == DirectiveType.PROMPT_PDU:
            pass
        elif directive_type == DirectiveType.KEEP_ALIVE_PDU:
            pass
        elif directive_type == DirectiveType.NONE:
            pass
        else:
            # todo: throw error
            pass

    def handle_metadata_pdu(self):
        pdu = MetadataPdu.unpack(self.packet_payload)
