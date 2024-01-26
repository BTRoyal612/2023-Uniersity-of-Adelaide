import sys
import socket

class Reader:

    def __init__(self, server_host, server_port, book_file):
        self.server_host = server_host
        self.server_port = server_port
        self.book_file = book_file

    def send_book(self):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as client_socket:
            # Connect to the server
            client_socket.connect((self.server_host, self.server_port))

            # Send book title first (assuming first line of the file is the title)
            with open(self.book_file, 'r', encoding='utf-8') as file:
                book_title = file.readline().strip()
                client_socket.sendall(book_title.encode('utf-8'))

                # Wait for acknowledgment from the server before sending content
                ack = client_socket.recv(1024)
                if ack != b'ACK':
                    print("Error: Did not receive acknowledgment for the book title!")
                    return

                # Send the rest of the book line by line
                for line in file:
                    client_socket.sendall(line.encode('utf-8'))
                    
                    # Wait for acknowledgment after each line
                    ack = client_socket.recv(1024)
                    if ack != b'ACK':
                        print(f"Error: Did not receive acknowledgment for line: {line}")
                        break

if __name__ == "__main__":
    if len(sys.argv) != 4:
        print("Usage: python reader.py <SERVER_HOST> <SERVER_PORT> <BOOK_FILE_PATH>")
        sys.exit(1)

    SERVER_HOST = sys.argv[1]
    SERVER_PORT = int(sys.argv[2])
    BOOK_FILE = sys.argv[3]

    reader = Reader(SERVER_HOST, SERVER_PORT, BOOK_FILE)
    reader.send_book()

# python reader.py <SERVER_HOST> <SERVER_PORT> <BOOK_FILE_PATH>