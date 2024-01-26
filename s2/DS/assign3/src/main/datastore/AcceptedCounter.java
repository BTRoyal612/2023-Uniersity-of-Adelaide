package main.datastore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;

public class AcceptedCounter {
    private final HashMap<String, Integer> counts = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public int incrementAndReturnCount(String proposalNumber) {
        lock.lock();
        try {
            return counts.merge(proposalNumber, 1, Integer::sum);
        } finally {
            lock.unlock();
        }
    }

    public int getCount(String proposalNumber) {
        lock.lock();
        try {
            return counts.getOrDefault(proposalNumber, 0);
        } finally {
            lock.unlock();
        }
    }
}