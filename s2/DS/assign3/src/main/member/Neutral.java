package main.member;

import main.communication.Communicator;
import main.council.Councillor;
import main.datastore.AddressPortPair;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Neutral extends Councillor {
    private final Random random;

    public Neutral(String id, Map<String, AddressPortPair> councillorMap, int totalCouncillors, Communicator communicator) throws IOException {
        super(id, councillorMap, totalCouncillors, communicator);
        this.random = new Random();
    }

    @Override
    public synchronized String handlePrepare(String receivedProposalNumber) {
        // Introduce a random delay before handling the prepare request
        introduceVariableDelay();
        return super.handlePrepare(receivedProposalNumber);
    }

    @Override
    public synchronized String handleAcceptRequest(String receivedProposalNumber, String value) {
        // Introduce a random delay before handling the accept request
        introduceVariableDelay();
        return super.handleAcceptRequest(receivedProposalNumber, value);
    }

    private void introduceVariableDelay() {
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
