package com.example.busbuddy.communication;

//stoixeia twn brokers
public class BrokerInfo implements Comparable<BrokerInfo>{
    public String IP;
    public int Port;
    public int Hash;


    // Returns 0 if they are equal, if < it returns -1, if > it returns 1
    @Override
    public int compareTo(BrokerInfo o) {
        return Integer.compare(this.Hash, o.Hash);// Returns 0 if they are equal, if < it returns -1, if > it returns 1
    }
}
