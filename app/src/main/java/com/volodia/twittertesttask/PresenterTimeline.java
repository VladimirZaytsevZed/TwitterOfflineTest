package com.volodia.twittertesttask;

import android.util.Log;

import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.volodia.twittertesttask.model.DBHelper;
import com.volodia.twittertesttask.model.DataManager;
import com.volodia.twittertesttask.utils.ActionCallback;

import java.util.List;

/**
 * Created by Volodia on 30.11.2016.
 */

public class PresenterTimeline {

    DataManager dataManager;
    DBHelper dbHelper;
    ActivityTimeline activityTimeline;


    public PresenterTimeline(ActivityTimeline activityTimeline, DataManager dataManager, DBHelper dbHelper) {
        this.dataManager = dataManager;
        this.dbHelper = dbHelper;
        this.activityTimeline = activityTimeline;
    }

    public void getNewTweets(Tweet lastTweed) {
        dataManager.getNewTweets(new ActionCallback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> tweets) {
                if (activityTimeline != null)
                    activityTimeline.addNewTweets(tweets);
            }

            @Override
            public void failure(Throwable exception) {
                if (activityTimeline != null)
                    activityTimeline.loadNewTweetsFailure(exception);
            }
        }, lastTweed);
    }


    public void postTweet(String status) {
        dataManager.postTweet(status, new ActionCallback<Tweet>() {
            @Override
            public void success(Tweet result) {
                if (activityTimeline != null)
                    activityTimeline.tweetPosted(result);
            }

            public void failure(TwitterException exception) {
                if (activityTimeline != null)
                    activityTimeline.tweetPostFailed(exception);
            }
        });

    }

    public void clearTweets() {
        dbHelper.clearTweets();
    }


    public void getAllTweetsFromRealmBG() {
        dbHelper.getTweetsFromRealmBG(new ActionCallback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> result) {
                if (activityTimeline != null)
                    activityTimeline.updateTweets(result);
            }
        }, null);
    }

    public void release() {
        activityTimeline = null;
    }

    public void connect(ActivityTimeline activityTimeline) {
        this.activityTimeline = activityTimeline;
    }
}
