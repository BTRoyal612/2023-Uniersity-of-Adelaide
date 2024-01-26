/*
 * Author: Gia Bao Hoang
 * Date: 02/08/2023
 *
 * This file, Calculator.java, defines an interface for a simple remote calculator service.
 * It includes the remote methods for performing basic operations such as:
 *   - Pushing values and operations onto a stack
 *   - Popping values from the stack
 *   - Checking if the stack is empty
 *   - Delaying pop operations
 *   - Printing the current state of the stack
 *
 * These methods are to be implemented in CalculatorImplementation.java.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * Calculator interface
 * Defines remote methods that will be implemented by the server.
 */
public interface Calculator extends Remote {

    // Push a value onto the stack
    void pushValue(String clientId, int val) throws RemoteException;

    // Push an operation onto the stack
    void pushOperation(String clientId, String operator) throws RemoteException;

    // Check if the stack is empty
    boolean isEmpty(String clientId) throws RemoteException;

    // Pop a value from the stack
    int pop(String clientId) throws RemoteException;

    // Delay pop operation by a given amount of time
    int delayPop(String clientId, int millis) throws RemoteException;

    // Print the current stack
    void printStack(String clientId) throws RemoteException;
}
