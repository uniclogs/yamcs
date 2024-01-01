#!/usr/bin/env python

from socket import socket, AF_INET, SOCK_DGRAM
from collections import deque
from spacepackets.cfdp.conf import ByteFieldU8
from spacepackets.cfdp.defs import ChecksumType, TransmissionMode, ConditionCode
from spacepackets.cfdp.pdu import (
    MetadataParams,
    MetadataPdu,
    FileDataPdu,
    EofPdu,
    PduConfig,
)
from  spacepackets.cfdp.pdu.file_directive import DirectiveType
from spacepackets.cfdp.pdu.helper import PduFactory
from spacepackets.cfdp.pdu.ack import AckPdu, TransactionStatus
from spacepackets.cfdp.pdu.file_data import FileDataParams
from crcmod.predefined import PredefinedCrc

LOCAL_ID = ByteFieldU8(1)
REMOTE_ID = ByteFieldU8(2)

file_transfer_queue = deque()

src_name = "./sat/oreflat.png"
dest_name = "oreflat.png"
file_data = "A picture of OreFlat!"

seq_num = ByteFieldU8(0)
pdu_conf = PduConfig(LOCAL_ID, REMOTE_ID, seq_num, TransmissionMode.UNACKNOWLEDGED)
metadata_params = MetadataParams(True, ChecksumType.MODULAR, len(file_data), src_name, dest_name)
metadata_pdu = MetadataPdu(pdu_conf, metadata_params)

file_transfer_queue.append(metadata_pdu)

params = FileDataParams(file_data.encode(), 0)
fd_pdu = FileDataPdu(pdu_conf, params)

file_transfer_queue.append(fd_pdu)

crc_calculator = PredefinedCrc("crc32")
crc_calculator.update(file_data.encode())
crc_32 = crc_calculator.digest()
eof_pdu = EofPdu(pdu_conf, crc_32, len(file_data))
file_transfer_queue.append(eof_pdu)

SAT_ADDR = ('127.0.0.1', 40001)
GS_ADDR = ('127.0.0.1', 40002)

# Setup the transports
with socket(AF_INET, SOCK_DGRAM) as sat_udp, \
     socket(AF_INET, SOCK_DGRAM) as gs_udp:

    # Receive CFDP Requests
    sat_udp.bind(SAT_ADDR)
    gs_udp.connect(GS_ADDR)

    while True:
        raw_packet = sat_udp.recv(2048)
        pdu = PduFactory.from_raw(raw_packet)
        pdu_t = pdu.directive_type
        print(f'Got the following {DirectiveType(pdu_t).name} packet:\n{pdu}\n')

        ACKABLE_PDUS = (DirectiveType.EOF_PDU, DirectiveType.FINISHED_PDU)
        if(pdu_t in ACKABLE_PDUS and pdu.transmission_mode is TransmissionMode.ACKNOWLEDGED):
            ack_pdu = AckPdu(pdu_conf, pdu_t, ConditionCode.NO_ERROR, TransactionStatus.ACTIVE)
            gs_udp.send(ack_pdu.pack())
            print(f'Sent ACK: {ack_pdu}')

    # Send CFDP Requests
    #gs_udp.connect(GS_ADDR)
    #for idx, pdu in enumerate(file_transfer_queue):
    #   gs_udp.send(pdu.pack())
