import socket
import threading
import time
import struct


def create_cfdp_header(transaction_sequence_number, pdu_type, direction):
    ccsds_packet_version = 0  # CCSDS Packet version number (3 bits)
    pdu_type = pdu_type  # PDU Type (1 bit): 0 for File Directive, 1 for File Data
    direction = direction  # Direction (1 bit): 0 for toward file receiver, 1 for toward file sender
    ccsds_packet_type = 1  # CCSDS Packet Type (1 bit): 1 for CFDP
    spare = 0  # Spare field (2 bits)

    primary_header = (ccsds_packet_version << 5) | (pdu_type << 4) | (direction << 3) | (ccsds_packet_type << 2) | spare

    # Transaction Sequence Number (TSN)
    tsn = transaction_sequence_number

    return struct.pack(">H", (primary_header << 8) | tsn)


def send_cfdp_file(conn, file_name, file_content):
    transaction_sequence_number = 1

    # Create CFDP header for File Directive (Metadata) PDU
    cfdp_header = create_cfdp_header(transaction_sequence_number, 0, 0)

    # Create Metadata PDU
    pdu = cfdp_header + b'\x01' + struct.pack(">I", len(file_content)) + file_name.encode() + b'\x00'

    # Send Metadata PDU
    conn.sendall(pdu)

    # Create CFDP header for File Data PDU
    cfdp_header = create_cfdp_header(transaction_sequence_number, 1, 0)

    # Create File Data PDU
    pdu = cfdp_header + b'\x01' + file_content

    # Send File Data PDU
    conn.sendall(pdu)


# Telemetry Data Link (TM)
def handle_tm_data_link(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((host, port))
        s.listen()
        print(f"TM Listening on {host}:{port}")
        conn, addr = s.accept()
        with conn:
            print(f"TM Connection from {addr}")

            # Send the sample file to Yamcs server
            file_name = "sample_file.txt"
            file_content = b"This is a sample file from mock_satellite."
            send_cfdp_file(conn, file_name, file_content)

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

    tm_thread = threading.Thread(target=handle_tm_data_link, args=(host, tm_port))
    tc_thread = threading.Thread(target=handle_tc_data_link, args=(host, tc_port))
    alive_thread = threading.Thread(target=print_alive_message)

    tm_thread.start()
    tc_thread.start()
    alive_thread.start()

    tm_thread.join()
    tc_thread.join()
    alive_thread.join()

if __name__ == "__main__":
    main()
