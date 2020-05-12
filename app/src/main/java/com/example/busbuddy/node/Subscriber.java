package com.example.busbuddy.node;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;

import com.example.busbuddy.ConfigurationActivity;
import com.example.busbuddy.MapsActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Subscriber extends Client {
    private final int TIMEOUT = 120000;

    public TreeMap<Integer, String> databaseLineIDToTitle = new TreeMap<>(); // taxinomimeni lista me linedi kai titlous
    public List<Integer> subscribedLists = new ArrayList<Integer>(); //lista pou krataei ta leoforeia sta opoia exei ginei subscribe

    public TreeMap<Integer, Client> subscribedThreads = new TreeMap<>(); // antoistixo thread gia tis sindeseis gia to subscribedLists

    public TreeMap<Integer, Socket> subscribedSockets = new TreeMap<>(); // antoistixo thread gia tis sindeseis gia to subscribedLists

    int mypos = 0;
    private Handler mainHandler;

    //  1 hashmap => log for each busline ID which thread serves it
    public boolean initByContext(Context context, Handler mainHandler) {
        this.mainHandler = mainHandler; // arxikopoisi ,diavasma tou arxeiou, kai ektiposi
        super.init();

        try {
            AssetManager am = context.getAssets();

            InputStream inputStream = am.open(busLinesFile);

            InputStreamReader isr = new InputStreamReader(inputStream);

            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                Integer lineId = Integer.parseInt(fields[1]);
                String title = fields[2];
                databaseLineIDToTitle.put(lineId, title);
            }

            br.close();


            GetBrokerListThread t = new GetBrokerListThread(mainHandler);
            t.start();


            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void getBrokerList() {
        GetBrokerListThread t = new GetBrokerListThread(mainHandler);
        t.start();
    }

    private void visualizeData(String s) { //ektiposi stoixeion twn leoforeion
        System.out.println("Data received: " + s);
    }

    class UnsubscribeThread extends Thread { // stamatima sindromis gia ena leoforeio gia to thread

        Integer busLineID;
        private ConfigurationActivity configurationActivity;

        public UnsubscribeThread(Integer busLineID, ConfigurationActivity configurationActivity) {

            this.busLineID = busLineID;
            this.configurationActivity = configurationActivity;
        }

        public void run() {
            unsubscribe(busLineID, configurationActivity);
        }
    }

    class GetBrokerListThread extends Thread {


        private Handler mainHandler;

        public GetBrokerListThread(Handler mainHandler) {

            this.mainHandler = mainHandler;
        }

        public void run() {
            try {
                Socket requestSocket = connect(0);//sindesi tou subscriber

                ObjectOutputStream out = null; //pairnei kai stelnei (roes)
                ObjectInputStream in = null;

                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());


                Message msg = mainHandler.obtainMessage();
                msg.what = 1;
                mainHandler.sendMessage(msg);

                getBrokerList(out, in); // o subscriber lmvanei tin brokerlist apo ton broker

                Message msg2 = mainHandler.obtainMessage();
                msg2.what = 2;
                mainHandler.sendMessage(msg2);

                disconnect(requestSocket, out, in); // kleisimo sindesis

                Message msg3 = mainHandler.obtainMessage();
                msg3.what = 3;
                mainHandler.sendMessage(msg3);
            } catch (Exception ex) {

            }
        }
    }


    class SubscribeThread extends Thread {

        Integer busLineID;
        private ConfigurationActivity configurationActivity;

        public SubscribeThread(Integer busLineID, ConfigurationActivity configurationActivity) {
            this.busLineID = busLineID;
            this.configurationActivity = configurationActivity;
        }

        public void run() {
            subscribe(busLineID, configurationActivity);
        }
    }

    public Thread subscribeWithThread(Integer busLineID, ConfigurationActivity configurationActivity) { // subscribe se ena buslineID
        Thread t = new SubscribeThread(busLineID, configurationActivity);
        t.start();
        return t;
    }

    public Thread unsubscribeWithThread(Integer busLineID, ConfigurationActivity configurationActivity) { // subscribe se ena buslineID
        Thread t = new UnsubscribeThread(busLineID, configurationActivity);
        t.start();
        return t;
    }

    public void subscribe(Integer busLineID, final ConfigurationActivity configurationActivity) { // subscribe se ena buslineID
        Socket requestSocket = null;
        ObjectOutputStream out = null; //pairnei kai stelnei (roes)
        ObjectInputStream in = null;

        try {
            Client client = new Client();
            client.brokers = Subscriber.this.brokers;

            subscribedLists.add(busLineID);
            subscribedThreads.put(busLineID, client);

            int hash = hashTopic(busLineID);

            int no = findBroker(hash);

            requestSocket = connectWithTimeout(no, TIMEOUT);

            subscribedSockets.put(busLineID, requestSocket);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            String s = in.readUTF();

            out.writeUTF("subscriber");

            out.flush();

            s = in.readUTF();

            out.writeUTF("subscribe");

            out.flush();

            String msg = String.valueOf(busLineID);
            out.writeUTF(msg);
            out.flush();

            Message message = MapsActivity.mainHandler.obtainMessage();
            message.what = 4;
            MapsActivity.mainHandler.sendMessage(message);

            configurationActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    configurationActivity.updateGUI();
                }
            });


            while (true) {
                s = in.readUTF();
                visualizeData(s);

                message = MapsActivity.mainHandler.obtainMessage();
                message.what = 5;
                message.obj = s;

                MapsActivity.mainHandler.sendMessage(message);
            }
        } catch (SocketTimeoutException ex) {
            Message message = MapsActivity.mainHandler.obtainMessage();
            message.what = 8;
            message.obj = busLineID;
            MapsActivity.mainHandler.sendMessage(message);
            ex.printStackTrace();

            subscribedLists.remove(busLineID);

            if (requestSocket != null) {
                disconnect(requestSocket, out, in);
            }
        } catch (Exception ex) {
            Message message = MapsActivity.mainHandler.obtainMessage();
            message.what = 7;
            message.obj = ex.toString();
            MapsActivity.mainHandler.sendMessage(message);
            ex.printStackTrace();

            if (requestSocket != null) {
                disconnect(requestSocket, out, in);
            }
        }
    }

    public void unsubscribe(Integer busLineID, final ConfigurationActivity configurationActivity) { //unsubscribe antoistoixi gia ena buslineid
        subscribedLists.remove(busLineID);
        Client client = subscribedThreads.get(busLineID);

        Message message = MapsActivity.mainHandler.obtainMessage();
        message.what = 6;
        message.obj = busLineID;
        MapsActivity.mainHandler.sendMessage(message);

        configurationActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                configurationActivity.updateGUI();
            }
        });

        Socket socket = subscribedSockets.get(busLineID);
        try {
            socket.close();
        } catch (IOException e) {
        }
    }

    public int hashTopic(Integer lineId) throws NoSuchAlgorithmException { // algorithmos apo to stackoverflow gia ta hash
        String x = String.valueOf(lineId);

        MessageDigest md = MessageDigest.getInstance("MD5");

        byte[] messageDigest = md.digest((x).getBytes());

        BigInteger no = new BigInteger(1, messageDigest);

        int hash = Math.abs(no.intValue());

        return hash;
    }


    public String[] getLines() {// otan patas subscribe sto menu , print lista me ta lines
        int length = databaseLineIDToTitle.size();

        if (length == 0) {
            return null;
        } else {
            int subs = 0;

            for (Map.Entry<Integer, String> pair : databaseLineIDToTitle.entrySet()) { // entryset epistrefei ta periexomena tou hashmap
                if (subscribedLists.contains(pair.getKey())) {
                    subs++;
                }
            }

            String[] array = new String[length - subs];

            int i = 0;

            for (Map.Entry<Integer, String> pair : databaseLineIDToTitle.entrySet()) { // entryset epistrefei ta periexomena tou hashmap
                if (!subscribedLists.contains(pair.getKey())) {
                    array[i++] = String.format("%03d - %s \n ", pair.getKey(), pair.getValue());
                }
            }
            return array;
        }
    }

    public String[] getSubscribedLines() {// otan patas unsubscribe sto menu , print lista me ta lines pou eisai subscribed
        int length = subscribedLists.size();

        if (length == 0) {
            return null;
        } else {
            String[] array = new String[length];

            int i = 0;

            for (Map.Entry<Integer, String> pair : databaseLineIDToTitle.entrySet()) {
                if (subscribedLists.contains(pair.getKey())) {
                    array[i++] = String.format("%03d - %s \n ", pair.getKey(), pair.getValue());
                }
            }

            return array;
        }
    }
}
