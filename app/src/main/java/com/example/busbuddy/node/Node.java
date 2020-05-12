package com.example.busbuddy.node;

import com.example.busbuddy.communication.BrokerInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Node {

    public List<BrokerInfo> brokers = new ArrayList<BrokerInfo>();

    public String busLinesFile = "busLines.txt";
    public String busPositionsFile = "busPositions.txt";
    public String routeCodesFile = "RouteCodes.txt";

    public String brokerListToString() { //emfanisi twn brokers se morfi string me ip port k hash
        String s = "";
        for (BrokerInfo bi : brokers) {
            s = s + bi.ip + "-" + bi.port + "-" + bi.hash + "-";
        }
        return s; // ip-port-hash
    }

    public void brokerListFromString(String s) { // xorizoume to string se 3 merh
        String[] tokens = s.split("-");
        int n = tokens.length / 3;

        brokers.clear(); // katharizoume tin lista

        for (int i = 0; i < n; i++) { // antistoixei gia kathe broker ta ip.port.hash
            BrokerInfo bi = new BrokerInfo();
            bi.ip = tokens[3*i];
            bi.port = Integer.parseInt(tokens[3*i + 1]);// metatrepoyme to string port kai hash se int
            bi.hash = Integer.parseInt(tokens[3*i + 2]);
            brokers.add(bi); // prosthetoume stin lista BrokerInfo to ip, port, hash
        }
    }    
}
