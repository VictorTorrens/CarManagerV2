package com.torrens.victor.carmanagerv2;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
//import android.support.annotation.NonNull;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.NavigationView;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.luseen.simplepermission.permissions.MultiplePermissionCallback;
import com.luseen.simplepermission.permissions.Permission;
import com.luseen.simplepermission.permissions.PermissionActivity;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
//TODO: interfeis
public class MainActivity extends PermissionActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirabaseSingleton singleton;
    private final int RC_SIGN_IN = 123;
    private List<AuthUI.IdpConfig> providers;
    private LocationManager locationManager;
    private ImageView imageLateral;
    private FloatingActionButton fab;
    private Location location;
    private double lati;
    private double longi;
    private PositionReciver receiver;
    private LatLng actual;
    private boolean gpsOn = false;
    private Toolbar toolbar;
    private AdView mAdView;
    private SetPositionReciver receiver2;

    public class PositionReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            double[] d = intent.getExtras().getDoubleArray("position");
            if (d[0] == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actual, 15));

            } else {
                actual = new LatLng(d[0], d[1]);
                mMap.addMarker(new MarkerOptions().position(actual).title("Parking"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actual, 15));
            }
            fab.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Permission[] permissions = {Permission.FINE_LOCATION, Permission.COARSE_LOCATION};
        requestPermissions(permissions, new MultiplePermissionCallback() {

            @Override
            public void onPermissionGranted(boolean allPermissionsGranted, List<Permission> grantedPermissions) {
                Log.wtf("permisos", String.valueOf(allPermissionsGranted));

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MainActivity.this);

            }

            @Override
            public void onPermissionDenied(List<Permission> deniedPermissions, List<Permission> foreverDeniedPermissions) {

            }
        });

        IntentFilter filter = new IntentFilter("I_HAVE_THE_POSITION");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new PositionReciver();
        registerReceiver(receiver, filter);

        IntentFilter filter2 = new IntentFilter("SET_POSITION");
        filter2.addCategory(Intent.CATEGORY_DEFAULT);
        receiver2 = new SetPositionReciver();
        registerReceiver(receiver2, filter2);

        mAuth = FirebaseAuth.getInstance();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
    }

    private void mirargps() {
        String provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.wtf("gpss", provider);
        if (!provider.equals("")) {
            //GPS Enabled
            gpsOn = true;

        } else {
            gpsOn = false;
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this);
            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            //TODO: arreglar strings traduccion
            final String message = String.valueOf(R.string.msgGPS);

            builder.setMessage(message)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    startActivity(new Intent(action));
                                    d.dismiss();
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mirargps();
        if (gpsOn) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(MainActivity.this);

        }
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        TextView nameLateral = header.findViewById(R.id.nameLateral);
        TextView emailLateral = header.findViewById(R.id.emailLateral);
        imageLateral = header.findViewById(R.id.imageLateral);
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        mAdView = (AdView) findViewById(R.id.adView);


        //TODO: debbug de esto
        //TODO: tutorial????
        try {
            singleton.getDeviceName();

            nameLateral.setText(mAuth.getCurrentUser().getDisplayName());
            //
            emailLateral.setText(mAuth.getCurrentUser().getEmail());

            DownloadImageTask downloadImageTask = new DownloadImageTask();

            downloadImageTask.execute(String.valueOf(mAuth.getCurrentUser().getPhotoUrl()));
        } catch (RuntimeException re) {
            Log.wtf("log", "a petao");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // fab.setVisibility(View.VISIBLE);
        singleton = FirabaseSingleton.sharedInstance(this);

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            lati = location.getLatitude();
            longi = location.getLongitude();
            actual = new LatLng(lati, longi);
            Log.wtf("OLAAAAAAAAAAA", lati + "/" + longi);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }
        if (mAuth.getCurrentUser() == null) {
            signIn();
        } else {
            singleton.getLastLocation();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPoint();

            }
        });

        //TODO: publisidad rial
        AdRequest adRequest = new AdRequest.Builder()
                .setLocation(location)
                .build();
        mAdView.loadAd(adRequest);
    }

    private void setPoint() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        lati = location.getLatitude();
        longi = location.getLongitude();
        LatLng current = new LatLng(lati, longi);
        Log.wtf("gps", current.toString());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(current).title("Parking"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

        singleton.setLastLocation(current);
        Toast.makeText(MainActivity.this, R.string.savedLoacation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        unregisterReceiver(receiver2);

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.my_car) {
            Intent intent = new Intent(this, Bluetooh.class);
            startActivity(intent);

        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, Historial.class);
            startActivity(intent);
        } else if (id == R.id.acerca) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this);
            final String message = "Parking Manager\n" +
                    R.string.developed+" Victor Torrens";

            builder.setMessage(message)
                    .setTitle(R.string.acerca_de)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.dismiss();
                                }
                            });
            builder.create().show();
        } else if (id == R.id.nav_slideshow) {
            mAuth.signOut();
            AuthUI.getInstance()
                    .signOut(MainActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
            mMap.clear();
            signIn();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signIn() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setLogo(R.drawable.car)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(), RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                singleton = new FirabaseSingleton(this);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signIn();
        }
    }
//TODO: implementar glide
    class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            Bitmap bmp;
            try {
                URL url = new URL(urls[0]);
                bmp = BitmapFactory.decodeStream(
                        url.openConnection().getInputStream());
            } catch (Exception
                    ex) {
                ex.printStackTrace();
                return null;
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null)
                imageLateral.setImageBitmap(result);
            else
                imageLateral.setImageResource(R.drawable.error);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    private class SetPositionReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.wtf("reciver", "ola");
            double[] array= intent.getDoubleArrayExtra("pos");

            LatLng current= new LatLng(array[0], array[1]);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(current).title("Parking"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
        }
    }
}
