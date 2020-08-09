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
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.SharedPreferences.*;

public class MainActivity extends AppCompatActivity {

    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIME = true;

    int ID1_NORMAL = 001;
    int ID2_DEBUG = 002;
    private final String APP_ID = "DIABETES_REMINDER_BETA_APP_ID";
    private final String SENSOR_TIMESTAMP_KEY = "SENSOR_TIMESTAMP";
    private final String SPROYTE_TIMESTAMP_KEY = "SPROYTE_TIMESTAMP";

    int numberOfDaysSelected = 3;

    String CHANNEL_ID = "NOTE_NOTIFICATION_CHANNEL";
    String CHANNEL_ID_DEBUG = "DEBUG_NOTIFICATION_CHANNEL";

    final long oneDay = 1 * 1000 * 3600 * 24;

//    final long timePassedBeforeNotificationWarning = 1 * 1000 * 3; //final long threeDays = 3 * 1000 * 3600 * 24;
//    final long timePassedBeforeNotificationDeadline = 1 * 1000 * 7;    //final long fourDays = 4 * 1000 * 3600 * 24;

    final long timePassedBeforeNotificationWarning = 1 * 1000 * 3; //final long threeDays = 3 * 1000 * 3600 * 24;
    final long timePassedBeforeNotificationDeadline = 1 * 1000 * 7;    //final long fourDays = 4 * 1000 * 3600 * 24;

    long timePassedSproyte = 0;
    long timePassedSensor = 0;
    long longTimestampSensor;
    long longTimestampSproyte;

    SharedPreferences preferences;

    boolean init = false;

