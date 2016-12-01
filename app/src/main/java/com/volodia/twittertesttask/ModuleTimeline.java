package com.volodia.twittertesttask;

import com.volodia.twittertesttask.model.DBHelper;
import com.volodia.twittertesttask.model.DataManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Volodia on 30.11.2016.
 */

@Module
public class ModuleTimeline {
    ActivityTimeline activityTimeline;

    public ModuleTimeline(ActivityTimeline activity) {
        this.activityTimeline = activity;
    }

    @Provides
    @ActivityScope
    PresenterTimeline providePresenterTimeline(DataManager dataManager, DBHelper dbHelper){
        return new PresenterTimeline(activityTimeline, dataManager, dbHelper);
    }
}
