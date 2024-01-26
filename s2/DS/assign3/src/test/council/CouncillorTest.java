package test.council;

import main.communication.Communicator;
import main.council.Councillor;
import main.datastore.AddressPortPair;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CouncillorTest {

    private Councillor councillor;
    private Communicator mockCommunicator;

    @BeforeEach
    public void setUp() {
        mockCommunicator = Mockito.mock(Communicator.class);

        // Mock the AddressPortPair and councilMap for the Councillor constructor
        AddressPortPair mockAddress = new AddressPortPair("localhost", 9001);
        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("testNode", mockAddress);

        councillor = new Councillor("testNode", councillorMap, 3, mockCommunicator);

        // Ensure that you have a mechanism in Councillor class to inject/mock the communicator.
    }

    @AfterEach
    public void tearDown() {
        if (councillor.getCommunicator() != null) {
            councillor.getCommunicator().close();
        }
        councillor = null;
        mockCommunicator = null;
    }

    @Test
    public void testValidConstructor() {
        AddressPortPair address = councillor.getCouncillorMap().get("testNode");
        assertNotNull(address);
        assertEquals("localhost", address.getHost());
        assertEquals(9001, address.getPort());
        assertEquals("testNode", councillor.getNodeId());
        assertEquals(0, councillor.getCurrentSequenceNumber());
    }


    // If you have implemented a mechanism to get the current decision.
    @Test
    public void testDecisionInitialState() {
        assertEquals("Not yet decided", councillor.getDecision());
    }

    @Test
    public void testBroadcastWithMultipleNodes() {
        // Mock a scenario where we have multiple councillors
        AddressPortPair address1 = new AddressPortPair("localhost", 9001);
        AddressPortPair address2 = new AddressPortPair("localhost", 9002);
        AddressPortPair address3 = new AddressPortPair("localhost", 9003);

        Map<String, AddressPortPair> councillorMap = new HashMap<>();
        councillorMap.put("node1", address1);
        councillorMap.put("node2", address2);
        councillorMap.put("node3", address3);

        Communicator mockCom1 = Mockito.mock(Communicator.class);

        councillor = new Councillor("node1", councillorMap, 3, mockCom1);

        councillor.broadcast("testMessage");

        // Add a delay to wait for asynchronous processing.
        try {
            Thread.sleep(1000);  // Sleep for 1 second (1000 milliseconds)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restore interrupted status
            throw new RuntimeException(e);
        }

        // Verifying that the mockCommunicator's sendRequest method is called for each node
        Mockito.verify(mockCom1, Mockito.times(1)).sendRequest(Mockito.eq(address1.getHost()), Mockito.eq(address1.getPort()), Mockito.eq("testMessage"));
        Mockito.verify(mockCom1, Mockito.times(1)).sendRequest(Mockito.eq(address2.getHost()), Mockito.eq(address2.getPort()), Mockito.eq("testMessage"));
        Mockito.verify(mockCom1, Mockito.times(1)).sendRequest(Mockito.eq(address3.getHost()), Mockito.eq(address3.getPort()), Mockito.eq("testMessage"));
    }

    @Test
    public void testHandlePrepareWithHigherProposalNumber() {
        // Assuming that if a Councillor receives a PREPARE with a higher proposal number,
        // it will promise not to accept any proposal less than the received one.
        String currentProposal = councillor.handlePrepare("M1:1");
        String higherProposal = councillor.handlePrepare("M1:5");

        assertTrue(higherProposal.startsWith("PROMISE"));
        assertNotEquals(currentProposal, higherProposal);
    }

    @Test
    public void testHandlePrepareWithLowerProposalNumber() {
        // Assuming that if a Councillor receives a PREPARE with a lower proposal number
        // after it has already received a higher one, it won't make a promise.
        councillor.handlePrepare("M1:5");
        String lowerProposal = councillor.handlePrepare("M1:1");

        // The exact behavior may vary depending on your implementation, but it's likely
        // it might reject the lower proposal or simply not promise.
        assertFalse(lowerProposal.startsWith("PROMISE"));
    }

    @Test
    public void testHandleAcceptRequestWithHigherProposalNumber() {
        councillor.handlePrepare("M1:5");
        String acceptedResponse = councillor.handleAcceptRequest("M1:10", "newValue");

        assertTrue(acceptedResponse.startsWith("ACCEPTED"));
        assertEquals("newValue", councillor.getAcceptedValue());
    }

    @Test
    public void testHandleAcceptRequestWithLowerProposalNumber() {
        councillor.handlePrepare("M2:10");
        String rejectedResponse = councillor.handleAcceptRequest("M1:5", "oldValue");

        assertFalse(rejectedResponse.startsWith("ACCEPTED"));
        assertNotEquals("oldValue", councillor.getDecision());
    }

    @Test
    public void testCannotProposeTwiceInARow() {
        councillor.startProposal("value1");

        // Capturing the output stream to validate the print message
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        councillor.startProposal("value2");

        // Resetting the System.out back to its original state
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        String printedOutput = outContent.toString().trim();

        // Regex to match the timestamp and the desired message
        String regex = "Councillor testNode is proposing. Please wait.";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(printedOutput);

        // Check if the output matches the expected regex
        assertTrue(matcher.find(), "Output does not contain the expected string.");
    }

    @Test
    public void testProposalNumberGenerationAndIncrementation() {
        councillor.startProposal("value1");

        // Extract the current proposal number from the councillor after the first proposal
        String firstProposalNumber = councillor.craftProposalNumber();
        assertEquals("testNode:1", firstProposalNumber);

        // Resetting the councillor or its state for the next proposal
        councillor.resetProposalState();

        councillor.startProposal("value2");
        String secondProposalNumber = councillor.craftProposalNumber();
        assertEquals("testNode:2", secondProposalNumber);
    }

    @Test
    public void testProposalStateResetAfterAcceptance() {
        councillor.startProposal("value1");
        councillor.handlePrepare("testNode:1");
        councillor.handleAcceptRequest("testNode:1", "value1");
        councillor.resetProposalState();
        assertFalse(councillor.getIsProposing());
    }

    @Test
    public void testSimultaneousPrepareAndAccept() {
        councillor.handlePrepare("testNode:5");
        String responseAccept = councillor.handleAcceptRequest("testNode:5", "value1");
        String responsePrepare = councillor.handlePrepare("testNode:6");

        assertTrue(responseAccept.startsWith("ACCEPTED"));
        assertTrue(responsePrepare.startsWith("PROMISE"));
    }

    @Test
    public void testProposalWithoutMajority() {
        // Assuming there's a method to check for majority in your Paxos implementation.
        assertFalse(councillor.getPromiseMajority());
        assertFalse(councillor.getAcceptedMajority());
    }

    @Test
    public void testEmptyProposalValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            councillor.startProposal("");
        });
    }

    @Test
    public void testNullProposalValue() {
        assertThrows(NullPointerException.class, () -> {
            councillor.startProposal(null);
        });
    }
    @Test
    public void testResourceCleanup() {
        councillor.terminateVotingSystem();
        assertNull(councillor.getCommunicator());
    }

    @Test
    public void testTimeoutScenario() {
        councillor.startProposal("value1");
        // Simulating a delay to cause a timeout.
        try {
            Thread.sleep(16000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        assertFalse(councillor.getIsProposing());
    }
}
