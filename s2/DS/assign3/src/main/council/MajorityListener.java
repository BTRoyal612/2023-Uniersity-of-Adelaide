package main.council;

public interface MajorityListener {
    void onPromiseMajorityReached(String proposedNumber);
    void onAcceptedMajorityReached(String proposedNumber);
}
