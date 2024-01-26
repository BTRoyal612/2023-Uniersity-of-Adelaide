package main.council;

import main.communication.Communicator;
import main.datastore.AcceptedCounter;
import main.datastore.AddressPortPair;
import main.datastore.PromiseCounter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Councillor implements MajorityListener {
    private String nodeId;
    protected Communicator communicator;
    private int currentSequenceNumber;
    private String acceptedProposalNumber;
    private String highestReceivedProposalNumber;
    private String acceptedValue;
    private String proposedValue;
    private int totalCouncillors;
    private Map<String, AddressPortPair> councillorMap;
    private boolean isProposing = false;
    private String currentDecision = null;
    protected final ExecutorService executorService;
    private ScheduledFuture<?> timeoutHandle;
    private final PromiseCounter promiseCounter = new PromiseCounter();
    private final AcceptedCounter acceptedCounter = new AcceptedCounter();
    private final AtomicBoolean isPromiseMajority = new AtomicBoolean(false);
    private final AtomicBoolean isAcceptedMajority= new AtomicBoolean(false);
    private final AtomicBoolean decisionFinalized = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Runnable onPromiseMajorityCompletion;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Councillor(String id, Map<String, AddressPortPair> councillorMap, int totalCouncillors, Communicator communicator) {
        if (!councillorMap.containsKey(id)) {
            throw new IllegalArgumentException("ID not found in the councillorMap.");
        }

        this.nodeId = id;
        this.totalCouncillors = totalCouncillors;
        this.councillorMap = councillorMap;
        this.currentSequenceNumber = 0; // Initialize to zero, will increment during proposal

        executorService = Executors.newFixedThreadPool(totalCouncillors);

        this.communicator = communicator;
        this.communicator.setCouncillor(this);
        // Start the listening loop in a separate thread to avoid blocking the current thread
        new Thread(communicator::startListening).start();
    }

    public String getNodeId() {return this.nodeId;}
    public int getTotalCouncillors() {return this.totalCouncillors;}
    public String getAcceptedValue() {return this.acceptedValue;}
    public int getCurrentSequenceNumber() {return this.currentSequenceNumber;}
    public Communicator getCommunicator() {return this.communicator;}
    public Map<String, AddressPortPair> getCouncillorMap() {return councillorMap;}
    public boolean getIsProposing() {return this.isProposing;}
    public PromiseCounter getPromiseCounter() {return this.promiseCounter;}
    public AcceptedCounter getAcceptedCounter() {return this.acceptedCounter;}
    public boolean getPromiseMajority() {return isPromiseMajority.get();}
    public boolean getAcceptedMajority() {return isPromiseMajority.get();}
    public boolean trySetPromiseMajorityTrue() {
        return isPromiseMajority.compareAndSet(false, true);
    }
    public boolean trySetPromiseMajorityFalse() {
        return isPromiseMajority.compareAndSet(true, false);
    }
    public boolean trySetAcceptedMajorityTrue() {
        return isAcceptedMajority.compareAndSet(false, true);
    }
    public boolean trySetAcceptedMajorityFalse() {
        return isAcceptedMajority.compareAndSet(true, false);
    }
    public void setOnPromiseMajorityCompletion(Runnable completion) {
        this.onPromiseMajorityCompletion = completion;
    }
    public String craftProposalNumber() {
        return this.nodeId + ":" + this.currentSequenceNumber;
    }

    private LocalDateTime getCurrentTime() {
        long currentTimeMillis = System.currentTimeMillis();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis),
                ZoneId.systemDefault());
    }
    private Map<String, String> interpretProposalNumber(String proposalNumber) {
        String[] parts = proposalNumber.split(":");
        if (parts.length != 2) {
            // Handle the error or throw an exception
            throw new IllegalArgumentException("Invalid proposal number format");
        }

        Map<String, String> interpretation = new HashMap<>();
        interpretation.put("nodeId", parts[0]);
        interpretation.put("sequenceNumber", parts[1]);

        return interpretation;
    }

    private boolean isSameProposal(String newProposal, String existingProposal) {
        return newProposal.equals(existingProposal);
    }

    private boolean isNewerProposal(String newProposal, String existingProposal) {
        Map<String, String> newProps = interpretProposalNumber(newProposal);
        Map<String, String> existingProps = interpretProposalNumber(existingProposal);

        int newSequence = Integer.parseInt(newProps.get("sequenceNumber"));
        int existingSequence = Integer.parseInt(existingProps.get("sequenceNumber"));

        // First, compare based on sequence numbers
        if (newSequence != existingSequence) {
            return newSequence > existingSequence;
        }

        // If sequence numbers are equal, then compare node IDs lexicographically
        return newProps.get("nodeId").compareTo(existingProps.get("nodeId")) > 0;
    }


    private String getNewestAcceptedValue(HashMap<String, String> acceptedValues) {
        String newestProposal = null;
        String newestAcceptedValue = null;

        for (Map.Entry<String, String> entry : acceptedValues.entrySet()) {
            String proposal = entry.getKey();
            String value = entry.getValue();

            if (newestProposal == null || isNewerProposal(proposal, newestProposal)) {
                newestProposal = proposal;
                newestAcceptedValue = value;
            }
        }

        return newestAcceptedValue; // This will return the accepted value associated with the newest proposal
    }

    @Override
    public void onPromiseMajorityReached(String proposalNumber) {
        // Check and update the majority status atomically
        if (trySetPromiseMajorityTrue()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " reach Majority on PROMISE: " + proposalNumber);

            // Cancel the timeout
            if (timeoutHandle != null && !timeoutHandle.isDone()) {
                timeoutHandle.cancel(false);
            }

            // Handle when majority is reached
            // E.g., move to the next phase
            HashMap<String, String> acceptedValues = promiseCounter.getAcceptedValuesForProposal(proposalNumber);
            if (acceptedValues != null && !acceptedValues.isEmpty()) {
                // If there are no previously accepted values, use the user's value
                this.proposedValue = getNewestAcceptedValue(acceptedValues);
            }

            sendAcceptRequest(proposalNumber);
        }
        // If trySetPromiseMajorityTrue() returns false, the code here won't be executed because the majority was already reached.
    }

    @Override
    public void onAcceptedMajorityReached(String proposalNumber) {
        if (trySetAcceptedMajorityTrue()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " reach Majority on ACCEPTED: " + proposalNumber);

            if (timeoutHandle != null && !timeoutHandle.isDone()) {
                timeoutHandle.cancel(false);
            }

            // Handle when majority is reached
            // E.g., move to the next phase
            HashMap<String, String> acceptedValues = promiseCounter.getAcceptedValuesForProposal(proposalNumber);
            if (acceptedValues != null && !acceptedValues.isEmpty()) {
                // If there are no previously accepted values, use the user's value
                this.proposedValue = getNewestAcceptedValue(acceptedValues);
            }

            sendDecision(proposalNumber);
        }
    }

    public void handleTimeout() {
        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " has timed out: " + craftProposalNumber());
        resetProposalState();
    }

    public void broadcast(String message) {
        // Exclude self in initial broadcast
        for (String councillorId : getCouncillorMap().keySet()) {
            if (!councillorId.equals(getNodeId())) {  // Exclude self
                AddressPortPair address = getCouncillorMap().get(councillorId);
                executorService.submit(() -> {
                    communicator.sendRequest(address.getHost(), address.getPort(), message);
                });
            }
        }

        // Finally, send the message to self
        executorService.submit(() -> {
            AddressPortPair selfAddress = getCouncillorMap().get(getNodeId());
            communicator.sendRequest(selfAddress.getHost(), selfAddress.getPort(), message);
        });
    }

    // Prepare phase: Proposer sends prepare request
    public void sendPrepare() {
        if (decisionFinalized.get()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has reached decision: " + getDecision());
            return;  // Don't send any more decisions if one has already been reached
        }

        this.currentSequenceNumber += 1; // Increment the proposal number
        String message = "PREPARE " + craftProposalNumber();

        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " broadcast: " + message);

        broadcast(message);

        // Set up a timeout for waiting for promises
        timeoutHandle = scheduler.schedule(new Runnable() {
            public void run() {
                // Handle the timeout. E.g., abort, retry, or log the event
                handleTimeout();
            }
        }, 15, TimeUnit.SECONDS); // 10 seconds timeout
    }

    // Promise phase: Acceptor sends promise
    public synchronized String handlePrepare(String receivedProposalNumber) {
        if (decisionFinalized.get()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has reached decision: " + getDecision());
            return null;  // Don't send any more decisions if one has already been reached
        }
        String responseMessage;
        // If this is the first proposal this councillor has seen or if the received proposal is newer
        if (this.highestReceivedProposalNumber == null ||
                isNewerProposal(receivedProposalNumber, this.highestReceivedProposalNumber)) {

            // Update the highest received proposal number
            this.highestReceivedProposalNumber = receivedProposalNumber;

            // Construct a response message based on whether we've accepted any proposals in the past
            if (this.acceptedProposalNumber != null && !this.acceptedProposalNumber.isEmpty()) {
                // If we've accepted a proposal before, send back a promise with the details
                responseMessage = "PROMISE " + receivedProposalNumber + " " + this.acceptedProposalNumber + " " + this.acceptedValue;
            } else {
                // If we've not accepted any proposals before, just send back a promise without any prior details
                responseMessage = "PROMISE " + receivedProposalNumber;
            }
        } else {
            // If the received proposal is not newer, you can choose to ignore or send a NACK
            responseMessage = "NACK " + receivedProposalNumber;
        }

        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " response: " + responseMessage);

        return responseMessage;
    }

    // Accept Request: Proposer sends accept request after majority promise
    public void sendAcceptRequest(String proposalNumber) {
        if (decisionFinalized.get()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has reached decision: " + getDecision());
            return;  // Don't send any more decisions if one has already been reached
        }
        String message = "ACCEPT_REQUEST " + proposalNumber + " " + this.proposedValue;

        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " broadcast: " + message);

        broadcast(message);

        if (onPromiseMajorityCompletion != null) {
            onPromiseMajorityCompletion.run();
        }

        // Set up a timeout for waiting for promises
        timeoutHandle = scheduler.schedule(new Runnable() {
            public void run() {
                // Handle the timeout. E.g., abort, retry, or log the event
                handleTimeout();
            }
        }, 15, TimeUnit.SECONDS); // 10 seconds timeout
    }

    // Accepted Phase: Acceptor sends accepted message after accept request
    public synchronized String handleAcceptRequest(String receivedProposalNumber, String value) {
        if (decisionFinalized.get()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has reached decision: " + getDecision());
            return null;  // Don't send any more decisions if one has already been reached
        }

        String responseMessage;
        // If this is the first proposal this councillor has seen or if the received proposal is newer
        if (this.highestReceivedProposalNumber == null ||
                isSameProposal(receivedProposalNumber, this.highestReceivedProposalNumber) ||
                isNewerProposal(receivedProposalNumber, this.highestReceivedProposalNumber)) {

            // Update the proposal number
            this.highestReceivedProposalNumber = receivedProposalNumber;
            this.acceptedProposalNumber = receivedProposalNumber;
            this.acceptedValue = value;

            responseMessage = "ACCEPTED " + this.acceptedProposalNumber + " " + value;

        } else {
            // If the received proposal is not newer, you can choose to ignore or send a NACK
            responseMessage = "REJECTED " + receivedProposalNumber;
        }

        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " response: " + responseMessage);

        return responseMessage;
    }

    // Decide Phase: Proposer broadcasts decision after majority acceptance
    public void sendDecision(String proposalNumber) {
        if (decisionFinalized.get()) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has reached decision: " + getDecision());
            return;  // Don't send any more decisions if one has already been reached
        }
        String message = "DECIDE " + proposalNumber + " " + this.proposedValue;

        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " broadcast: " + message);

        broadcast(message);
    }

    // Acceptors handle the decision and update their state
    public synchronized void handleDecision(String decidedProposalNumber, String decidedValue) {
        if (decisionFinalized.getAndSet(true)) {
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " has reached decision: " + getDecision());
            return;  // If a decision has already been reached, ignore further decisions
        }

        this.acceptedProposalNumber = decidedProposalNumber;
        this.acceptedValue = decidedValue;
        this.currentDecision = decidedValue;

        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " reaches decision at: " + decidedProposalNumber
                        + ". The president is "+ decidedValue);

        // Here, you can terminate the system or perform any other clean-up actions
        resetProposalState();
        terminateVotingSystem();
    }

    public String getDecision() {
        return currentDecision != null ? currentDecision : "Not yet decided";
    }

    public void startProposal(String proposedValue) {
        // Check for null proposedValue and throw NullPointerException
        if (proposedValue == null) {
            throw new NullPointerException("The proposed value cannot be null.");
        } else if (proposedValue.isEmpty()) {
            throw new IllegalArgumentException("The proposed value cannot be empty.");
        }

        // Implementation to start the Paxos proposal phase with the given value
        if (this.isProposing) {
            // Inform user they cannot start a new proposal yet
            LocalDateTime dateTime = getCurrentTime();
            System.out.println(
                    "[" + dateTime.format(formatter) + "] " + "Councillor "
                            + getNodeId() + " is proposing. Please wait.");
            return;
        }
        this.isProposing = true;
        this.proposedValue = proposedValue;

        // Start the Paxos prepare phase
        sendPrepare();
    }

    public void resetProposalState() {
        this.isProposing = false;
        this.proposedValue = null;
        this.highestReceivedProposalNumber = null;
        this.acceptedProposalNumber = null;
        this.acceptedValue = null;

        trySetPromiseMajorityFalse();
        trySetAcceptedMajorityFalse();

        // Inform user that the proposal is completed and they can initiate a new one
        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " has finished proposed. You can start a new one.");
    }

    public void terminateVotingSystem() {
        // This method will contain logic to shut down the voting system.
        // Depending on your system's architecture, this might involve stopping threads, closing sockets, etc.
        LocalDateTime dateTime = getCurrentTime();
        System.out.println(
                "[" + dateTime.format(formatter) + "] " + "Councillor "
                        + getNodeId() + " terminate voting system.");

        this.communicator.close();
        this.communicator = null;
    }
}
