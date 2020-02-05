package com.bigsoftware.jh_simple_pos.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bigsoftware.jh_simple_pos.R;
import com.bigsoftware.jh_simple_pos.views.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shanesepac on 4/27/19.
 */

public class InteractionService extends IntentService {

    private static final String TAG = "Interaction Service";
    public int counter=0;

    private static boolean monitoring = true;
    private static final long alarmMS = 3600000;
    private static final long snoozeMS = 1800000;
    private static long lastActive;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    public InteractionService() {
        super("Interaction Service");
        Log.d(TAG, "InteractionService: Service started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent(this, InteractionServiceBroadcastReceiver.class);

        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
            }
        };
    }
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    
    private static void setLastActive(){
        lastActive = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void wakeService(){
        setLastActive();
        monitoring = true;
        Log.d(TAG, "wakeService: service awoken");
    }

    public static void sleepService(){
        monitoring = false;
        Log.d(TAG, "sleepService: service sleeping");
    }

    private void sendNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID))
                .setSmallIcon(R.drawable.chips)
                .setContentTitle("Are you tracking purchases?")
                .setContentText("We detected no transactions in a while.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("We'd greatly appreciate you continuing to track purchases. Please track all store purchases!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define (set to 1000)
        notificationManager.notify(1000, builder.build());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            while (true) {
                if (monitoring && System.currentTimeMillis() > (alarmMS + lastActive)) { //send notification
                    sendNotification();
                }
                Thread.sleep(snoozeMS);
            }
        }

        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
