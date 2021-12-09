package com.torrens.victor.carmanagerv2;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
//import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BluetoohService extends Service {

    private FirabaseSingleton singleton;

    private String selectedDevice;
    private BlueReciver mReciver;
    private Location location;
    private LocationManager locationManager;
    private double lati, longi;
    private String currentUser;
    private DatabaseReference myRef;
    private FirebaseDatabase database;


    public class BlueReciver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (selectedDevice.equals(device.getAddress())) {
                Log.wtf("reciver", "ola");

                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                lati = location.getLatitude();
                longi = location.getLongitude();
                LatLng current = new LatLng(lati, longi);


                long currentTime = System.currentTimeMillis();
                myRef.child(currentUser).child("location").child(String.valueOf(currentTime)).setValue(current);
               // singleton.setLastLocation(current);
//
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("SET_POSITION");
                double[] array= new double[2];
                array[0]=lati;
                array[1]=longi;

                broadcastIntent.putExtra("pos", array);
                singleton.getMainActivity().sendBroadcast(broadcastIntent);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        singleton = FirabaseSingleton.sharedInstance();
        Bundle b = null;

        b = intent.getExtras();
        selectedDevice = b.getString("selectedDevice");
        currentUser= b.getString("user");
        database = FirebaseDatabase.getInstance();

        myRef = database.getReference("usuarios");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mReciver = new BlueReciver();
        this.registerReceiver(mReciver, filter);

        Notification notification = new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.drawable.iconocoche)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .build();

        startForeground(1337, notification);
        Toast.makeText( getBaseContext(),"Estoy vivo",Toast.LENGTH_LONG).show();

        return START_REDELIVER_INTENT;
    }


}