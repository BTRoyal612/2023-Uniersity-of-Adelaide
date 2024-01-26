package test.member;

import main.communication.SocketCommunicator;
import main.member.M2;
import main.member.M3;
import main.member.Neutral;

import main.communication.Communicator;
import main.datastore.AddressPortPair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

public class MemberTest {
    @Test
    public void testM2IsAtCafeProbability() throws IOException {
        int atCafeCount = 0;
        int sampleSize = 10000;

        // Even though we don't use this map for the test, it's initialized to adhere to the object construction requirements.
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("M2_test", new AddressPortPair("localhost", 9999));

        M2 m2 = new M2("M2_test", councillorMap, 1, new SocketCommunicator(8081));

        for (int i = 0; i < sampleSize; i++) {
            if (m2.isActuallyAtCafe()) {
                atCafeCount++;
            }
        }

        double probability = (double) atCafeCount / sampleSize;
        assertTrue(probability <= 0.06, "Probability should be close to 5%");
    }

    @Test
    public void testM2OfflineState() throws IOException {
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("M2_test", new AddressPortPair("localhost", 9999));
        M2 m2 = new M2("M2_test", councillorMap, 1, new SocketCommunicator(8081));

        // Test when M2 is set offline
        m2.setOffline(true);
        assertTrue(m2.isActuallyOffline(), "M2 should be offline");

        // Test when M2 is set online
        m2.setOffline(false);
        assertFalse(m2.isActuallyOffline(), "M2 should be online");
    }

    @Test
    public void testM2HandlePrepareDelay() throws IOException {
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("M2_test", new AddressPortPair("localhost", 9999));
        M2 m2 = new M2("M2_test", councillorMap, 1, new SocketCommunicator(8081));

        long start = System.currentTimeMillis();
        m2.handlePrepare("TestProposal");
        long duration = System.currentTimeMillis() - start;

        // Assuming worst-case scenario where max delay is 19 seconds
        assertTrue(duration <= (19 * 1000), "Delay should be less than or equal to 19 seconds");
    }

    @Test
    void testM3CampingBehaviour() throws IOException, InterruptedException {
        // Assuming a setup for M3, communicator, councillorMap etc.
        // Mock Communicator
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("M3_test", new AddressPortPair("localhost", 9999));
        Communicator mockCommunicator = Mockito.mock(Communicator.class);

        M3 m3 = new M3("M3_test", councillorMap, 1, mockCommunicator);

        // Trigger the method that would lead to camping
        m3.backFromCamping();

        // Wait for longer than the camping duration
        Thread.sleep(11000);  // Camping duration is 10000ms, so wait for 11000ms to be safe

        // Assert that no messages are sent while camping
        Mockito.verify(mockCommunicator, Mockito.never()).sendRequest(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testNeutralHandlePrepare() throws IOException {
        // Mocking dependencies
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("M4", new AddressPortPair("localhost", 9999));
        Communicator mockCommunicator = new SocketCommunicator(8080); // Or a mock

        Neutral councillor = new Neutral("M4", councillorMap, 1, mockCommunicator);

        long start = System.currentTimeMillis();
        councillor.handlePrepare("TestProposal");
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration >= 1000 && duration <= 5000, "Delay should be between 1 to 5 seconds");
    }
}

