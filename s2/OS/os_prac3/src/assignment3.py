#!/usr/bin/env python3

import argparse
import socket
import threading

class Node:
    def __init__(self, data):
        self.data = data  # Contains line from the book
        self.next = None  # Points to next node in list
        self.book_next = None  # Points to next line from the same book
        # Next node that contains the search term; For Step 3
        self.next_frequent_search = None

class SharedLinkedList:
    def __init__(self):
        self.head = None  # Start of the list

    # Method to add data to the list
    def append(self, data, book_head):
        new_node = Node(data)
        if not self.head:
            self.head = new_node
            book_head.book_next = new_node
        else:
            # Traverse to the end of the list and append
            current = self.head
            while current.next:
                current = current.next
            current.next = new_node
            # Set book_next pointer
            if book_head.book_next:
                temp = book_head.book_next
                while temp.book_next:
                    temp = temp.book_next
                temp.book_next = new_node
            else:
                book_head.book_next = new_node

        # Print addition of the node
        print(f"Added node: {data}")

class PatternAnalysisThread(threading.Thread):
    def __init__(self, shared_linked_list, pattern):
        threading.Thread.__init__(self)
        self.shared_linked_list = shared_linked_list
        self.pattern = pattern
        self.lock = threading.Lock()
        self.output_lock = threading.Lock()
        self.output_event = threading.Event()

    def run(self):
        # Implement the analysis logic here
        count = self.count_pattern_occurrences()
        self.try_output_results(count)

    def count_pattern_occurrences(self):
        count = 0
        current_node = self.shared_linked_list.head
        while current_node:
            count += current_node.data.count(self.pattern)
            current_node = current_node.next
        return count

    def try_output_results(self, count):
        with self.output_lock:
            if not self.output_event.is_set():
                print(f"Pattern: {self.pattern} - Count: {count}")
                self.output_event.set()
                interval = 10  # For example, every 10 seconds
                threading.Timer(interval, self.output_event.clear).start()


class Server:
    def __init__(self, host, port, pattern):
        self.host = host
        self.port = port
        self.pattern = pattern
        self.shared_list = SharedLinkedList()
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server.bind((self.host, self.port))
        self.pattern_analysis_thread = self.initialize_pattern_analysis_thread()
        self.connection_count = 0
        self.active_connections = []
        self.stop_event = threading.Event()
        self.shared_list_lock = threading.Lock()

    def initialize_pattern_analysis_thread(self):
        # Initialize and return the pattern analysis thread
        pattern_thread = PatternAnalysisThread(self.shared_list, self.pattern)
        return pattern_thread

    # Client handling function
    def handle_client(self, client_socket):
        # Receive the title first
        book_title = client_socket.recv(1024).decode('utf-8')
        book_head = Node(book_title)
        
        # Acknowledge the title receipt
        client_socket.sendall(b'ACK')
        
        while True:
            data = client_socket.recv(1024)
            if len(data) < 1024:
                break

            # Append received data to the shared list using a lock
            with self.shared_list_lock:
                self.shared_list.append(data.decode('utf-8'), book_head)
            
            # Acknowledge the receipt of each line
            client_socket.sendall(b'ACK')


        client_socket.close()

        # Write received book to file
        self.write_received_book(book_head)
        
    def write_received_book(self, book_head):
        self.connection_count += 1
        filename = f"book_{self.connection_count:02}.txt"
        with open(filename, 'w', encoding='utf-8') as file:
            current = book_head
            while current:
                file.write(current.data + '\n')
                current = current.book_next
        print(f"Written received book to {filename}")

    def run_server(self):
        self.server.listen(10)  # Max 30 connections in the waiting queue
        print(f"Server started on port {self.port}")
        
        # Start the pattern analysis thread
        self.pattern_analysis_thread.start()

        while not self.stop_event.is_set():  # Use the event to check if we should stop
            try:
                client_socket, addr = self.server.accept()
                self.active_connections.append(client_socket)
                client_handler = threading.Thread(target=self.handle_client, args=(client_socket,))
                client_handler.start()
            except socket.timeout:
                continue  # If the accept call times out, check the stop event and possibly loop again

    def stop_server(self):
        self.stop_event.set()  # Signal all threads to stop

        # Close all active client connections
        for conn in self.active_connections:
            conn.close()

        # Finally, close the main server socket
        self.server.close()
        self.pattern_analysis_thread.join()
        print("Server stopped.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Start the server.")
    parser.add_argument('-l', type=int, default=12345, help='The listen port for the server.')
    parser.add_argument('-p', type=str, required=True, help='The search pattern.')
    args = parser.parse_args()

    server = Server('0.0.0.0', args.l, args.p)

    try:
        server.run_server()
    except KeyboardInterrupt:
        print("\nShutting down the server...")
        # Perform any cleanup actions here
        server.stop_server()  # Assume you have a stop_server method to close connections