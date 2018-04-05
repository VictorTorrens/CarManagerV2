package com.torrens.victor.carmanagerv2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Historial extends AppCompatActivity {

    private ArrayList<ListItem> s;
    private ListView list;
    private final String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private PSReciver receiver;

    public class PSReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                List<Address> addresses;
                Geocoder geocoder = new Geocoder(getApplication(), Locale.getDefault());
                addresses = geocoder.getFromLocation(intent.getExtras().getDouble("ps1"), intent.getExtras().getDouble("ps2"), 1);
                String address = addresses.get(0).getAddressLine(0);
                s.add(new ListItem(time(intent.getExtras().getLong("ms")), address));


            } catch (IOException e) {
                s.add(new ListItem(time(intent.getExtras().getLong("ms")), intent.getExtras().getDouble("ps1") + ", " + intent.getExtras().getDouble("ps2")));

            }

            list.setAdapter(new MyListAdapter(getApplicationContext(), s));
            list.setActivated(true);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        FirabaseSingleton singleton = FirabaseSingleton.sharedInstance(this);
        singleton.crearHistorial();
        IntentFilter filter = new IntentFilter("I_HAVE_THE_PS");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new PSReciver();
        registerReceiver(receiver, filter);

        list = findViewById(R.id.list);

        s = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private String time(long ms) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ms);

        int mYear = calendar.get(Calendar.YEAR);
        int mMonth = calendar.get(Calendar.MONTH);
        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR);
        int m = calendar.get(Calendar.MINUTE);

        return h+":"+m+" / "+mDay + " de " + meses[mMonth] + " de " + mYear;
    }
}
