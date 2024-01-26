package main.communication;

import main.council.Councillor;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public interface Communicator {
    void sendRequest(String targetName, int port, String message);
    String receive(Socket socket);
    void startListening();
    void close();
    void setCouncillor(Councillor councillor);
}
