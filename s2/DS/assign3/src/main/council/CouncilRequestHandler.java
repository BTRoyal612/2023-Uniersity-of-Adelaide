package main.council;

import main.communication.SocketCommunicator;

import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CouncilRequestHandler implements Runnable {
    private Socket clientSocket;
    private Councillor councillor;
    private MajorityListener listener;
    private SocketCommunicator communicator;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CouncilRequestHandler(Socket clientSocket, Councillor councillor, SocketCommunicator communicator) {
        this.clientSocket = clientSocket;
        this.councillor = councillor;
        this.communicator = communicator;
    }

    public void setMajorityListener(MajorityListener listener) {
        this.listener = listener;
    }

    private LocalDateTime getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis),
                ZoneId.systemDefault());
    }

    @Override
    public void run() {
        String request = communicator.receive(clientSocket);

        String response = processReceivedMessage(request);
        if (response != null) {
            communicator.sendResponse(clientSocket, response);
        }
    }

    public String processReceivedMessage(String message) {
        if (message != null) {
            String[] parts = message.split(" ");
            String messageType = parts[0];

            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + this.councillor.getNodeId() + " received: " + message);

            switch (messageType) {
                case "PREPARE":
                    return handlePrepareRequest(parts);
                case "PROMISE":
                    handlePromiseResponse(parts);
                    return null;
                case "ACCEPT_REQUEST":
                    return handleAcceptRequest(parts);
                case "ACCEPTED":
                    handleAcceptedResponse(parts);
                    return null;
                case "DECIDE":
                    handleDecision(parts);
                    return null;
                case "NACK":
                    return null;
                case "REJECTED":
                    return null;
                default:
                    System.out.println("Wrong message format in this Paxos System");
                    return null;
            }
        }
        return null;
    }

    private String handlePrepareRequest(String[] parts) {
        // Process the prepare request and send a response (Promise or Reject)
        return councillor.handlePrepare(parts[1]);
    }

    private void handlePromiseResponse(String[] parts) {
        // Process the promise response from other councillors
        int promiseCount = councillor.getPromiseCounter().incrementAndReturnTotalPromises(parts[1]);
        if (parts.length > 3) {
            councillor.getPromiseCounter().storeAcceptedValue(parts[1], parts[2], parts[3]);
        }
        if (promiseCount > councillor.getTotalCouncillors() / 2) {
            if (listener != null) {
                listener.onPromiseMajorityReached(parts[1]);
            }
        }
    }

    private String handleAcceptRequest(String[] parts) {
        // Process the accept request and send a response (Accepted or Reject)
        return councillor.handleAcceptRequest(parts[1], parts[2]);
    }

    private void handleAcceptedResponse(String[] parts) {
        // Process the accepted response from other councillors
        int acceptCount = councillor.getAcceptedCounter().incrementAndReturnCount(parts[1]);
        if (acceptCount > councillor.getTotalCouncillors() / 2) {
            if (listener != null) {
                listener.onAcceptedMajorityReached(parts[1]);
            }
        }
    }

    private void handleDecision(String[] parts) {
        // Process the decision message and update the state of the councillor
        councillor.handleDecision(parts[1], parts[2]);
    }
}
