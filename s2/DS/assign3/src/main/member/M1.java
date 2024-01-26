package main.member;

import main.communication.Communicator;
import main.council.Councillor;
import main.datastore.AddressPortPair;

import java.io.IOException;
import java.util.Map;

public class M1 extends Councillor {
    public M1(String id, Map<String, AddressPortPair> councillorMap, int totalCouncillors, Communicator communicator) throws IOException {
        super(id, councillorMap, totalCouncillors, communicator);
    }
    // No additional methods or overrides needed for M1
}
