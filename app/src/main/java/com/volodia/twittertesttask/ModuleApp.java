package com.volodia.twittertesttask;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.volodia.twittertesttask.model.DBHelper;
import com.volodia.twittertesttask.model.DataManager;

import java.text.SimpleDateFormat;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;


@Module
public class ModuleApp {
    private Application application;

    public ModuleApp(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }


    /*@Provides
    @Singleton
    public PrefsManager providePreferences() {
        return new PrefsManager(application);
    }*/

    @Provides
    @Singleton
    public SimpleDateFormat provideTwitterDateFormat() {
        String pattern = "EEE MMM d HH:mm:ss Z yyyy";
        return  new SimpleDateFormat(pattern);
    }


    @Provides
    @Singleton
    public Realm provideUIRealm() {
        Realm.init(application);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(realmConfiguration);
        return  Realm.getDefaultInstance();
    }

    @Provides
    @Singleton
    public Gson provideGson(){
        return new Gson();
    }

    @Provides
    @Singleton
    public Handler provideUIHandler(){
        return new Handler(Looper.getMainLooper());
    }


    @Provides
    @Singleton
    public DBHelper provideDbHelper(Realm realmUI, Gson gson, Handler uiHandler, SimpleDateFormat twitterDateFormat) {
        return  new DBHelper(realmUI, gson, uiHandler, twitterDateFormat);
    }

    @Provides
    @Singleton
    public DataManager provideTweetsDAO(DBHelper dbHelper, SimpleDateFormat twitterDateFormat, Gson gson) {
        return  new DataManager(dbHelper, twitterDateFormat, gson);
    }

}