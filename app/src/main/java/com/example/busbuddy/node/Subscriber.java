package com.example.busbuddy.node;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;

import com.example.busbuddy.ConfigurationActivity;
import com.example.busbuddy.MapsActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Subscriber extends Client {
    private final int TIMEOUT = 120000;

    public TreeMap<Integer, String> databaseLineIDToTitle = new TreeMap<>(); // taxinomimeni lista me linedi kai titlous
    public List<Integer> subscribedLists = new ArrayList<Integer>(); //lista pou krataei ta leoforeia sta opoia exei ginei subscribe

    public TreeMap<Integer, Client> subscribedThreads = new TreeMap<>(); // Thread for the connections with the subscribedLists

    public TreeMap<Integer, Socket> subscribedSockets = new TreeMap<>(); // Socket for the connections with the subscribedLists

    int mypos = 0;
    private Handler mainHandler;

    //  1 hashmap => log for each busline ID which thread serves it
    public boolean initByContext(Context context, Handler mainHandler) {
        this.mainHandler = mainHandler;     // initialization, read of the file, printout
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

    // Unsubscribe from a track and for a thread
    class UnsubscribeThread extends Thread { // stamatima sindromis gia ena leoforeio gia to thread

        Integer busLineID;
        private ConfigurationActivity configurationActivity;

        public UnsubscribeThread(Integer busLineID, ConfigurationActivity configurationActivity) {

            this.busLineID = busLineID;
            this.configurationActivity = configurationActivity;
        }

        public void run() {
            Unsubscribe(busLineID, configurationActivity);
        }
    }

    class GetBrokerListThread extends Thread {


        private Handler mainHandler;

        public GetBrokerListThread(Handler mainHandler) {

            this.mainHandler = mainHandler;
        }

        public void run() {
            try {
                Socket requestSocket = connect(0);      // Consumer's connection

                ObjectOutputStream out = null;              // Gets and sends streams
                ObjectInputStream in = null;

                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());


                Message msg = mainHandler.obtainMessage();
                msg.what = 1;
                mainHandler.sendMessage(msg);

                getBrokerList(out, in);                     // The Consumer receives the brokerlist from the broker

                Message msg2 = mainHandler.obtainMessage();
                msg2.what = 2;
                mainHandler.sendMessage(msg2);

                disconnect(requestSocket, out, in);         // We close the connection

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
            Subscribe(busLineID, configurationActivity);
        }
    }

    // We subscribe (with thread) to a track
    public Thread subscribeWithThread(Integer busLineID, ConfigurationActivity configurationActivity) { // subscribe se ena buslineID
        Thread t = new SubscribeThread(busLineID, configurationActivity);
        t.start();
        return t;
    }

    // We unsubscribe (with thread) from a track
    public Thread unsubscribeWithThread(Integer busLineID, ConfigurationActivity configurationActivity) { // subscribe se ena buslineID
        Thread t = new UnsubscribeThread(busLineID, configurationActivity);
        t.start();
        return t;
    }

    // We subscribe to a track
    public void Subscribe(Integer busLineID, final ConfigurationActivity configurationActivity) { // subscribe se ena buslineID
        Socket requestSocket = null;
        ObjectOutputStream out = null;      // Gets and sends streams
        ObjectInputStream in = null;

        try {
            Client client = new Client();
            client.brokers = Subscriber.this.brokers;

            subscribedLists.add(busLineID);
            subscribedThreads.put(busLineID, client);

            int hash = HashTopic(busLineID);

            int no = FindBroker(hash);

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

    // We unsubscribe from a track
    public void Unsubscribe(Integer busLineID, final ConfigurationActivity configurationActivity) { //unsubscribe antoistoixi gia ena buslineid
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

    // We hash the 'txt' variable using the MD5 hashing algorithm.
    public int HashTopic(Integer lineId) throws NoSuchAlgorithmException {
        String x = String.valueOf(lineId);

        MessageDigest m = MessageDigest.getInstance("MD5");

        byte[] messageDigest = m.digest((x).getBytes());

        BigInteger bi = new BigInteger(1, messageDigest);

        int Hash = Math.abs(bi.intValue());

        return Hash;
    }

    // When you subscribe in the menu, we print a list with the tracks
    public String[] getLines() {// otan patas subscribe sto menu , print lista me ta lines
        int length = databaseLineIDToTitle.size();

        if (length == 0) {
            return null;
        } else {
            int subs = 0;

            for (Map.Entry<Integer, String> pair : databaseLineIDToTitle.entrySet()) {      // Entryset, returns the hashmap
                if (subscribedLists.contains(pair.getKey())) {
                    subs++;
                }
            }

            String[] array = new String[length - subs];

            int i = 0;

            for (Map.Entry<Integer, String> pair : databaseLineIDToTitle.entrySet()) {      // Entryset, returns the hashmap
                if (!subscribedLists.contains(pair.getKey())) {
                    array[i++] = String.format("%03d - %s \n ", pair.getKey(), pair.getValue());
                }
            }
            return array;
        }
    }

    // When you unsubscribe in the menu, we print a list with the tracks you are subscribed to
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