    /*
    TODO/Commit message - 18.07.2020:
    It still works but there is something fucked up with doBusinessLogic().
    It seems it was written for initial loading and not loading for old settings.
    Should rewrite it to something simple, or just support all the cases.
    The 4 permutations of

    what-alarm-is-for * what-day-is-the alarm

    needs to be represented everywhere for it to correctly load both features after restart.
    This means when it is restarted 3 days later after alarm was initially set,
    it should not set it 4 days forward again, but only set it 1 day forward. <- This is basically acceptance criteria
    Maybe I should play around with some unit tests for this? A good exercise in refactoring!
    Refactoring is probably gonna go to hell because I don't have any tests now so probably break
    it fast to try out and just revert.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = MainActivity.this.getSharedPreferences(APP_ID, Context.MODE_PRIVATE);

        RadioGroup radioGroup = findViewById(R.id.radioListDays);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if(checkedId == R.id.radioButton1) {
                    numberOfDaysSelected = 1;
                } else  if(checkedId == R.id.radioButton2) {
                    numberOfDaysSelected = 2;
                } else  if(checkedId == R.id.radioButton3) {
                    numberOfDaysSelected = 3;
                } else  if(checkedId == R.id.radioButton4) {
                    numberOfDaysSelected = 4;
                } else if(checkedId == R.id.radioButton5) {
                    numberOfDaysSelected = 5;
                }
            }
        });


        doBusinessLogic();
    }

    private void doBusinessLogic() {
        updateDebugInfo("Before");

        if(preferences.getLong(SENSOR_TIMESTAMP_KEY, 0) != 0){
            timePassedSensor = getTimePassedSensor();

            if(timePassedSensor > timePassedBeforeNotificationWarning){
                save(SENSOR_TIMESTAMP_KEY, new Date().getTime()); // FIXME Why the fuck save now? Put in a check if we have data instead?
                onButtonPressedNotificationSensor(null);
            }
        }

        if(preferences.getLong(SPROYTE_TIMESTAMP_KEY, 0) != 0){
            timePassedSproyte = getSproyteTimePassed();

            if(timePassedSproyte > timePassedBeforeNotificationWarning){
                save(SPROYTE_TIMESTAMP_KEY, new Date().getTime());  // FIXME Why the fuck save now?
                onButtonPressedNotificationSproyte(null);
            }
        }
        updateDebugInfo("After");
    }

    private long getTimePassedSensor() {
        longTimestampSensor = preferences.getLong(SENSOR_TIMESTAMP_KEY, 0);
        return new Date().getTime() - longTimestampSensor;
    }

    private long getSproyteTimePassed() {
        longTimestampSproyte = preferences.getLong(SENSOR_TIMESTAMP_KEY, 0);
        return new Date().getTime() - longTimestampSproyte;
    }

    public void refreshNotifications(View view) {
        doBusinessLogic();
        // Since business logic saves as part of it because of a bug, we have to clear stuff here.
        save(SENSOR_TIMESTAMP_KEY, Long.MAX_VALUE);
        save(SPROYTE_TIMESTAMP_KEY, Long.MAX_VALUE);
    }

    private void updateDebugInfo(String caller) {
        setInfoText(getInfoText());
        if(DEBUG){
            sendNote(caller + " Current state:" + getInfoText(), true);
        }
    }

    private void setInfoText(String text) {
        TextView infoText = findViewById(R.id.infoText);
        infoText.setText(text);
    }

    private String getInfoText() {
        if(!DEBUG)
            return "";
        return "\n\nDEBUG:\nSENSOR_TIMESTAMP_KEY=" + preferences.getLong(SENSOR_TIMESTAMP_KEY, 0) + "\n" +
               "SPROYTE_TIMESTAMP_KEY=" + preferences.getLong(SPROYTE_TIMESTAMP_KEY, 0) + "\n" +
               "new Date()" + new SimpleDateFormat().format(new Date().getTime()) + "\n" +
               "timePassedSproyte=" + timePassedSproyte + "\n" +
               "timePassedSensor=" + timePassedSensor + "\n" +
                "longTimestampSensor=" + longTimestampSensor + "\n" +
                "longTimestampSproyte=" + longTimestampSproyte + "\n" +
                "timePassedBeforeNotificationWarning=" + timePassedBeforeNotificationWarning + "\n" +
                "timePassedBeforeNotificationDeadline=" + timePassedBeforeNotificationDeadline + "\n";
    }

    private void save(String key, long value) {
        Editor editor = preferences.edit();
        editor.putLong(key, value);
        editor.commit();


        long timestampSensor = preferences.getLong(SENSOR_TIMESTAMP_KEY, 0);
        long timestampSproyte = preferences.getLong(SPROYTE_TIMESTAMP_KEY, 0);
        try {
            setInfoText("DEBUG:\n" +
                    "Current time: " + format(new Date()) + "\n" +
                    "Alarm for neste sensor: " + format(new Date(timestampSensor + getDelay(false, false))) + "\n" +
                    "Alarm for neste sprøyte: " + format(new Date(timestampSproyte + getDelay(false, false))) + "\n");
        } catch (Exception e){
            int a = 0;
            int b = a;
        }

        if(DEBUG){
            sendNote("Saved the following preferences: " + getInfoText(), true);
        }
    }

    private String format(Date date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy MMMM EEE d HH:mm:ss");
        if(date.getTime() == Long.MAX_VALUE){
            return "              ";
        }
        return format.format(date);
    }

    public void onButtonPressedNotificationSensor(View view) {
        // SLEEP 5 SECONDS HERE ...
        Handler handler = new Handler();
        save(SENSOR_TIMESTAMP_KEY, new Date().getTime());

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sensor i morgen");
            }
        }, getDelay(false, true));

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sensor i dag");
                save(SENSOR_TIMESTAMP_KEY, Long.MAX_VALUE);
            }
        }, getDelay(false, false));

    }

    public void onButtonPressedNotificationSproyte(View view) {
        // SLEEP 5 SECONDS HERE ...
        Handler handler = new Handler();
        save(SPROYTE_TIMESTAMP_KEY, new Date().getTime());

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sproyte i morgen");
            }
        }, getDelay(true, true));

        handler.postDelayed(new Runnable() {
            public void run() {
                sendNote("Note Sproyte i dag");
                save(SPROYTE_TIMESTAMP_KEY, Long.MAX_VALUE);
            }
        }, getDelay(true, false));
    }

    private long getDelay(boolean isSproyte, boolean isWarning) {
        long days = numberOfDaysSelected;

        long timePassed = 0;
        if(isSproyte){
            timePassed = timePassedSproyte;
        } else {
            timePassed = timePassedSensor;
        }
        long result = 0;
        if(isWarning){
            long warningDays = days - 1;
            result =  (warningDays * getTimeFactor()) - timePassed;
        } else {
            result = (days * getTimeFactor()) - timePassed;
        }
        return result;
    }

    private long getTimeFactor() {
        if(DEBUG_TIME)
            //return 1000 * 3;
            return 1000 * 3600;
        return oneDay;
    }


    private void sendNote(String text) {
        sendNote(text, false);
    }

    private void sendNote(String text, boolean debugNotificationChannel_NOT_DEBUG_MODE_MIND_YOU) {
        if (!init)
            createNotificationChannel();

        //if(text.contains("Sproyte"))
            //timePassedSproyte = 0; //FIXME Må fikse dagen-foer-warning vs bytte-idag-warning

        //if(text.contains("Sensor"))
            //timePassedSensor = 0;  //FIXME Må fikse dagen-foer-warning vs bytte-idag-warning

        NotificationCompat.Builder builder;
        if(debugNotificationChannel_NOT_DEBUG_MODE_MIND_YOU){
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

        TextView infoText = findViewById(R.id.notificationLog);
        infoText.setText(text + "\n" + infoText.getText());

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if(debugNotificationChannel_NOT_DEBUG_MODE_MIND_YOU){
            notificationManagerCompat.notify(ID2_DEBUG, builder.build());
        } else {
            notificationManagerCompat.notify(ID1_NORMAL, builder.build());
        }
    }


    private void createNotificationChannel() {
        init = true;

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
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
            int importance = NotificationManager.IMPORTANCE_LOW;
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
