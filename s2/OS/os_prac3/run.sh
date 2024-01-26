#!/bin/bash

# Default port and passphrase
PORT=12345
PASSPHRASE="happy"

# Check if a port is provided
if [ ! -z "$1" ]; then
  PORT=$1
fi

# Check if a passphrase is provided
if [ ! -z "$2" ]; then
  PASSPHRASE=$2
fi

# Function to kill all background processes
cleanup() {
  echo "Terminating server and all nc processes..."
  kill 0 # Kills all processes in the current process group
}

# Trap SIGINT (Ctrl+C)
trap cleanup SIGINT

# Start the server in the background
python3 assignment3.py -l "$PORT" -p "$PASSPHRASE" &

# Wait for the server to initialize
sleep 2

# Open nc connections in separate terminals
xterm -e "nc localhost $PORT -i 2 < book1.txt" &
xterm -e "nc localhost $PORT -i 2 < book3.txt" &
xterm -e "nc localhost $PORT -i 2 < book8.txt" &

# Wait for background processes to end
wait