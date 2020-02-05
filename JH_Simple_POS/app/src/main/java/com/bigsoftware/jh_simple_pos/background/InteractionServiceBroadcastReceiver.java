package com.bigsoftware.jh_simple_pos.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by shanesepac on 4/27/19.
 */

public class InteractionServiceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "IS BR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Service stopped, attempting to restart.");
        context.startService(new Intent(context, InteractionService.class));;
    }
}
