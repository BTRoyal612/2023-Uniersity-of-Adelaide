# Paxos Consensus System
This system implements the Paxos consensus algorithm, designed to reach agreement among distributed processes (nodes) in a network, particularly in the presence of faulty or unreliable nodes. The implementation models a council of nodes that propose, promise, accept, or reject values based on the algorithm's rules to eventually achieve consensus on a single value.

## System Structure
The codebase is structured as follows:

```
`src`
├── `main`
│   ├── `communication`
│   │   ├── `Communicator.java`
│   │   └── `SocketCommunicator.java`
│   ├── `council`
│   │   ├── `CouncilRequestHandler.java`
│   │   ├── `Councillor.java`
│   │   └── `MajorityListener.java`
│   ├── `datastore`
│   │   ├── `AcceptedCounter.java`
│   │   ├── `AddressPortPair.java`
│   │   └── `PromiseCounter.java`
│   └── `member`
│       ├── `M1.java`
│       ├── `M2.java`
│       ├── `M3.java`
│       └── `Neutral.java`
└── `test`
    ├── `council`
    │   └── `CouncillorTest.java`
    ├── `member`
    │   └── `MemberTest.java`
    └── `system`
        └── `PaxosTest.java`
```
## Understanding Paxos Algorithm: The 3 Phases

Paxos is a consensus algorithm that operates in three distinct phases to ensure a network of distributed systems (or nodes) can agree on a single value, even in the presence of failures. The three phases are:

### Phase 1: Prepare
1. **Proposal Initiation**: A proposer selects a proposal number `n` and sends a prepare request with this number to a quorum of acceptors.
2. **Promise**: Acceptors respond to the prepare requests. An acceptor must promise not to accept any more proposals numbered less than `n`. If the acceptor has already accepted a proposal, it sends the proposal with the highest number it has accepted so far.

### Phase 2: Accept
1. **Proposer Receives Promises**: Once the proposer receives a majority of promises from a quorum, it needs to set the value for its proposal.
2. **Sending Accept Requests**: If any acceptor has accepted a proposal, the proposer must use the value of the highest-numbered proposal among the responses. If none of the acceptors have accepted any proposals, the proposer can choose a new value. The proposer then sends an accept request with the proposal number `n` and the value to the acceptors.

### Phase 3: Decide
1. **Acceptors Respond to Accept Requests**: Acceptors, upon receiving the accept request, accept the proposal unless they have already promised to another proposal with a higher number.
2. **Decision and Learning**: Once the proposer learns that a majority of acceptors have accepted its proposal, it considers the proposal chosen. The proposer then sends a message to all nodes (or learners) informing them of the decision.

This sequence ensures that despite failures or message loss, the Paxos algorithm can achieve consensus as long as a majority of nodes can communicate reliably. Furthermore, due to its design, Paxos guarantees that if any value is chosen, then that value will be the only one ever to be chosen in subsequent runs, hence achieving consistency and reliability in distributed systems.

## Achieving the Behavior of `M2`

The `M2` class in the system is designed to simulate a councillor with dynamic network conditions, including being offline, at a cafe with a stable connection, or experiencing poor connectivity. To utilize `M2` in your application and replicate its behavior, follow these steps:

### Initialization
First, instantiate an `M2` object with the required parameters:
- `id`: A unique identifier for the councillor.
- `councillorMap`: A map of other councillors' IDs to their respective `AddressPortPair` objects.
- `totalCouncillors`: The total number of councillors.
- `communicator`: An instance of `Communicator` for network communications.

```java
Map<String, AddressPortPair> councillorMap = // ... initialize your map
Communicator communicator = // ... initialize your communicator
M2 m2 = new M2("m2-id", councillorMap, totalCouncillors, communicator);
```

### Behavior Simulation
`M2` automatically simulates various behaviors:

1. **At the Cafe**:
    - `M2` has a 20% chance of being at Sheoak Cafe during each message broadcast.
    - When at the cafe, `M2` has a stable connection and sends messages without delay.

2. **Offline State**:
    - To set `M2` as offline, which prevents any message broadcast:
      ```java
      m2.setOffline(true);
      ```
    - To check if `M2` is offline:
      ```java
      boolean isOffline = m2.isActuallyOffline();
      ```

3. **Poor Connection**:
    - When not at the cafe, `M2` simulates a poor connection.
    - Messages may be sent with a delay of 5 to 10 seconds, or not sent at all.

### Messaging
To broadcast a message via `M2`, use the `broadcast` method. The method decides internally whether the message will be sent immediately, delayed, or dropped based on the current simulated network condition.

```java
m2.broadcast("Hello, World!");
```

### Request Handling
`M2` also introduces variable delays or drops requests during the handling of `prepare` and `acceptRequest` methods in the consensus process.

- It ensures:
    - Immediate response when at the cafe.
    - Delayed response or request drop based on connectivity.

### Thread Safety
Methods within `M2` are synchronized where necessary to ensure thread safety, particularly in handling the consensus protocol methods (`handlePrepare`, `handleAcceptRequest`).

Remember that `M2`'s behavior is nondeterministic due to the use of random number generation. This design aims to simulate real-world scenarios of network instability and their impacts on distributed systems.

## Achieving the Behavior of `M3`

The `M3` class in the system represents a councillor with unique characteristics such as going "camping," which is a metaphor for being temporarily offline or unavailable. Below is a guide on how to implement and understand the behavior of `M3`.

### Initialization
To initialize an instance of `M3`:

