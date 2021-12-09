package com.torrens.victor.carmanagerv2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Bluetooh extends AppCompatActivity {

    private List<ListItem> s;
    private String selectedDevice;
    private FirabaseSingleton singleton;
    private TextView editText;
    private String selectedDeviceName;
    private Intent intent;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooh);

        singleton = FirabaseSingleton.sharedInstance(this);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.blueService);

        selectedDevice = singleton.getDeviceAdress();
        editText.setText(singleton.getSelectedDeviceName());
        if (singleton.getSelectedDeviceName() == null) {

        } else {
            startService();
            button.setText(R.string.desBlue);
        }

        ListView list = findViewById(R.id.list);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        s = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices) {
            s.add(new ListItem(bt.getName(), bt.getAddress()));
        }
        MyListAdapter adapter = new MyListAdapter(this, s);

        list.setAdapter(adapter);
        list.setActivated(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedDevice = s.get(i).getNumber();
                selectedDeviceName = s.get(i).getName();
                singleton.setMACAdress(selectedDevice, selectedDeviceName);
                editText.setText(selectedDeviceName);

                startService();

                button.setText(R.string.desBlue);

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    singleton.setMACAdress(null, null);

                    intent = new Intent(getBaseContext(), BluetoohService.class);
                    stopService(intent);

                    button.setText(R.string.actBlue);
                    Toast.makeText(getApplicationContext(), R.string.desService, Toast.LENGTH_SHORT).show();
                }

        });
    }
    //TODO: explicacion notificacion al usuario

    private void startService() {
        intent = new Intent(getBaseContext(), BluetoohService.class);
        intent.putExtra("selectedDevice", selectedDevice);
        intent.putExtra("user", singleton.getmAuth().getCurrentUser().getUid());
        startService(intent);


        Toast.makeText(getApplicationContext(), "Servicio Activado", Toast.LENGTH_SHORT).show();
    }
}