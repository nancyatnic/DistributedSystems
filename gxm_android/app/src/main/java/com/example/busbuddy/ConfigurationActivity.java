package com.example.busbuddy;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ConfigurationActivity extends AppCompatActivity {

    private Button buttonSubscribe;
    private Button buttonUnsubscribe;
    private Spinner spinnerBusline;
    private Spinner spinnerSubscribedBuses;

    class SubscribeListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (spinnerBusline.getSelectedItem() != null) {
                String item = spinnerBusline.getSelectedItem().toString();

                String[] fields = item.split("-");

                String busID = fields[0].trim();

                Integer value = Integer.parseInt(busID);

                Toast.makeText(ConfigurationActivity.this, "Trying to subscribe to " + busID + " clicked!", Toast.LENGTH_LONG).show();

                MapsActivity.subscriber.subscribeWithThread(value, ConfigurationActivity.this);
            }
        }
    }

    class UnSubscribeListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Toast.makeText(ConfigurationActivity.this, "Unsubscribe clicked!", Toast.LENGTH_LONG).show();

            if (spinnerSubscribedBuses.getSelectedItem() != null) {
                String item = spinnerSubscribedBuses.getSelectedItem().toString();


                String[] fields = item.split("-");

                String busID = fields[0].trim();

                Integer value = Integer.parseInt(busID);

                MapsActivity.subscriber.unsubscribeWithThread(value, ConfigurationActivity.this);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        buttonSubscribe = findViewById(R.id.idSubscribe);
        buttonUnsubscribe = findViewById(R.id.idUnsubscribe);
        spinnerBusline = findViewById(R.id.idBusLine);
        spinnerSubscribedBuses = findViewById(R.id.idSubscribedBuses);

        SubscribeListener bob = new SubscribeListener();
        UnSubscribeListener eva = new UnSubscribeListener();

        buttonSubscribe.setOnClickListener(bob);
        buttonUnsubscribe.setOnClickListener(eva);

        updateGUI();
    }

    public void updateGUI() {
        String[] arraySpinner = MapsActivity.subscriber.getLines();
        if (arraySpinner != null && arraySpinner.length > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner);
            spinnerBusline.setAdapter(adapter);
        } else {
            spinnerBusline.setAdapter(null);
        }

        String[] arraySpinner2 = MapsActivity.subscriber.getSubscribedLines();
        if (arraySpinner2 != null && arraySpinner2.length > 0) {
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arraySpinner2);
            spinnerSubscribedBuses.setAdapter(adapter2);
        } else {
            spinnerSubscribedBuses.setAdapter(null);
        }
    }
}
