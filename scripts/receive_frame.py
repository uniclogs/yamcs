import socket

# Define entity ID and UDP port
satellite_entity_id = 2
satellite_port = 12345

# Create a UDP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Bind the socket to the port
sock.bind(('0.0.0.0', satellite_port))

print(f"Mock satellite {satellite_entity_id} listening on port {satellite_port}")

while True:
    # Receive data
    data, addr = sock.recvfrom(4096)

    # Print received data
    print(f"Received from {addr}: {data}")
