package com.example.busbuddy.node;

import com.example.busbuddy.communication.BrokerInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;


public class Client extends Node {

    private String server_ip = "192.168.1.4"; // configure first broker IP
    private int port = 5001;        // configure first broker port

    private int client_position = 0; // configure publisher position


    public void init() {

    }

    public Socket connect(int id) { //connect ston broker
        Socket requestSocket = null;

        try {
            if (super.brokers.size() > id) {
                server_ip = super.brokers.get(id).ip;
                port = super.brokers.get(id).port;
            }
            requestSocket = new Socket(InetAddress.getByName(server_ip), port);
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
        return requestSocket;
    }

    public Socket connectWithTimeout(int id, int timeout) { //connect ston broker
        Socket requestSocket = null;

        try {
            if (super.brokers.size() > id) {
                server_ip = super.brokers.get(id).ip;
                port = super.brokers.get(id).port;
            }
            requestSocket = new Socket(InetAddress.getByName(server_ip), port);
            requestSocket.setSoTimeout(timeout);
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
        return requestSocket;
    }


    public void disconnect(Socket requestSocket, ObjectOutputStream out,  ObjectInputStream in) { // kleisimo sindesis
        try {
            in.close();
            out.close();
            requestSocket.close();
        } catch (IOException ioException) {
            
        }
    }

    public void getBrokerList(ObjectOutputStream out,  ObjectInputStream in) { // pernoume tin brokerlist,dialogos me ton broker, kai ektiposi twn brokers
        try {
            String s = in.readUTF();

            out.writeUTF("subscriber");
            out.flush();

            s = in.readUTF();

            out.writeUTF("brokerlist");
            out.flush();

            s = in.readUTF();

            System.out.println("outcome: " + s);

            brokerListFromString(s);

            Collections.sort(brokers);

            System.out.println("----------- Brokers -----------");
            for (BrokerInfo x : brokers) {
                System.out.println(x.ip + "-" + x.port + "-" + x.hash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int findBroker(int hash) { //vriskoume ton antoistoixo broker meso tou hash
        if (brokers.size() == 1) {
            return 0;
        }

        for (int i = 0; i < brokers.size(); i++) {
            if (hash <= brokers.get(i).hash) {
                return i;
            }
        }
        return hash % brokers.size();
    }


}
