package main.datastore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;

public class PromiseCounter {
    private final HashMap<String, Integer> totalPromises = new HashMap<>();
    private final HashMap<String, HashMap<String, String>> acceptedValues = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public int incrementAndReturnTotalPromises(String proposalNumber) {
        lock.lock();
        try {
            return totalPromises.merge(proposalNumber, 1, Integer::sum);
        } finally {
            lock.unlock();
        }
    }

    public void storeAcceptedValue(String currentProposalNumber, String priorProposalNumber, String acceptedValue) {
        lock.lock();
        try {
            acceptedValues
                    .computeIfAbsent(currentProposalNumber, k -> new HashMap<>())
                    .put(priorProposalNumber, acceptedValue);
        } finally {
            lock.unlock();
        }
    }

    public HashMap<String, String> getAcceptedValuesForProposal(String proposalNumber) {
        lock.lock();
        try {
            return acceptedValues.getOrDefault(proposalNumber, new HashMap<>());
        } finally {
            lock.unlock();
        }
    }

    public int getTotalPromises(String proposalNumber) {
        lock.lock();
        try {
            return totalPromises.getOrDefault(proposalNumber, 0);
        } finally {
            lock.unlock();
        }
    }
}
