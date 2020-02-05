package com.bigsoftware.jh_simple_pos.views;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bigsoftware.jh_simple_pos.R;
import com.bigsoftware.jh_simple_pos.background.InteractionService;
import com.bigsoftware.jh_simple_pos.data.PurchaseDBHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private PurchaseDBHelper pHelper;
    private static final int VIEW_CONFIG = 1000;
    private static final int VIEW_DATA = 1001;
    private static boolean passAccepted = false;
    private static MainActivity activity;
    private static long lastUndo;
    private static Intent serviceIntent;
    //private static InteractionService IS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainContainer, CheckoutFragment.newInstance("",""))
                .commit();

        setActionBarTitle();

        pHelper = new PurchaseDBHelper(this);
        activity =this;

        serviceIntent = new Intent(this, InteractionService.class);
        if(!isMyServiceRunning(InteractionService.class)){
            startService(serviceIntent);
        }
        createNotificationChannel();

    }

    @Override
    protected void onDestroy() {
        pHelper.close();
        stopService(serviceIntent);
        super.onDestroy();
    }

    private MediaPlayer _mediaPlayer;

    /***
     * Play's the default notification sound.
     */
    private void playFromResource(int resId)
    {
        if (_mediaPlayer != null)
        {
            // _mediaPlayer.stop();     freeze on some emulator snapshot
            // _mediaPlayer.release();
            _mediaPlayer.reset();     // reset stops and release on any state of the player
        }
        _mediaPlayer = MediaPlayer.create(this, resId);
        _mediaPlayer.start();
    }

    public void setActionBarTitle(){

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String title = sharedPref.getString(getString(R.string.storepreference), getString(R.string.defaultstore));

        getSupportActionBar().setTitle(title);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.config:
                startPasswordProtectDialog(VIEW_CONFIG);
                return true;
            case R.id.viewData:
                startPasswordProtectDialog(VIEW_DATA);
                return true;
            case R.id.home:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainContainer, CheckoutFragment.newInstance("",""))
                        .commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean undoDelaySatisfied(){
        if(lastUndo!=0){
            long currTime = System.currentTimeMillis();
            long delta = currTime-lastUndo;
            lastUndo = System.currentTimeMillis();
            return delta>5000;
        }
        lastUndo = System.currentTimeMillis();
        return true;
    }

    public void itemClicked(View view) {
        final String viewItemName = getResources()
                .getResourceName(view.getId()).replace(getPackageName() + ":id/", "");

        if(viewItemName.equals("undoBtn")){

            if(undoDelaySatisfied()) {
                if (pHelper.deleteLastEntry()) {
                    Toast.makeText(this, "Undo successful!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Could not undo last entry!", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(this, "Please wait a few seconds before undoing more data!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String itemName = getString(getResources().getIdentifier(viewItemName,"string", getPackageName()));
        Log.d(TAG, "itemClicked: " + itemName);

        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        String storeName = pref.getString(getString(R.string.storepreference), getString(R.string.defaultstore));


        if(!storeName.equals(getString(R.string.defaultstore))) {

            pHelper.insertData(itemName, storeName, new Runnable() {
                @Override
                public void run() {
                    final ImageView checkView = findViewById(
                            getResources()
                                    .getIdentifier("check" + viewItemName,"id", getPackageName()));

                    startSuccessAnimation(checkView);
                    playFromResource(R.raw.ding);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Could not submit data entry! Contact developer.", Toast.LENGTH_LONG).show();
                }
            });


        }
        else{ //TODO: store is not set; handle this
            Toast.makeText(activity, "You have not set which store data is being bound to. Add or select a store in config.",
                    Toast.LENGTH_LONG).show();
        }
    }

    protected String getCurrentStore(){
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        return prefs.getString(getString(R.string.storepreference), getString(R.string.defaultstore));
    }

    private void startSuccessAnimation(final ImageView checkView){
        checkView.clearAnimation();
        checkView.setAlpha(1.0f);
        checkView.setVisibility(View.VISIBLE);
        checkView.animate().alpha(0.0f).setDuration(1000).start();
    }


    private void startPasswordProtectDialog(int action){

        if(!passAccepted){
            openDataDialog(action);
        }
        else {
            switch (action) {
                case VIEW_CONFIG:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainContainer, ConfigFragment.newInstance("",""))
                            .addToBackStack("config")
                            .commit();
                    break;
                case VIEW_DATA:
                    DataViewFragment dvf = DataViewFragment.newInstance(getCurrentStore(),"");
                    dvf.bindPurchaseHelper(pHelper); //have to bind helper with method as opposed to constructor, because
                    //static fragment creation method requires empty constructor
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.mainContainer, dvf)
                            .addToBackStack("dataview")
                            .commit();
                    break;
            }
        }

    }

    private void openDataDialog(final int action){
        AlertDialog.Builder b = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View prompt = inflater.inflate(R.layout.passworddialog, null);

        b.setView(prompt);

        final EditText passET = prompt.findViewById(R.id.editTextDialogUserInput);

        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(passET.getText().toString().equals(getString(R.string.datapass))){
                    passAccepted = true;
                    startPasswordProtectDialog(action);
                    dialogInterface.dismiss();
                }
                else{
                    Toast.makeText(activity, "Incorrect password", Toast.LENGTH_LONG).show();
                }
            }
        });
        b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        b.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMyServiceRunning(InteractionService.class)){
            InteractionService.sleepService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isMyServiceRunning(InteractionService.class)){
            InteractionService.wakeService();
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
