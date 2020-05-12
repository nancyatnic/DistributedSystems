package com.example.busbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.busbuddy.node.Subscriber;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final Subscriber subscriber = new Subscriber();
    public static Handler mainHandler = null;

    private GoogleMap mMap;

    private HashMap<String, Marker> vehicleIDtoMarker = new HashMap<>();
    private HashMap<String, List<String>> lineIDtoVehicleID = new HashMap<>();

    private Thread monitorThread = null;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                try {
                    if (msg.what == 1) {
                        Toast.makeText(MapsActivity.this, "Connected. Getting broker list", Toast.LENGTH_SHORT).show();
                    } else if (msg.what == 2) {
                        Toast.makeText(MapsActivity.this, "Broker list downloaded", Toast.LENGTH_SHORT).show();
                    } else if (msg.what == 3) {
                        Toast.makeText(MapsActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    } else if (msg.what == 4) {
                        Toast.makeText(MapsActivity.this, "subscribe ok!", Toast.LENGTH_SHORT).show();
                    } else if (msg.what == 5) {
                        Toast.makeText(MapsActivity.this, "Sample received!" + msg.obj.toString(), Toast.LENGTH_SHORT).show();

                        String[] fields = msg.obj.toString().split(",");

                        String lineid = fields[0];
                        String linecode = fields[1];
                        String vid = fields[3];
                        Double latitude = Double.parseDouble(fields[4]);
                        Double longitude = Double.parseDouble(fields[5]);

                        // -------------------------

                        if (lineIDtoVehicleID.containsKey(lineid)) {
                            List<String> vids = lineIDtoVehicleID.get(lineid);
                            if (!vids.contains(vid)) {
                                vids.add(vid);
                            }
                        } else {
                            List<String> vids = new ArrayList<>();
                            vids.add(vid);
                            lineIDtoVehicleID.put(lineid, vids);
                        }

                        // -------------------------

                        if (vehicleIDtoMarker.containsKey(vid)) {
                            Marker marker = vehicleIDtoMarker.get(vid);
                            LatLng newpos = new LatLng(latitude, longitude);
                            marker.setPosition(newpos);
                        } else {
                            LatLng initialpos = new LatLng(latitude, longitude);
                            Marker m = mMap.addMarker(new MarkerOptions().position(initialpos).title(vid));
                            vehicleIDtoMarker.put(vid, m);
                        }
                    } else if (msg.what == 8) {
                        Integer busLineID = (Integer) msg.obj;

                        String s = String.valueOf(busLineID);

                        if (lineIDtoVehicleID.containsKey(s)) {
                            List<String> vids = lineIDtoVehicleID.get(s);

                            for (String vid : vids) {
                                if (vehicleIDtoMarker.containsKey(vid)) {
                                    Marker marker = vehicleIDtoMarker.get(vid);
                                    marker.remove();
                                    vehicleIDtoMarker.remove(vid);
                                }
                            }

                            lineIDtoVehicleID.remove(busLineID);
                        }

                        Toast.makeText(MapsActivity.this, "timeout ok from " + busLineID, Toast.LENGTH_SHORT).show();
                    } else if (msg.what == 6) {
                        Integer busLineID = (Integer) msg.obj;

                        String s = String.valueOf(busLineID);

                        if (lineIDtoVehicleID.containsKey(s)) {
                            List<String> vids = lineIDtoVehicleID.get(s);

                            for (String vid : vids) {
                                if (vehicleIDtoMarker.containsKey(vid)) {
                                    Marker marker = vehicleIDtoMarker.get(vid);
                                    marker.remove();
                                    vehicleIDtoMarker.remove(vid);
                                }
                            }

                            lineIDtoVehicleID.remove(busLineID);
                        }

                        Toast.makeText(MapsActivity.this, "unsubscribe ok from " + busLineID, Toast.LENGTH_SHORT).show();
                    } else if (msg.what == 7) {
                        Toast.makeText(MapsActivity.this, "Error:" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        subscriber.initByContext(this, mainHandler); //arxikopoisi subscriber
    }

    @Override
    protected void onResume() {
        super.onResume();

        monitorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(3000);
                        subscriber.getBrokerList();
                    }
                } catch (Exception e) {

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (monitorThread != null) {
            monitorThread.interrupt();
            monitorThread =null;
        }

    }


                /**
                 * Manipulates the map once available.
                 * This callback is triggered when the map is ready to be used.
                 * This is where we can add markers or lines, add listeners or move the camera. In this case,
                 * we just add a marker near Sydney, Australia.
                 * If Google Play services is not installed on the device, the user will be prompted to install
                 * it inside the SupportMapFragment. This method will only be triggered once the user has
                 * installed Google Play services and returned to the app.
                 */
        @Override
        public void onMapReady (GoogleMap googleMap){
            mMap = googleMap;

            LatLng athens = new LatLng(37.9837, 23.7293);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(athens));
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.mainmenu, menu);
            return true;
        }


        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle item selection
            switch (item.getItemId()) {
                case R.id.idConfigure:
                    Intent intent = new Intent(this, ConfigurationActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.idExit:
                    this.finish();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    }
