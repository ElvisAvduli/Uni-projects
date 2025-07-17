from socket import *
import struct
import threading

def handle_multiplication(numbers):
    result = 1
    for num in numbers:
        result *= num
    return result

def handle_average(numbers):
    return sum(numbers) / len(numbers)

def handle_subtraction(set1, set2):
    return [a - b for a, b in zip(set1, set2)]

def client_handler(connectionSocket, addr):
    try:
        print(f"Handling connection from: {addr}")
        
        # Receive the header (operation_type and num_values)
        header = connectionSocket.recv(2)
        operation_type, num_values = struct.unpack('!BB', header)

        # Receive the data (values)
        data = connectionSocket.recv(num_values * 4)
        numbers = list(struct.unpack(f'!{num_values}i', data))

        # Perform the operation
        if operation_type == 1:  # Multiplication
            if not all(-5 <= num <= 5 for num in numbers):
                raise ValueError("Numbers for multiplication must be between -5 and 5")
            result = handle_multiplication(numbers)
            response_data = struct.pack('!i', result)
        elif operation_type == 2:  # Average
            if not all(0 <= num <= 200 for num in numbers):
                raise ValueError("Numbers for average must be between 0 and 200")
            result = handle_average(numbers)
            response_data = struct.pack('!f', result)
        elif operation_type == 3:  # Subtraction
            if not all(0 <= num <= 60000 for num in numbers):
                raise ValueError("Numbers for subtraction must be between 0 and 60000")
            set1 = numbers[:len(numbers)//2]
            set2 = numbers[len(numbers)//2:]
            result = handle_subtraction(set1, set2)
            response_data = struct.pack(f'!{len(result)}i', *result)
        else:
            raise ValueError("Invalid operation type")

        # Send the result back to the client
        response_header = struct.pack('!BB', operation_type, 0)  # Status code 0 for success
        connectionSocket.send(response_header + response_data)

    except Exception as e:
        # Send an error message to the client
        error_message = str(e)
        response_header = struct.pack('!BB', operation_type, 1)  # Status code 1 for error
        connectionSocket.send(response_header + error_message.encode())

    finally:
        connectionSocket.close()
        print(f"Connection with {addr} closed")

def main():
    serverPort = 12000
    serverSocket = socket(AF_INET, SOCK_STREAM)
    serverSocket.bind(('', serverPort))
    serverSocket.listen(5)  # Increased backlog for multiple connections
    print('The server is ready to receive')

    try:
        while True:
            connectionSocket, addr = serverSocket.accept()
            print(f"New connection from: {addr}")
            
            # Create a new thread to handle the client
            client_thread = threading.Thread(
                target=client_handler,
                args=(connectionSocket, addr)
            )
            client_thread.daemon = True  # Thread will exit when main program exits
            client_thread.start()
            
    except KeyboardInterrupt:
        print("Server is shutting down...")
    finally:
        serverSocket.close()

if __name__ == "__main__":
    main()