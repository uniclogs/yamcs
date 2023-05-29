import socket
import threading
import time
from spacepackets.cfdp.pdu import FileDataPdu, MetadataPdu, AckPdu
from spacepackets.ccsds.spacepacket import SpacePacket



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

                # # Parse the received PDU
                # space_packet = SpacePacket(packet_data=data)
                # if space_packet.get_apid_from_raw_space_packet() == 2045:
                #     print("Received CFDP PDU")
                #     cfdp_pdu = space_packet.payload
                #
                #     # If the PDU is a Metadata PDU, start a new file transfer
                #     if isinstance(cfdp_pdu, MetadataPdu):
                #         handle_metadata_pdu(cfdp_pdu)
                #
                #     # If the PDU is a File Data PDU, append data to the file
                #     elif isinstance(cfdp_pdu, FileDataPdu):
                #         handle_file_data_pdu(cfdp_pdu)
                #
                #     # If the PDU is an ACK PDU, handle the acknowledgement
                #     elif isinstance(cfdp_pdu, AckPdu):
                #         handle_ack_pdu(cfdp_pdu)

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
