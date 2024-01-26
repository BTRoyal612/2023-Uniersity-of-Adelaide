/*
 * Author: Gia Bao Hoang
 * Date: 02/08/2023
 *
 * This file, CalculatorServer.java, is the entry point for the remote calculator service.
 * It initializes an instance of the CalculatorImplementation class and binds it to the RMI registry
 * under the name "Calculator". This makes the Calculator service available to remote clients,
 * allowing them to perform calculations by invoking the remote methods defined in the Calculator
 * interface.
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CalculatorServer {
    /**
     * Main method to create and bind the Calculator remote object, making it ready to accept client requests.
     * Input: args (Type: String[]), command-line arguments.
     * Output: None, but initializes the RMI registry and binds the Calculator object.
     */
    public static void main(String[] args) {
        try {
            // Create an object of the interface implementation class
            Calculator obj = new CalculatorImplementation();

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Calculator", obj);

            System.out.println("Calculator Server ready.");

        } catch (Exception e) {
            System.out.println("Calculator Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
