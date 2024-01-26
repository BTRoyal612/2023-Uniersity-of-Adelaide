package main.communication;

import main.council.Councillor;
import main.council.CouncilRequestHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

public class SocketCommunicator implements Communicator {
    private int port;
    private Councillor councillor;  // A reference to the councillor to set the MajorityListener
    private ServerSocket serverSocket;

    public SocketCommunicator(int port) {
        this.port = port;
    }

    @Override
    public void setCouncillor(Councillor councillor) {
        this.councillor = councillor;
    }

    @Override
    public void startListening() {
        try {
            serverSocket = new ServerSocket(port);
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleConnection(clientSocket)).start();
                } catch (SocketException e) {
                    if (serverSocket.isClosed()) {
                        break; // Exit the loop as the serverSocket has been closed
                    }
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void handleConnection(Socket clientSocket) {
        // Create the handler with the current SocketCommunicator instance
        CouncilRequestHandler handler = new CouncilRequestHandler(clientSocket, councillor, this);
        // Set the MajorityListener
        handler.setMajorityListener(councillor);
        try {
            handler.run(); // Execute the handler's logic for the client socket
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendRequest(String host, int port, String message) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(message);

            String response = in.readLine();
            if (response != null) {
                processReceivedMessage(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String processReceivedMessage(String message) {
        CouncilRequestHandler handler = new CouncilRequestHandler(null, councillor, this);
        // Set the MajorityListener
        handler.setMajorityListener(councillor);
        return handler.processReceivedMessage(message);
    }

    public void sendResponse(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            // Do NOT close out here, or the socket will close too.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String receive(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = in.readLine();
            // Do NOT close in here either.
            return message;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Remove the reference to the councillor
        councillor = null;
    }
}
