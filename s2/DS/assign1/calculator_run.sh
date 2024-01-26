#!/bin/bash

# This script automates the process of compiling, starting, and testing the Calculator RMI service.

# Compile the necessary files.
compile_files() {
    echo "Compiling..."
    make
}

# Start the RMI registry.
start_rmi_registry() {
    echo "Launching RMI registry..."
    rmiregistry &
    RMI_PID=$!
    sleep 2 # Allow some time for the RMI registry to initialize.
}

# Start the CalculatorServer.
start_calculator_server() {
    echo "Launching CalculatorServer..."
    java CalculatorServer &
    SERVER_PID=$!
    sleep 5 # Allow some time for the CalculatorServer to initialize.
}

# Run the CalculatorClient for single client testing.
run_calculator_client_single() {
    echo "Executing CalculatorClient for single client testing..."
    java CalculatorClient < input.txt > output_single.txt
}

# Check if there are any CalculatorClient instances still running
clients_running() {
    pgrep -f CalculatorClient > /dev/null
    return $?
}

# Run the CalculatorClient for multiple client testing.
run_calculator_client_multiple() {
    echo "Executing CalculatorClient for multiple client testing..."
    > output_multiple.txt
    for i in {1..5}
    do
        java CalculatorClient < input.txt >> output_multiple.txt &
        sleep 1 # This helps stagger the client start times a bit. Adjust as needed.
    done

    # Wait for all clients to complete
    while clients_running; do
        echo "Waiting for clients to finish..."
        sleep 10
    done
}

# Run the CalculatorTesting for validation.
run_calculator_testing_single() {
    echo "Executing CalculatorTesting..."
    java CalculatorTesting output_single.txt expected_output_single.txt
}

run_calculator_testing_multiple() {
    echo "Executing CalculatorTesting..."
    java CalculatorTesting output_multiple.txt expected_output_multiple.txt
}

# Terminate any background services (RMI registry and CalculatorServer).
shutdown_services() {
    echo "Terminating services..."
    kill -9 $RMI_PID
    kill -9 $SERVER_PID
    echo "All done!"
}

# Entry point for the script.
main() {
    compile_files
    start_rmi_registry
    start_calculator_server
    run_calculator_client_single
    run_calculator_testing_single
    run_calculator_client_multiple
    run_calculator_testing_multiple
    shutdown_services
}

# Invoke the main function.
main
