package com.volodia.twittertesttask;

import android.app.Application;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Volodia on 21.11.2016.
 */

public class AppTwitteer extends Application {
    private static AppTwitteer instance;

    public final static String CONSUMER_KEY = "CA71E0eLAFX8sZAupVcpmF8K1";
    public final static String CONSUMER_SECRET = "pYQr119XskqXENn8thlf4W3X1VwThfPqvyCWqUdJgDEPPhnM0c";

    private ComponentApp componentApp;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        TwitterAuthConfig authConfig = new TwitterAuthConfig(CONSUMER_KEY, CONSUMER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        initAppComponent();
    }

    private void initAppComponent() {
        componentApp = DaggerComponentApp.builder()
                .moduleApp(new ModuleApp(this))
                .build();
    }

    public static AppTwitteer getInstance() {
        return instance;
    }

    public ComponentApp getComponentApp() {
        return componentApp;
    }

}
