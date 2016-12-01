package com.volodia.twittertesttask.sync;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.volodia.twittertesttask.AppTwitteer;
import com.volodia.twittertesttask.model.DataManager;
import com.volodia.twittertesttask.utils.Connectivity;

import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.realm.Realm;

public class SyncService extends Service {

    @Inject
    Handler handler;
    @Inject
    DataManager dataManager;

    SyncListener syncListener;

    boolean syncInProgress;

    public SyncService() {
        super();
        AppTwitteer.getInstance().getComponentApp().inject(this);
    }

    public void setListener(SyncListener listener) {
        this.syncListener = listener;
    }

    public boolean isSyncInProgress() {
        return syncInProgress;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void syncBG() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sync();
            }
        }).start();
    }


    protected void sync() {
        if (Twitter.getSessionManager().getActiveSession() == null) return;
        postStartSync();
        if (!Connectivity.isConnected(getApplicationContext())) {
            postFinishSync();
            return;
        }

        Realm realm = Realm.getDefaultInstance();

        try {
            dataManager.sync(realm);
        } catch (IOException e) {
        } finally {
            realm.close();
            postFinishSync();
        }
    }

    private void postStartSync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                syncInProgress = true;
                if (syncListener != null ){
                    syncListener.onStartSync();
                }
            }
        });
    }

    private void postFinishSync() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                syncInProgress = false;
                if (syncListener != null ){
                    syncListener.onFinishSync();
                }
            }
        });
    }

}
