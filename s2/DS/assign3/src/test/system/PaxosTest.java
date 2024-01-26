package test.system;

import main.communication.Communicator;
import main.communication.SocketCommunicator;
import main.council.Councillor;
import main.datastore.AddressPortPair;
import main.member.M1;
import main.member.M2;
import main.member.M3;
import main.member.Neutral;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class PaxosTest {
    @Test
    public void testPaxosConsensusWithThreeNodes() throws IOException {
        System.out.println("\nTest Description: Tests the Paxos consensus in a simplified environment with only 3 nodes. "
                + "Node M1 proposes 'M1', and node M3 proposes 'M3'. Observes the consensus when M1 reaches the accept phase "
                + "before M3 sends out its prepare request.\n"
                + "Expected Outcome: Node M1 is more likely to be chosen as the leader.\n");

        // Setup the mapping of node IDs to their host and port info
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("M1", new AddressPortPair("localhost", 9001));
        councillorMap.put("M2", new AddressPortPair("localhost", 9002));
        councillorMap.put("M3", new AddressPortPair("localhost", 9003));

        Communicator com1 = new SocketCommunicator(9001);
        Communicator com2 = new SocketCommunicator(9002);
        Communicator com3 = new SocketCommunicator(9003);

        // Setting up the councillors
        Councillor M1 = new Councillor("M1", councillorMap, 3, com1);
        Councillor M2 = new Councillor("M2", councillorMap, 3, com2);
        Councillor M3 = new Councillor("M3", councillorMap, 3, com3);

        CountDownLatch latch = new CountDownLatch(1);

        // Set the listener to M1
        M1.setOnPromiseMajorityCompletion(() -> {
            latch.countDown();
        });

        // M1 proposes value 1
        Thread t1 = new Thread(() -> M1.startProposal("M1"));
        t1.start();

        // After some time, M3 proposes value 3
        Thread t2 = new Thread(() -> {
            try {
                latch.await();  // Introducing delay for concurrency
                M3.startProposal("M3");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t2.start();

        // Wait for threads to finish
        try {
            Thread.sleep(10000);  // Sleep for 10,000 milliseconds = 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
            // Handle the interruption, if necessary
        }

        // Check for consensus
        String decisionM1 = M1.getDecision();
        String decisionM2 = M2.getDecision();
        String decisionM3 = M3.getDecision();

        assertNotNull(decisionM1);
        assertNotNull(decisionM2);
        assertNotNull(decisionM3);

        assertEquals(decisionM1, decisionM2);
        assertEquals(decisionM2, decisionM3);

        System.out.println("The end of three nodes instant response test.\n\n");
    }

    @Test
    public void testPaxosConsensusWithNineNodes() throws IOException {
        System.out.println("\nTest Description: Expands the network to 9 nodes, all responding instantly. "
                + "Nodes M1 and M3 simultaneously propose 'M1' and 'M3'. The test aims to observe the behavior of a larger "
                + "consensus group under simultaneous proposals.\n"
                + "Expected Outcome: Node M3 is more likely to win the presidency due to higher proposal number.\n");

        // Setup councillor mapping
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            councillorMap.put("M" + i, new AddressPortPair("localhost", 9000 + i));
        }

        // Setting up councillors and communicators
        List<Councillor> councillors = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            Communicator com = new SocketCommunicator(9000 + i);
            Councillor councillor = new Councillor("M" + i, councillorMap, 9, com);
            councillors.add(councillor);
        }

        CountDownLatch latch = new CountDownLatch(2);

        // M1 and M3 propose almost simultaneously
        Thread t1 = new Thread(() -> {
            councillors.get(0).startProposal("M1");
            latch.countDown();
        });
        Thread t3 = new Thread(() -> {
            councillors.get(2).startProposal("M3");
            latch.countDown();
        });

        // M2 waits and then proposes
        Thread t2 = new Thread(() -> {
            try {
                latch.await();  // Wait for M1 and M3 to start their proposals
                Thread.sleep(500); // Additional delay to let M1 and M3's proposals process a bit
                councillors.get(1).startProposal("M2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t3.start();
        t2.start();

        // Wait for threads to finish
        try {
            Thread.sleep(20000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        // Checking for consensus
        String consensusValue = councillors.get(0).getDecision();
        assertNotNull(consensusValue);
        for (Councillor councillor : councillors) {
            assertEquals(consensusValue, councillor.getDecision());
        }

        for (Councillor councillor : councillors) {
            if (councillor.getCommunicator() != null) {
                councillor.terminateVotingSystem();
            }
        }

        // Wait for threads to finish
        try {
            Thread.sleep(2000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        System.out.println("The end of nine nodes instant response test.\n\n");
    }

    private List<Councillor> initializeCouncillors() throws IOException {
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        for (int i = 1; i <= 9; i++) {
            councillorMap.put("M" + i, new AddressPortPair("localhost", 9000 + i));
        }

        List<Councillor> councillors = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            Communicator com = new SocketCommunicator(9000 + i);
            Councillor councillor;
            switch (i) {
                case 1:
                    councillor = new M1("M1", councillorMap, 9, com);
                    break;
                case 2:
                    councillor = new M2("M2", councillorMap, 9, com);
                    break;
                case 3:
                    councillor = new M3("M3", councillorMap, 9, com);
                    break;
                default:
                    councillor = new Neutral("M" + i, councillorMap, 9, com);
            }
            councillors.add(councillor);
        }
        return councillors;
    }

    @Test
    public void testM2ProposesThenGoesOffline() throws IOException {
        System.out.println("\nTest Description: Simulates a scenario where node M2 proposes and then goes offline. "
                + "Nodes M1 and M3 then propose their respective values. Checks system resilience and correct behavior in "
                + "face of node failures.\n"
                + "Expected Outcome: Unpredictable outcome, but M3 has a higher likelihood of winning.\n");

        List<Councillor> councillors = initializeCouncillors();

        // M2 proposes and then goes offline
        councillors.get(1).startProposal("M2");

        // Wait for threads to finish
        try {
            Thread.sleep(5000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        ((M2) councillors.get(1)).setOffline(true);

        councillors.get(0).startProposal("M1");
        councillors.get(2).startProposal("M3");

        // Wait for the decision
        try {
            Thread.sleep(60000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        String consensusValue = null;

        for (Councillor councillor : councillors) {
            String decision = councillor.getDecision();
            if (decision == null || decision.equals("Not yet decided")) {
                continue;  // No decision made yet, skip to the next councillor.
            }

            if (consensusValue == null) {
                consensusValue = decision;  // Set the first found decision as the consensus value.
            } else {
                assertEquals(consensusValue, decision, "There should only be one unique decision among all councillors");
            }
        }

        // It's valid for the consensusValue to be null if no councillor has reached a decision.
        // If consensusValue is not null, it implies that at least one councillor has reached a decision.
        if (consensusValue != null) {
            System.out.println("Consensus reached on value: " + consensusValue);
        } else {
            System.out.println("No consensus reached yet among councillors.");
        }

        for (Councillor councillor : councillors) {
            if (councillor.getCommunicator() != null) {
                councillor.terminateVotingSystem();
            }
        }

        // Wait for threads to finish
        try {
            Thread.sleep(2000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        System.out.println("The end of M2 proposes then goes offline test.\n\n");
    }

    @Test
    public void testM3ProposesThenGoesCamping() throws IOException {
        System.out.println("\nTest Description: Node M3 proposes and goes 'camping'. During M3's absence, nodes M1 and M2 "
                + "propose their own values. Observes system behavior when M3 returns.\n"
                + "Expected Outcome: Likelihood of M1 or M3 winning increases, depending on how quickly M3 can repropose.\n");

        // Initialize councillors with M2 at the cafe...
        List<Councillor> councillors = initializeCouncillors();

        // M3 proposes and then goes camping
        councillors.get(2).startProposal("M3");
        // Simulate M3 going camping, e.g., by disconnecting or disabling response
        ((M3) councillors.get(2)).goToCamping();

        // M1 and M2 propose
        councillors.get(0).startProposal("M1");
        councillors.get(1).startProposal("M2");

        // Wait for the decision
        try {
            Thread.sleep(20000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        councillors.get(0).startProposal("M1");
        councillors.get(1).startProposal("M2");

        // Wait for the decision
        try {
            Thread.sleep(40000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        String consensusValue = null;

        for (Councillor councillor : councillors) {
            String decision = councillor.getDecision();
            if (decision == null || decision.equals("Not yet decided")) {
                continue;  // No decision made yet, skip to the next councillor.
            }

            if (consensusValue == null) {
                consensusValue = decision;  // Set the first found decision as the consensus value.
            } else {
                assertEquals(consensusValue, decision, "There should only be one unique decision among all councillors");
            }
        }

        // It's valid for the consensusValue to be null if no councillor has reached a decision.
        // If consensusValue is not null, it implies that at least one councillor has reached a decision.
        if (consensusValue != null) {
            System.out.println("Consensus reached on value: " + consensusValue);
        } else {
            System.out.println("No consensus reached yet among councillors.");
        }

        for (Councillor councillor : councillors) {
            if (councillor.getCommunicator() != null) {
                councillor.terminateVotingSystem();
            }
        }

        // Wait for threads to finish
        try {
            Thread.sleep(2000);  // Sleep for 20,000 milliseconds = 20 seconds to let the process complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Reset the interrupt status
        }

        System.out.println("The end of M3 proposes then goes camping test.\n\n");
    }
}
