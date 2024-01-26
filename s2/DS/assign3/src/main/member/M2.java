package main.member;

import main.communication.Communicator;
import main.council.Councillor;
import main.datastore.AddressPortPair;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class M2 extends Councillor {
    private final Random random;
    private boolean isAtCafe = false;
    private boolean isOffline = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public M2(String id, Map<String, AddressPortPair> councillorMap, int totalCouncillors, Communicator communicator) {
        super(id, councillorMap, totalCouncillors, communicator);
        this.random = new Random();
    }

    private LocalDateTime getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis),
                ZoneId.systemDefault());
    }

    @Override
    public void broadcast(String message) {
        // Determine the current status of M2
        isAtCafe = random.nextInt(100) < 20; // 5% chance M2 is at Sheoak Café
        if (isOffline) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " is Offline. Message not send: " + message);
            return;
        }

        LocalDateTime dateTime = getCurrentTime();
        if (isAtCafe) {
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " is at the cafe.");
        } else {
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has poor connection.");
        }


        for (String councillorId : getCouncillorMap().keySet()) {
            AddressPortPair address = getCouncillorMap().get(councillorId);
            executorService.submit(() -> {
                if (isAtCafe) {
                    communicator.sendRequest(address.getHost(), address.getPort(), message);
                } else {
                    simulatePoorConnection(councillorId, message, address);
                }
            });
        }
    }

    private void simulatePoorConnection(String councillorId, String message, AddressPortPair address) {
        try {
            if (random.nextInt(100) < 30) {
                LocalDateTime dateTime = getCurrentTime();
                System.out.println(
                        "[" + dateTime.format(formatter) + "] " + "Councillor "
                                + getNodeId() + " failed to send " + message + " to " + councillorId);
                return;
            }

            int delay = 5000 + random.nextInt(5000); // Delay between 5 to 10 seconds
            Thread.sleep(delay);

            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " send " + message + " with " + delay +  "s delay to " + councillorId);
            communicator.sendRequest(address.getHost(), address.getPort(), message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isActuallyAtCafe() {
        return this.isAtCafe;
    }

    public boolean isActuallyOffline() {
        return this.isOffline;
    }

    public void setOffline(boolean offline) {
        this.isOffline = offline;
    }

    @Override
    public synchronized String handlePrepare(String receivedProposalNumber) {
        if (!introduceVariableDelay()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " dropped request: " + receivedProposalNumber);
            return null;
        }
        return super.handlePrepare(receivedProposalNumber);
    }

    @Override
    public synchronized String handleAcceptRequest(String receivedProposalNumber, String value) {
        if (!introduceVariableDelay()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " dropped request: " + receivedProposalNumber + " " + value);
            return null;
        }
        return super.handleAcceptRequest(receivedProposalNumber, value);
    }

    private boolean introduceVariableDelay() {
        if (isOffline) {
            return false;
        }

        try {
            int fate = random.nextInt(100);
            if (fate < 20) { // At Sheoak Café
                return true;
            } else if (fate < 75) { // 70% of the time, long delay due to poor connectivity
                int delay = 5 + random.nextInt(5); // Random delay between 5 to 10 seconds
                TimeUnit.SECONDS.sleep(delay);
                return true;
            }
            return false; // 25% chance to drop the request
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
