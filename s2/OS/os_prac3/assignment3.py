#!/usr/bin/env python3

import argparse
import socket
import threading
import time
import uuid

class Node:
    def __init__(self, data, book_title, book_id):
        self.data = data  # Contains line from the book
        self.next = None  # Points to next node in list
        self.book_next = None  # Points to next line from the same book
        # Next node that contains the search term; For Step 3
        self.next_frequent_search = None
        self.book_title = book_title
        self.book_id = book_id


class SharedLinkedList:
    def __init__(self):
        self.head = None  # Start of the list
        self.tail = None
        self.shared_list_lock = threading.Lock()

    # Method to add data to the list
    def append(self, data, book_head, book_title, book_id):
        new_node = Node(data=data, book_title=book_title, book_id=book_id) 

        if not self.head:
            self.head = new_node
        else:
            self.tail.next = new_node

        # Set book_next pointer
        if book_head:
            # traverse from the book_head to find the last node of this book
            temp = book_head
            while temp.book_next:
                temp = temp.book_next
            temp.book_next = new_node

        self.tail = new_node  # Update the tail pointer

        # Print addition of the node
        print(f"Added node: {data}")

        return new_node

class PatternAnalysisThread(threading.Thread):
    output_event = threading.Event()  # Shared event among all threads
    output_lock = threading.Lock()  # Shared lock among all threads

    def __init__(self, shared_linked_list, pattern, interval=5, thread_id=0):
        super().__init__()
        self.shared_linked_list = shared_linked_list
        self.pattern = pattern
        self.interval = interval
        self.thread_id = thread_id  # Thread identifier
        self.stop_event = threading.Event()
        self.last_processed_node = None
        self.book_counter = {}
        self.book_map = {}

    def run(self):
        last_output_time = time.time()  # initial timestamp
        print(f"Thread-{self.thread_id} Analysis start")

        while not self.stop_event.is_set():
            # Try to acquire the lock without blocking
            acquired = self.shared_linked_list.shared_list_lock.acquire(blocking=False)
            if acquired:
                # If the lock was successfully acquired, process the patterns
                try:
                    self.count_pattern_occurrences()
                finally:
                    # Always release the lock when done
                    self.shared_linked_list.shared_list_lock.release()

            # Check if enough time has passed since the last output
            current_time = time.time()
            if current_time - last_output_time >= self.interval:
                self.try_output_results()
                last_output_time = current_time

    def count_pattern_occurrences(self):
        # Start from next_frequent_search if it exists, else from last_processed_node or head
        if self.last_processed_node and self.last_processed_node.next_frequent_search:
            current_node = self.last_processed_node.next_frequent_search
        elif self.last_processed_node:
            current_node = self.last_processed_node.next
        else:
            current_node = self.shared_linked_list.head

        previous_node_with_pattern = None

        while current_node:
            book_id = current_node.book_id
            words = current_node.data.split()
            count = words.count(self.pattern)

            if book_id not in self.book_map:
                self.book_map[book_id] = current_node.book_title

            if count:
                # If a previous node with pattern is found, update its next_frequent_search
                if previous_node_with_pattern and not previous_node_with_pattern.next_frequent_search:
                    previous_node_with_pattern.next_frequent_search = current_node
                previous_node_with_pattern = current_node

                if book_id in self.book_counter:
                    self.book_counter[book_id] += count
                else:
                    self.book_counter[book_id] = count

            self.last_processed_node = current_node  # update the last processed node
            if current_node.next_frequent_search:  # Traverse using next_frequent_search if available
                current_node = current_node.next_frequent_search
            else:
                current_node = current_node.next

    def try_output_results(self):
        with PatternAnalysisThread.output_lock:
            if not PatternAnalysisThread.output_event.is_set():
                sorted_books = sorted(
                    self.book_counter.items(), key=lambda x: x[1], reverse=True)
                # print current real time using local time
                print(
                    f"Current time: {time.strftime('%H:%M:%S', time.localtime())}")
                if (len(sorted_books) > 0):
                    print("Thread", self.thread_id, "Print Patterns:")
                    for i in range(len(sorted_books)):
                        print(i+1, '-', self.book_map[sorted_books[i][0]], '- Count:', sorted_books[i][1])
                    print("\n")
                PatternAnalysisThread.output_event.set()
                threading.Timer(
                    1, PatternAnalysisThread.output_event.clear).start()

class Server:
    def __init__(self, host, port, pattern):
        self.host = host
        self.port = port
        self.pattern = pattern
        self.shared_list = SharedLinkedList()
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.server.bind((self.host, self.port))
        self.print_lock = threading.Lock()
        self.pattern_analysis_threads = self.initialize_pattern_analysis_threads()
        self.connection_count = 0
        self.active_connections = []
        self.stop_event = threading.Event()
        self.connection_lock = threading.Lock()

    def initialize_pattern_analysis_threads(self, num_threads=2):
        threads = []
        for i in range(num_threads):
            pattern_thread = PatternAnalysisThread(
                self.shared_list, self.pattern, thread_id=i)
            threads.append(pattern_thread)
        return threads

    # Client handling function
    def handle_client(self, client_socket):
        book_id = uuid.uuid4()

        # Receive the title first
        first_data = client_socket.recv(1024).decode('utf-8')
        # change book_title until it meets the first '\n'
        book_title = first_data[:first_data.find('\n')]

        with self.shared_list.shared_list_lock:
            # Append the book_head to the shared list
            try:
                book_head = self.shared_list.append(data=first_data, book_title=book_title, book_id=book_id, book_head=None)
            except UnicodeDecodeError:
                pass

        # Acknowledge the title receipt
        client_socket.sendall(b'READ\n')

        while True:
            data = client_socket.recv(1024)

            # Append received data to the shared list using a
            with self.shared_list.shared_list_lock:
                try:
                    self.shared_list.append(data=data.decode(
                        'utf-8'),book_head=book_head, book_title=book_title, book_id=book_id)
                except UnicodeDecodeError:
                    continue

            # Acknowledge the receipt of each line
            client_socket.sendall(b'READ\n')
            if len(data) < 1024:
                break

        client_socket.close()

        # Write received book to file
        self.write_received_book(book_head)

    def write_received_book(self, book_head):
        with self.connection_lock:
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
        for thread in self.pattern_analysis_threads:
            thread.start()

        while not self.stop_event.is_set():  # Use the event to check if we should stop
            try:
                client_socket, addr = self.server.accept()
                self.active_connections.append(client_socket)
                client_handler = threading.Thread(
                    target=self.handle_client, args=(client_socket,))
                client_handler.start()

            except socket.timeout:
                continue  # If the accept call times out, check the stop event and possibly loop again

    def stop_server(self):
        self.stop_event.set()
        for conn in self.active_connections:
            conn.close()

        for thread in self.pattern_analysis_threads:
            thread.stop_event.set()
            thread.join()
            
        self.server.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Start the server.")
    parser.add_argument('-l', type=int, default=12345,
                        help='The listen port for the server.')
    parser.add_argument('-p', type=str, required=True,
                        help='The search pattern.')
    args = parser.parse_args()

    server = Server('0.0.0.0', args.l, args.p)

    try:
        server.run_server()
    except KeyboardInterrupt:
        print("\nShutting down the server...")
        # Perform any cleanup actions here
        server.stop_server()  # Assume you have a stop_server method to close connections
