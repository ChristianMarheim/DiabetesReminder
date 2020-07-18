package com.example.diabetesreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final String KEY_LONG_TIMESTAMP_SPROYTE = "longtimestampsproyte";
    private final String KEY_LONG_TIMESTAMP_SENSOR = "longtimestampsensor";

    String CHANNEL_ID = "note";
    String CHANNEL_ID_DEBUG = "noteDEBUG";



    int ID = 001;
    final long timePassedBeforeNotificationWarning = 1 * 1000 * 20; //final long threeDays = 3 * 1000 * 3600 * 24;
    final long timePassedBeforeNotificationDeadline = 1 * 1000 * 30;    //final long fourDays = 4 * 1000 * 3600 * 24;

    long timePassedSproyte = 0;
    long timePassedSensor = 0;
    long longTimestampSensor;
    long longTimestampSproyte;

    SharedPreferences sensorPreferences;
    SharedPreferences sproytePreferences;

    boolean init = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorPreferences = MainActivity.this.getSharedPreferences(KEY_LONG_TIMESTAMP_SENSOR, Context.MODE_PRIVATE);
        sproytePreferences = MainActivity.this.getSharedPreferences(KEY_LONG_TIMESTAMP_SPROYTE, Context.MODE_PRIVATE);

        //doBusinessLogic();
    }

    private void doBusinessLogic() {
        updateDebugInfo("Before");

        if(sensorPreferences.getLong(KEY_LONG_TIMESTAMP_SENSOR, 0) != 0){
            timePassedSensor = getTimePassedSensor();

            if(timePassedSensor > timePassedBeforeNotificationWarning){
                save(KEY_LONG_TIMESTAMP_SENSOR, new Date().getTime());
                displayNotificationSensor(null);
            }
        }

        if(sproytePreferences.getLong(KEY_LONG_TIMESTAMP_SPROYTE, 0) != 0){
            timePassedSproyte = getSproyteTimePassed();

            if(timePassedSproyte > timePassedBeforeNotificationWarning){
                save(KEY_LONG_TIMESTAMP_SPROYTE, new Date().getTime());
                displayNotificationSproyte(null);
            }
        }
        updateDebugInfo("After");
    }

    private long getTimePassedSensor() {
        longTimestampSensor = sensorPreferences.getLong(KEY_LONG_TIMESTAMP_SENSOR, 0);

        return new Date().getTime() - longTimestampSensor;
    }

    private long getSproyteTimePassed() {
        longTimestampSproyte = sproytePreferences.getLong(KEY_LONG_TIMESTAMP_SPROYTE, 0);
        return new Date().getTime() - longTimestampSproyte;
    }

    public void refreshNotifications(View view) {
        doBusinessLogic();
    }

    private void updateDebugInfo(String caller) {
        TextView infoText = (TextView) findViewById(R.id.infoText);
        infoText.setText(getInfoText());
        sendNote(caller + " Current state:" + getInfoText(), true);

    }

    private void updateDebugInfo() {
        updateDebugInfo("");
    }

    private String getInfoText() {
        return "longtimestampsensor - " + longTimestampSensor + "\n" +
                "longtimestampsproyte - " + longTimestampSproyte + "\n" +
                "timeSinceSproyteAlarmActiviated - " + timePassedSproyte + "\n" +
                "timeSinceSensorAlarmActiviated - " + timePassedSensor + "\n" +
                "timePassedBeforeNotificationWarning - " + timePassedBeforeNotificationWarning + "\n" +
                "timePassedBeforeNotificationDeadline - " + timePassedBeforeNotificationDeadline;
    }

    private void save(String key, long value) {
        SharedPreferences sharedPref = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.commit();
        sendNote("Saved the following preferences: " + getInfoText(), true);
    }

    public void displayNotificationSproyte(View view) {
        // SLEEP 5 SECONDS HERE ...
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sproyte i morgen");
            }
        }, timePassedBeforeNotificationWarning - timePassedSproyte);

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sproyte i dag");
            }
        }, timePassedBeforeNotificationDeadline - timePassedSproyte);
    }

    public void displayNotificationSensor(View view) {
        // SLEEP 5 SECONDS HERE ...
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sensor i morgen");
            }
        }, timePassedBeforeNotificationWarning - timePassedSensor);

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sensor i dag");
            }
        }, timePassedBeforeNotificationDeadline - timePassedSensor);

    }

    private void sendNote(String text) {
        sendNote(text, false);
    }

    private void sendNote(String text, boolean debug) {
        if (!init)
            createNotificationChannel();

        if(text.contains("Sproyte"))
            timePassedSproyte = 0;

        if(text.contains("Sensor"))
            timePassedSensor = 0;

        NotificationCompat.Builder builder;
        if(debug){
            builder = new NotificationCompat.Builder(this, CHANNEL_ID_DEBUG);
        } else {
            builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        }
        builder.setSmallIcon(R.drawable.icon_notification);
        text = text + "\n" + getInfoText();
        builder.setContentTitle(text);
        builder.setContentText(text);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(ID, builder.build());
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

        init = true;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_DEBUG, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /* PLUMBING */

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
}
