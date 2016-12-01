package com.volodia.twittertesttask;

import com.volodia.twittertesttask.sync.SyncService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ModuleApp.class})
public interface ComponentApp {

    void inject(SyncService syncService);

    ComponentTimeline plus(ModuleTimeline moduleTimeline);
}