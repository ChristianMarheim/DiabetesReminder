package com.example.diabetesreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Date;

import static java.lang.Integer.getInteger;

public class MainActivity extends AppCompatActivity {

    private final String longtimestampsproyte = "longtimestampsproyte";
    private final String longtimestampsensor = "longtimestampsensor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        SharedPreferences sensor = MainActivity.this.getSharedPreferences(longtimestampsensor, Context.MODE_PRIVATE);
        SharedPreferences sproyte = MainActivity.this.getSharedPreferences(longtimestampsproyte, Context.MODE_PRIVATE);

        if(sensor.getLong( longtimestampsensor, 0) != 0){
            long timestamp = sensor.getLong( longtimestampsensor, 0);
            offsetSensor = new Date().getTime() - timestamp;
            if(offsetSproyte < fourDays)
                displayNotificationSproyte(null);
        }

        if(sensor.getLong( longtimestampsproyte, 0) != 0){
            long timestamp= sensor.getLong( longtimestampsproyte, 0);
            offsetSensor = new Date().getTime() - timestamp;
            if(offsetSensor < fourDays)
                displayNotificationSensor(null);
        }
    }

    private void save(String key, long value) {
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNotificationChannel() {
        init = true;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    String CHANNEL_ID = "note";

    int ID = 001;
    final long threeDays = 3 * 1000 * 3600 * 24;
    final long fourDays = 4 * 1000 * 3600 * 24;

    long offsetSproyte = 0;
    long offsetSensor = 0;

boolean init = false;

    public void displayNotificationSproyte(View view) {

        save(longtimestampsproyte, new Date().getTime());

        // SLEEP 5 SECONDS HERE ...
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sproyte i morgen");
            }
        }, threeDays - offsetSproyte);

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sproyte i dag");
            }
        }, fourDays - offsetSproyte);

    }

    public void displayNotificationSensor(View view) {

        save(longtimestampsensor, new Date().getTime());

        // SLEEP 5 SECONDS HERE ...
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sensor i morgen");
            }
        }, threeDays - offsetSensor);

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sensor i dag");
            }
        }, fourDays - offsetSensor);

    }

    private void sendNote(String text) {
        if (!init)
            createNotificationChannel();

        if(text.contains("Sproyte"))
            offsetSproyte = 0;

        if(text.contains("Sensor"))
            offsetSensor = 0;


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon_notification);
        builder.setContentTitle(text);
        builder.setContentText(text);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(ID, builder.build());
    }


}
