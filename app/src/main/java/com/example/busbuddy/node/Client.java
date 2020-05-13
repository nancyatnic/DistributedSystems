package com.example.busbuddy.node;

import com.example.busbuddy.communication.BrokerInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;


public class Client extends Node {

    private String Server_IP = "192.168.1.4"; // configure first broker IP
    private int Port = 5001;        // configure first broker port

    private int client_position = 0; // configure publisher position


    public void init() {

    }

    public Socket connect(int ID) { //connect ston broker
        Socket requestSocket = null;

        try {
            if (super.brokers.size() > ID) {
                Server_IP = super.brokers.get(ID).IP;
                Port = super.brokers.get(ID).Port;
            }
            requestSocket = new Socket(InetAddress.getByName(Server_IP), Port);
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
        return requestSocket;
    }

    public Socket connectWithTimeout(int ID, int Timeout) { //connect ston broker
        Socket requestSocket = null;

        try {
            if (super.brokers.size() > ID) {
                Server_IP = super.brokers.get(ID).IP;
                Port = super.brokers.get(ID).Port;
            }
            requestSocket = new Socket(InetAddress.getByName(Server_IP), Port);
            requestSocket.setSoTimeout(Timeout);
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
                System.out.println(x.IP + "-" + x.Port + "-" + x.Hash);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int FindBroker(int Hash) { //vriskoume ton antoistoixo broker meso tou Hash
        if (brokers.size() == 1) {
            return 0;
        }

        for (int i = 0; i < brokers.size(); i++) {
            if (Hash <= brokers.get(i).Hash) {
                return i;
            }
        }
        return Hash % brokers.size();
    }


}
