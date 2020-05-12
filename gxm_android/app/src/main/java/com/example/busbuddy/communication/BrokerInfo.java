package com.example.busbuddy.communication;

//stoixeia twn brokers
public class BrokerInfo implements Comparable<BrokerInfo>{
    public String ip;
    public int port;
    public int hash;

    @Override
    public int compareTo(BrokerInfo o) {
        return Integer.compare(this.hash, o.hash); // epistrefei 0 an einai isa, enw an this < o epistrefei -1, enw an this > o epistrefei 1
    }
}
