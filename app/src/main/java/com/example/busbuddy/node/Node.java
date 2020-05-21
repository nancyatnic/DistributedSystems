package com.example.busbuddy.node;

import com.example.busbuddy.communication.BrokerInfo;

import java.util.ArrayList;
import java.util.List;


public class Node {

    public List<BrokerInfo> brokers = new ArrayList<BrokerInfo>();

    public String busLinesFile = "busLines.txt";
    public String busPositionsFile = "busPositions.txt";
    public String routeCodesFile = "RouteCodes.txt";

    // We print the brokers as a String with IP, Port and Hash
    public String brokerListToString() {
        String s = "";
        for (BrokerInfo bi : brokers) {
            s = s + bi.IP + "-" + bi.Port + "-" + bi.Hash + "-";
        }
        return s; // IP-Port-Hash
    }

    // We break the String into 3 parts
    public void brokerListFromString(String s) {
        String[] tokens = s.split("-");
        int n = tokens.length / 3;

        brokers.clear();                // We clear the List

        for (int i = 0; i < n; i++) {   // We match the brokers with the IP, Port and Hash
            BrokerInfo bi = new BrokerInfo();
            bi.IP = tokens[3*i];
            bi.Port = Integer.parseInt(tokens[3*i + 1]);    // We turn Port from String to int
            bi.Hash = Integer.parseInt(tokens[3*i + 2]);    // We turn Hash from String to int
            brokers.add(bi); // We add IP, Port and Hash into the BrokerInfo list
        }
    }    
}
