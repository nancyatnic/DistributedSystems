package com.example.busbuddy.communication;

//stoixeia twn brokers
public class BrokerInfo implements Comparable<BrokerInfo>{
    public String IP;
    public int Port;
    public int Hash;

    @Override
    public int compareTo(BrokerInfo o) {
        return Integer.compare(this.Hash, o.Hash); // epistrefei 0 an einai isa, enw an this < o epistrefei -1, enw an this > o epistrefei 1
    }
}
