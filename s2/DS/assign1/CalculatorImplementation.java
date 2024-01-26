/*
 * Author: Gia Bao Hoang
 * Date: 02/08/2023
 *
 * This file, CalculatorImplementation.java, provides an implementation for the Calculator interface
 * defined in Calculator.java. This class extends UnicastRemoteObject and implements the remote methods
 * defined in the Calculator interface. It also defines helper methods for gcd (greatest common divisor)
 * and lcm (least common multiple) calculations, and maintains a map of client IDs to their respective
 * stacks. It utilizes synchronization to handle concurrent print requests from multiple threads.
 */

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CalculatorImplementation extends UnicastRemoteObject implements Calculator {
    // Create a static object to use as a lock
    private static final Object printLock = new Object();

    // Create a hashmap to store the stack of each client using clientID
    private final Map<String, Stack<Integer>> stackMap = new HashMap<>();
    protected CalculatorImplementation() throws RemoteException {
        super();
    }

    /**
     * Returns the greatest common divisor of two integers.
     * Input: Two integers a and b.
     * Output: An integer representing the greatest common divisor.
     * Special Cases: None.
     */
    private int gcd (int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    /**
     * Returns the least common multiple of two integers.
     * Input: Two integers a and b.
     * Output: An integer representing the least common multiple.
     * Special Cases: None.
     */
    private int lcm(int a, int b) {
        return a * b / gcd(a, b);
    }

    /**
     * Retrieves or creates a stack for the client identified by clientID.
     * Input: clientID representing a unique client.
     * Output: The client's stack.
     * Special Cases: If no stack exists, a new one is created.
     */
    private Stack<Integer> getStack(String clientID) {
        Stack<Integer> stack = stackMap.get(clientID);
        if (stack == null) {
            stack = new Stack<>();
            stackMap.put(clientID, stack);
        }
        return stack;
    }

    /**
     * Pushes a value to a client's stack.
     * Input: clientID and an integer value.
     * Output: None.
     * Special Cases: None.
     */
    @Override
    public void pushValue(String clientID, int val) throws RemoteException {
        // implementation
        getStack(clientID).push(val);
    }

    /**
     * Performs a specified operation on a client's stack.
     * Input: clientID and an operator ("min", "max", "lcm", "gcd").
     * Output: The result of the operation is pushed to the client's stack.
     * Special Cases: Depends on the operation specified.
     */
    @Override
    public void pushOperation(String clientID, String operator) throws RemoteException {
        switch (operator) {
            // Get the minimum value of the stack
            case "min":
                int min = getStack(clientID).pop();
                while (!getStack(clientID).isEmpty()) {
                    int val = getStack(clientID).pop();
                    if (val < min) {
                        min = val;
                    }
                }
                getStack(clientID).push(min);
                break;

            // Get the maximum value of the stack
            case "max":
                int max = getStack(clientID).pop();
                while (!getStack(clientID).isEmpty()) {
                    int val = getStack(clientID).pop();
                    if (val > max) {
                        max = val;
                    }
                }
                getStack(clientID).push(max);
                break;

            // Get the least common multiple of the stack
            case "lcm":
                int lcm = getStack(clientID).pop();
                while (!getStack(clientID).isEmpty()) {
                    int val = getStack(clientID).pop();
                    lcm = lcm(val, lcm);
                }
                getStack(clientID).push(lcm);
                break;

            // Get the greatest common divisor of the stack
            case "gcd":
                int gcd = getStack(clientID).pop();
                while (!getStack(clientID).isEmpty()) {
                    int val = getStack(clientID).pop();
                    gcd = gcd(val, gcd);
                }
                getStack(clientID).push(gcd);
                break;
            default:
                break;
        }
    }

    /**
     * Checks if a client's stack is empty.
     * Input: clientID representing a unique client.
     * Output: A boolean value indicating whether the stack is empty.
     * Special Cases: None.
     */
    @Override
    public boolean isEmpty(String clientID) throws RemoteException {
        return getStack(clientID).isEmpty();
    }

    /**
     * Pops a value from a client's stack.
     * Input: clientID representing a unique client.
     * Output: The integer value popped from the client's stack.
     * Special Cases: None.
     */
    @Override
    public int pop(String clientID) throws RemoteException {
        return getStack(clientID).pop();
    }

    /**
     * Delays popping a value from a client's stack by a specified number of milliseconds.
     * Input: clientID representing a unique client and the delay in milliseconds.
     * Output: The integer value popped from the client's stack.
     * Special Cases: InterruptedException if the sleep is interrupted.
     */
    @Override
    public int delayPop(String clientID, int millis) throws RemoteException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getStack(clientID).pop();
    }

    /**
     * Prints the client ID and a specified message to the console.
     * Input: clientID representing a unique client, message to be printed alongside the client ID.
     * Output: None, but the clientID and message are printed to the console.
     * Special Cases: Concurrent requests are synchronized to avoid interleaving print statements.
     */
    public void print(String clientID, String message) {
        synchronized (printLock) {
            System.out.println(clientID);
            System.out.println(message);
        }
    }

    /**
     * Prints a client's stack.
     * Input: clientID representing a unique client.
     * Output: None, but the client's stack is printed to the console.
     * Special Cases: None.
     */
    @Override
    public void printStack(String clientID) throws RemoteException {
        String message = "Stack: " + getStack(clientID).toString() + "\n";
        print(clientID, message);
    }
}
