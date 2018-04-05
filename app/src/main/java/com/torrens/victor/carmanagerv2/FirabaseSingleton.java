package com.torrens.victor.carmanagerv2;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

class FirabaseSingleton {

    private static FirabaseSingleton singleton;
    private final FirebaseAuth mAuth;
    private final DatabaseReference myRef;
    private String usuario;
    private String selectedDeviceName;
    private String selectedDevice;
    private final MainActivity mainActivity;

    public String getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public FirebaseAuth getmAuth() {
        return mAuth;
    }

    public DatabaseReference getMyRef() {
        return myRef;
    }

    FirabaseSingleton(Activity activity) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("usuarios");
        mAuth = FirebaseAuth.getInstance();
        mainActivity = (MainActivity) activity;


        if (mAuth.getCurrentUser() != null)
            usuario = mAuth.getCurrentUser().getUid();

    }

    static FirabaseSingleton sharedInstance() {
        return singleton;
    }

    static FirabaseSingleton sharedInstance(Activity activity) {
        if (singleton == null) {
            singleton = new FirabaseSingleton(activity);
        }
        return singleton;
    }

    void setLastLocation(LatLng latLng) {
        usuario = mAuth.getCurrentUser().getUid();

        long currentTime = System.currentTimeMillis();
        myRef.child(usuario).child("location").child(String.valueOf(currentTime)).setValue(latLng);
    }

    void setMACAdress(String s, String n) {
        selectedDevice = s;
        selectedDeviceName = n;

        usuario = mAuth.getCurrentUser().getUid();
        myRef.child(usuario).child("BlueDevice").setValue(s);
        myRef.child(usuario).child("BlueDeviceName").setValue(n);

    }

    void getDeviceName() {
        usuario = mAuth.getCurrentUser().getUid();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(usuario).hasChild("BlueDeviceName")) {
                    selectedDeviceName = dataSnapshot.child(usuario).child("BlueDeviceName").getValue(String.class);
                    selectedDevice = dataSnapshot.child(usuario).child("BlueDevice").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void crearHistorial() {
        final ArrayList<Position> positions = new ArrayList<>();
        myRef.child(usuario).child("location").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Position ps = new Position(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class),
                        Long.parseLong(dataSnapshot.getKey()));

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("I_HAVE_THE_PS");
                broadcastIntent.putExtra("ps1", dataSnapshot.child("latitude").getValue(Double.class));
                broadcastIntent.putExtra("ps2", dataSnapshot.child("longitude").getValue(Double.class));
                broadcastIntent.putExtra("ms", Long.parseLong(dataSnapshot.getKey()));
                mainActivity.sendBroadcast(broadcastIntent);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    void getLastLocation() {
        final double[] datos = new double[2];
        usuario = mAuth.getCurrentUser().getUid();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(usuario).hasChild("location")) {
                    Query lastQuery = myRef.child(usuario).child("location").orderByKey().limitToLast(1);
                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            datos[0] = dataSnapshot.getChildren().iterator().next().child("latitude").getValue(Double.class);
                            datos[1] = dataSnapshot.getChildren().iterator().next().child("longitude").getValue(Double.class);

                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction("I_HAVE_THE_POSITION");
                            broadcastIntent.putExtra("position", datos);
                            mainActivity.sendBroadcast(broadcastIntent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    datos[0] = 0;
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("I_HAVE_THE_POSITION");
                    broadcastIntent.putExtra("position", datos);
                    mainActivity.sendBroadcast(broadcastIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public String getDeviceAdress() {
        return selectedDevice;
    }
}