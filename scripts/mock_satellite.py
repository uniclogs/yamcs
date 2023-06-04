import socket
import threading
import time
from spacepackets.cfdp.pdu import FileDataPdu, MetadataPdu, AckPdu, header, file_directive
import struct

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



# Telemetry Data Link (TM)
def handle_tm_data_link(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((host, port))
        s.listen()
        print(f"TM Listening on {host}:{port}")
        conn, addr = s.accept()
        with conn:
            print(f"TM Connection from {addr}")

            while True:
                data = conn.recv(1024)
                if not data:
                    break
                print(f"TM Received: {data}")


# Telecommand Data Link (TC)
def handle_tc_data_link(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((host, port))
        s.listen()
        print(f"TC Listening on {host}:{port}")
        conn, addr = s.accept()
        with conn:
            print(f"TC Connection from {addr}")
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                print(f"TC Received: {data}")

                # Parse the received PDU
                # CCSDS Packet
                ccsds_packet = CcsdsPacket(data)

                if ccsds_packet.ccsds_apid == 2045:
                    print("Received CFDP PDU")
                    ccsds_packet_payload = ccsds_packet.get_data_payload()
                    cfdp_header = header.PduHeader.unpack(ccsds_packet_payload)
                    cfdp_pdu = cfdp_header.pdu_type

                    # 0 - File Directive PDU
                    if cfdp_pdu == 0:
                        print("Received File Directive PDU")
                        file_directive_header = file_directive.FileDirectivePduBase.unpack(ccsds_packet_payload)
                        directive_type = file_directive_header.directive_type

                        # 7 - Metadata PDU
                        if directive_type == 7:
                            print("Received Metadata PDU")
                            pdu = MetadataPdu.unpack(ccsds_packet_payload)
                            print(pdu)
                            pass
                        elif directive_type == 6:
                            print("Received Ack PDU")
                            pass
                        elif directive_type == 5:
                            print("Received Finished PDU")
                            pass
                        elif directive_type == 4:
                            print("Received EOF PDU")
                            pass

                    # 1 - File Data PDU
                    elif cfdp_pdu == 1:
                        print("Received File Data PDU")
                        pdu = FileDataPdu.unpack(ccsds_packet_payload)
                        print(pdu)
                    else:
                        print("Received Unknown PDU")
                        pass


def handle_py_tc_data_link(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((host, port))
        s.listen()
        print(f"PyTC Listening on {host}:{port}")
        conn, addr = s.accept()
        with conn:
            print(f"PyTC Connection from {addr}")
            while True:
                data = conn.recv(1024)
                if not data:
                    break
                print(f"PyTC Received: {data}")
def handle_metadata_pdu(pdu):
    # Start a new file transfer
    file_name = pdu.source_file_name
    print(f"Starting transfer of {file_name}")

def handle_file_data_pdu(pdu):
    # Append data to the file
    file_data = pdu.file_data_field
    print(f"Received file data: {file_data}")

def handle_ack_pdu(pdu):
    # Handle the acknowledgement
    print(f"Received ACK for transaction {pdu.transaction_seq_num}")

# Alive message
def print_alive_message():
    while True:
        print("Mock satellite is alive.")
        time.sleep(10)


# Main
def main():
    host = "0.0.0.0"
    tm_port = 10010
    tc_port = 10026
    py_tc_port = 10027

    tm_thread = threading.Thread(target=handle_tm_data_link, args=(host, tm_port))
    tc_thread = threading.Thread(target=handle_tc_data_link, args=(host, tc_port))
    py_tc_port = threading.Thread(target=handle_py_tc_data_link, args=(host, py_tc_port))

    alive_thread = threading.Thread(target=print_alive_message)

    tm_thread.start()
    tc_thread.start()
    py_tc_port.start()
    alive_thread.start()

    tm_thread.join()
    tc_thread.join()
    py_tc_port.join()
    alive_thread.join()

if __name__ == "__main__":
    main()
