/*
 * Author: Gia Bao Hoang
 * Date: 02/08/2023
 *
 * This class provides an interactive interface to a remote calculator service
 * via Java's RMI (Remote Method Invocation) mechanism. The client is able to
 * perform basic mathematical operations by interacting with the calculator service.
 * Each client instance has a unique identifier generated using UUID.
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.UUID;

public class CalculatorClient {
    /**
     * Entry point for the CalculatorClient.
     * Provides an interactive interface allowing users to perform operations on the remote calculator.
     *
     * Inputs: User choices and values through the command-line interface.
     * Outputs: Displays results of the selected operations or informative messages based on user input.
     * Special Cases: Handles any exceptions that might arise during the RMI communication.
     */
    public static void main(String[] args) {
        try {
            // Get the registry
            Registry registry = LocateRegistry.getRegistry();

            // Look up the registry for the remote object
            Calculator stub = (Calculator) registry.lookup("Calculator");

            // Generate UUID for the client
            String clientID = UUID.randomUUID().toString();
            System.out.println("Options:");
            System.out.println("1. Push Value");
            System.out.println("2. Push Operation");
            System.out.println("3. Pop Result");
            System.out.println("4. Delayed Pop Result for (millisecond)?");
            System.out.println("5. Is the stack empty?");
            System.out.println("6. Exit");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        int value = scanner.nextInt();
                        stub.pushValue(clientID, value);
                        break;
                    case 2:
                        String operation = scanner.next().toLowerCase();
                        stub.pushOperation(clientID, operation);
                        break;
                    case 3:
                        System.out.println("Result: " + stub.pop(clientID));
                        break;
                    case 4:
                        int delay = scanner.nextInt();
                        System.out.println("Delayed Result: " + stub.delayPop(clientID, delay));
                        break;
                    case 5:
                        System.out.println("Result: " + stub.isEmpty(clientID));
                        break;
                    case 6:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