```java
Map<String, AddressPortPair> councillorMap = // ... initialize your map
Communicator communicator = // ... initialize your communicator
M3 m3 = new M3("m3-id", councillorMap, totalCouncillors, communicator);
```

### Understanding `M3` Behaviors

1. **Camping (Being Offline)**:
    - `M3` randomly decides to go camping (i.e., become temporarily offline).
    - The default probability of going camping is 10% each time `broadcast` is called.
    - While camping, `M3` will not send or process any messages.

2. **Setting Camping Behavior**:
    - You can control `M3`'s behavior using these methods:
        - `goToCamping()`: Forces `M3` to go camping.
        - `backFromCamping()`: Manually set `M3` to return from camping. Useful for testing.

3. **Message Broadcasting with Delays**:
    - `M3` introduces random delays to message broadcasting to simulate variable responsiveness.
    - The delay ranges from 100 milliseconds to 1 second, adding realism to the message propagation.

4. **Handling Consensus Messages**:
    - `M3` also handles consensus protocol messages (`handlePrepare` and `handleAcceptRequest`) with variable delays, simulating different response times in a real-world scenario.
    - Responses can be quick (100-500 milliseconds) or slower (1-3 seconds), depending on a random determination.

### Usage

- **Broadcasting a Message**:
  To broadcast a message using `M3`, simply call:
  ```java
  m3.broadcast("Your message here");
  ```
  This will automatically account for `M3`'s current state (camping or not) and include message delays.

- **Camping State**:
  To check if `M3` is currently camping (offline), use:
  ```java
  boolean isCamping = m3.isCurrentlyCamping();
  ```

### Thread Safety
Like other councillors in the system, `M3` ensures thread safety in its operations, particularly for message handling and state transitions (e.g., going camping or coming back).

### Conclusion
`M3` adds a layer of complexity and realism to the simulation by introducing unpredictable offline periods and variable responsiveness. Its behavior is a key component in testing the resilience and performance of distributed systems under realistic network conditions.

## Achieving the Behavior of Neutrals (M4 to M9)

The `Neutral` class extends the `Councillor` class, representing councillors M4 to M9 in the system. These councillors are termed as "Neutrals" and primarily contribute by introducing variable delays in message handling, simulating different network and processing speeds. Below is how you can achieve and understand the behavior of Neutrals.

### Initialization
To initialize an instance of `Neutral`:

```java
Map<String, AddressPortPair> councillorMap = // ... initialize your map
        Communicator communicator = // ... initialize your communicator
        Neutral neutral = new Neutral("neutral-id", councillorMap, totalCouncillors, communicator);
```

### Understanding Neutrals' Behaviors

1. **Variable Delays in Handling Requests**:
    - `Neutral` councillors introduce random delays before handling consensus messages (`handlePrepare` and `handleAcceptRequest`).
    - This delay simulates the time taken for a message to be processed and responded to, mimicking real-world scenarios where network and processing speeds can vary.

2. **Random Delay Distribution**:
    - 5% of the time, a Neutral councillor responds quickly with a delay between 100 to 500 milliseconds.
    - The other 95% of the time, the response delay ranges from 1 to 3 seconds.
    - This variability ensures that the simulation reflects both optimal and sub-optimal network conditions.

### Usage

- **Consensus Protocol Messages**:
  When a Neutral instance receives a prepare or accept request, it will automatically introduce these random delays. For example:
  ```java
  String response = neutral.handlePrepare("Proposal Number");
  ```
  ```java
  String response = neutral.handleAcceptRequest("Proposal Number", "Value");
  ```
  These methods will incorporate the delay internally before processing the request.

### Importance in Simulation

- Neutrals play a crucial role in testing the distributed system's resilience and efficiency under varying network conditions.
- They help in understanding the impact of different response times on the overall consensus protocol.

### Thread Safety
Neutral instances ensure thread safety in their operations. Synchronized methods in the handling of prepare and accept requests guarantee that state changes and message processing do not collide, thus preventing data races and inconsistencies.

### Conclusion
Implementing Neutrals like M4 to M9 in a distributed system is critical for creating a realistic and robust simulation environment. They help assess the system's behavior under different network and processing conditions, ensuring that the system is well-tested for real-world deployment.

## Using Makefile

### Compilation
To compile the project, use the following command:
```sh
make all
```

### Running Tests
To run the tests, execute:
```sh
make test
```
This will run a suite of 25 tests, encompassing both unit and system tests.

## Test Descriptions

### Unit Tests
- `CouncillorTest` and `MemberTest`: These are focused on testing individual components and edge cases.

### System Tests (`PaxosTest`)
1. **3 Nodes with Instant Response (M1 and M3 Proposals)**: Tests with 3 nodes where M1 proposes "M1" and M3 proposes "M3". M1 likely to be chosen. (case: `testPaxosConsensusWithThreeNodes()`)

2. **9 Nodes with Instant Response (Simultaneous M1 and M3 Proposals)**: Tests with 9 nodes where M1 and M3 propose simultaneously. M3's proposal likely to win due to higher proposal number. (case: `testPaxosConsensusWithNineNodes()`)

3. **M2 Proposes, Then Goes Offline; M1 and M3 Propose Themselves**: M2 proposes then goes offline; M1 and M3 propose themselves. Outcome is unpredictable but M3 often wins. (case: `testM2ProposesThenGoesOffline()`)

4. **M3 Proposes, Goes Camping, Then Returns; M1 and M2 Propose in the Meantime**: M3 proposes, becomes inactive, then returns. M1 and M2 propose during this period. M1 and M3 have higher chances of winning. (case: `testM3ProposesThenGoesCamping()`)