# Multi-Threaded Network Server for Pattern Analysis
## Overview
This multi-threaded network server is designed for pattern analysis in text data, specifically focusing on concurrency and socket handling in networked applications. The server handles incoming connections, processes text data, and analyses patterns (such as frequency of specific words) within the data. It utilizes multiple threads to manage connections and data analysis efficiently.

## Getting Started
### Prerequisites
- Python 3.x
- netcat (nc) for testing client connections.
- xterm for opening multiple terminal windows automatically (for client simulation).
- Text files containing books or similar large documents for testing.

### Installation
1. Clone or download this repository to your local machine.
2. Make sure Python 3.x is installed.
3. Obtain text files for testing (e.g., from Gutenberg Project).
### Configuration
- Default listening port: 12345
- Default search pattern: "happy"
- These can be changed by providing arguments when running the script.

### Running the Server
To start the server, use the following command:

```bash
./assignment3 -l [PORT] -p [SEARCH_PATTERN]
```
Replace `[PORT]` with the port number and `[SEARCH_PATTERN]`` with the string you want to search for within the texts. If omitted, the defaults (12345 and "happy") are used.

### Sending Data to the Server
To send text files to the server, use netcat. For instance:

```bash
nc localhost [PORT] -i <delay> < file.txt
```
Where [PORT] is the server's listening port, <delay> is the interval between sending lines from the file, and file.txt is the path to the text file.

## Automated Client Simulation
### Step 1: Ensure Script Permissions
Before running the script, you need to ensure it has the necessary execution permissions. To do this:

1. Open a terminal.

2. Navigate to the directory where run.sh is located.

3. Run the following command to make the script executable:

```bash
chmod +x run.sh
```

### Step 2: Running the Script
Once the script is executable, you can run it directly from the terminal. If no arguments are provided, it will use the default port (12345) and passphrase ("happy"). However, you can specify a different port and passphrase if needed.

To run the script with default settings:

```bash
./run.sh
```

To run the script with a custom port and passphrase:
  
```bash
./run.sh 54321 "custompassphrase"
```

Replace 54321 with your chosen port number and "custompassphrase" with your desired passphrase.

### Additional Notes
- Terminal Requirement: The script opens multiple xterm windows. Ensure that xterm is installed on your system. If it's not, you can typically install it via your distribution's package manager (e.g., sudo apt-get install xterm on Debian/Ubuntu).
- Python Server Script: The script assumes that python3 and the server script assignment3.py are available in the current directory or a system path. Ensure that Python is installed and that assignment3.py is correctly configured.
- Text Files: Ensure that the text files (book1.txt, book3.txt, book8.txt) are present in the books directory relative to where the script is run.
### Troubleshooting
- Permissions Error: If you encounter a permissions error when running ./run.sh, recheck that you've set the execution permission correctly with chmod +x.
- xterm or python3 Not Found: Ensure that both xterm and python3 are installed and accessible from your terminal. You might need to install them or adjust your system's PATH variable.
- Script Not Running: Make sure you're in the correct directory where run.sh is located. You can navigate to the script's directory using the cd command.
- If the script cannot run: If for any reason you cannot execute the `run.sh` script, you can manually open multiple terminal windows. In one terminal, run the server using the instructions provided in the "Running the Server" section. In other terminals, use netcat to send data to the server as described in the "Sending Data to the Server" section.

## Functionality
### Multi-Threaded Network Server
- Listens for incoming connections on the specified port.
- Efficiently manages multiple simultaneous connections.
- Creates a new thread for each connection.
- Non-blocking reads for efficient data processing.

### Shared Data Structure
- Stores incoming data in a shared list with multiple links per node.
- Handles multiple readers and writers.
- Maintains the order of data for each book.

### Analysis Threads
- Periodically analyzes data to find the frequency of the search pattern.
- Outputs book titles sorted by the most frequent occurrences of the search pattern.

### Output
- Each connection's received data is written to a file named book_xx.txt, where xx is the connection number.
- The frequency analysis output is shown on the screen (stdout).

### Testing
Ensure to test the program with:
- A small number of input streams for initial validation.
- At least 10 simultaneous input streams to ensure robustness and scalability.