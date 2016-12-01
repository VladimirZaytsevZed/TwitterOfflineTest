package com.volodia.twittertesttask.sync;

import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

/**
 * Created by Volodia on 25.11.2016.
 */
public interface SyncListener {

    void onStartSync();
    void onFinishSync();
}
