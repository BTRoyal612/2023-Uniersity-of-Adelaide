# Remote Calculator Service

## Description of Files

This project offers a remote calculator service using Java RMI. Below is a brief description of each file included:

- **Calculator.java**:
   - Describes the Calculator interface for the remote calculator service.

- **CalculatorImplementation.java**:
   - Contains the actual implementation for the Calculator interface, providing methods for various mathematical calculations.

- **CalculatorServer.java**:
   - Responsible for setting up the RMI registry and binds the Calculator object to cater to client requests.

- **CalculatorClient.java**:
   - Contains client-side logic, built to interact with the remote calculator, and performs various operation tests. This client is interactive, prompting user for inputs.

- **CalculatorTesting.java**:
   - A tool to compare the content of output files produced by the CalculatorClient with the expected outputs.

- **calculator_run.sh**:
   - A utility bash script to automate the tasks of compiling, initiating, and testing the calculator RMI service.

- **input.txt**:
   - Contains predefined test case inputs for the CalculatorClient.

- **expected_output_single.txt**:
   - The expected output for running a single client instance.

- **expected_output_multiple.txt**:
   - The expected output for running multiple client instances simultaneously.

- **Makefile**:
   - Holds the compilation rules for the project.

## Testing

The testing process ensures the accuracy and robustness of the calculator service:

1. **Single Client Testing**:
   - Checks accuracy of all functions with a single CalculatorClient instance.
   - Uses `input.txt` for predefined test cases, storing results in `output_single.txt`.
   - Compares the output with `expected_output_single.txt` to check accuracy.

2. **Multiple Client Testing**:
   - Verifies functionality with multiple clients (5 in our tests) running at the same time.
   - Uses `input.txt`, collating results into `output_multiple.txt`.
   - Compares this with `expected_output_multiple.txt` for correctness.

For both tests, `CalculatorTesting` compares two files. If the output states:
- "The files are identical", all tests have passed.
- "The files are different", at least one test failed.

## How to Run

To compile, execute, and test:

1. Assign execution permissions to the script:
    ```
    chmod +x calculator_run.sh
    ```
2. Execute the script:
    ```
    ./calculator_run.sh
    ```

Ensure all required files, like `expected_output_single.txt` and `expected_output_multiple.txt`, are in the correct directories. Always follow the detailed order above.
