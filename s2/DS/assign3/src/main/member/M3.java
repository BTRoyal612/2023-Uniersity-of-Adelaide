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

public class M3 extends Councillor {
    private final Random random;
    private static final int CAMPING_DURATION_MS = 10000; // Duration of being offline (e.g., 5 seconds)
    private boolean isCamping = false;
    private double campingProbabilityThreshold = 0.1; // Default 10% chance of going camping
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public M3(String id, Map<String, AddressPortPair> councillorMap, int totalCouncillors, Communicator communicator) {
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
        checkAndGoCamping();

        if (isCurrentlyCamping()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " goes camping. Message not sent: " + message);
            return;
        }

        // Broadcast this message to all other councillors
        for (String councillorId : getCouncillorMap().keySet()) {
            AddressPortPair address = getCouncillorMap().get(councillorId);

            executorService.submit(() -> {
                int delay = 100 + random.nextInt(900);  // Delaying the message to simulate responsiveness

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Handle interruption
                }

                LocalDateTime dateTime = getCurrentTime();
                System.out.println(
                        "[" + dateTime.format(formatter) + "] " + "Councillor "
                                + getNodeId() + " send " + message + " with " + delay +  "ms delay to " + councillorId);

                communicator.sendRequest(address.getHost(), address.getPort(), message);
            });
        }
    }

    private void checkAndGoCamping() {
        if (random.nextDouble() < campingProbabilityThreshold) {
            goToCamping();
        }
    }

    // Checks if M3 is currently camping
    public boolean isCurrentlyCamping() {
        return this.isCamping;
    }

    // Simulates M3 going camping
    public void goToCamping() {
        if (!isCamping) {
            this.isCamping = true;
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " goes camping.");
            new Thread(() -> {
                try {
                    Thread.sleep(CAMPING_DURATION_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Handle interruption
                }
                this.isCamping = false;
                LocalDateTime dateTime1 = getCurrentTime();
                System.out.println(
                        "[" + dateTime1.format(formatter) + "] " + "Councillor "
                                + getNodeId() + " is back from camping.");
            }).start();
        }
    }

    // Manually set M3 to return from camping, mainly for testing purposes
    public void backFromCamping() {
        this.isCamping = false;
        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " is back from camping (manually triggered).");
    }

    @Override
    public synchronized String handlePrepare(String receivedProposalNumber) {
        introduceVariableDelay();
        return super.handlePrepare(receivedProposalNumber);
    }

    @Override
    public synchronized String handleAcceptRequest(String receivedProposalNumber, String value) {
        introduceVariableDelay();
        return super.handleAcceptRequest(receivedProposalNumber, value);
    }

    private void introduceVariableDelay() {
        if (isCamping) {
            return; // If M3 is camping, they're offline and won't handle the request.
        }

        try {
            int fate = random.nextInt(100);

            if (fate < 5) { // 5% of the time, M3 responds quickly
                int delay = 100 + random.nextInt(400); // Random delay between 100 to 500 milliseconds
                TimeUnit.MILLISECONDS.sleep(delay);
            } else {
                int delay = 1 + random.nextInt(3); // Random delay between 1 to 3 seconds
                TimeUnit.SECONDS.sleep(delay);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
