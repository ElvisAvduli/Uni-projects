from socket import *
import struct
from time import sleep
def send_request(operation_type, numbers):
    serverName = '127.0.0.1'
    serverPort = 12000
    clientSocket = socket(AF_INET, SOCK_STREAM)
    clientSocket.connect((serverName, serverPort))

    try:
        # Prepare the header (operation_type and num_values)
        header = struct.pack('!BB', operation_type, len(numbers))

        # Prepare the data (values)
        data = struct.pack(f'!{len(numbers)}i', *numbers)

        # Send the request to the server
        clientSocket.send(header + data)

        # Receive the response from the server
        response_header = clientSocket.recv(2)
        operation_type, status_code = struct.unpack('!BB', response_header)

        if status_code == 0:
            if operation_type == 1:  # Multiplication
                result = struct.unpack('!i', clientSocket.recv(4))[0]
            elif operation_type == 2:  # Average
                result = struct.unpack('!f', clientSocket.recv(4))[0]
            elif operation_type == 3:  # Subtraction
                num_results = len(numbers) // 2
                result = list(struct.unpack(f'!{num_results}i', clientSocket.recv(num_results * 4)))
            print(f"Result: {result}")
        else:
            error_message = clientSocket.recv(1024).decode()
            print(f"Error: {error_message}")

    finally:
        clientSocket.close()

if __name__ == "__main__":
    # Example usage
    send_request(1, [2, 3, 4])  # Multiplication
    send_request(2, [10, 20, 30])  # Average
    send_request(3, [5, 10, 15, 2, 4, 6])  # Subtraction