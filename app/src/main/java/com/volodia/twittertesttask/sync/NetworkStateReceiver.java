package com.volodia.twittertesttask.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.volodia.twittertesttask.BuildConfig;
import com.volodia.twittertesttask.ActivityTimeline;
import com.volodia.twittertesttask.utils.Connectivity;


public class NetworkStateReceiver extends BroadcastReceiver {

    ActivityTimeline activityTimeline;

    final IntentFilter intentFilter = new IntentFilter();

    {
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        setDebugUnregister(true);
    }

    private NetworkState current;

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkState state = getNetworkState(context);
        if (current != state && state != null) {
            current = state;
            if (state == NetworkState.CONNECTED && activityTimeline != null) {
                activityTimeline.startSyncTweets();
            }
        }
    }

    public void register(ActivityTimeline activity) {
        this.activityTimeline = activity;
        activity.registerReceiver(this, intentFilter);
    }

    public void unregister(Context context) {
        activityTimeline = null;
        try {
            context.unregisterReceiver(this);
        } catch (Exception ex) {
            if (BuildConfig.DEBUG) Log.v("NetworkStateReceiver", "unregister", ex);
        }

    }

    public static NetworkState getNetworkState(Context context) {
        if (context == null) return null;
        if (Connectivity.isConnected(context)) {
            if (Connectivity.isConnectedFast(context)) {
                return NetworkState.CONNECTED;
            } else {
                return NetworkState.CONNECTED;
            }
        } else {
            return NetworkState.NO_CONNECTION;
        }
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }


}
