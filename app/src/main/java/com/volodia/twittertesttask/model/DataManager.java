package com.volodia.twittertesttask.model;

import android.util.Log;

import com.google.gson.Gson;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.*;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.volodia.twittertesttask.AppTwitteer;
import com.volodia.twittertesttask.utils.ActionCallback;
import com.volodia.twittertesttask.utils.Connectivity;
import com.volodia.twittertesttask.utils.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Volodia on 24.11.2016.
 */

public class DataManager {

    DBHelper dbHelper;
    SimpleDateFormat twitterDateFormat;
    Gson gson;

    public DataManager(DBHelper dbHelper, SimpleDateFormat twitterDateFormat, Gson gson) {
        this.dbHelper = dbHelper;
        this.twitterDateFormat = twitterDateFormat;
        this.gson = gson;
    }


    public void getNewTweets(final ActionCallback<List<Tweet>> callback, Tweet lastTweet) {
        if (Connectivity.isConnected(AppTwitteer.getInstance())) {
            getNewTweetsFromNetwork(callback, lastTweet);
        } else {
            dbHelper.getTweetsFromRealmBG(callback, lastTweet);
        }
    }


    public List<Tweet> getNewTweetsFromNetworkAndSave(final Tweet lastTweet) throws IOException {
        Response<List<Tweet>> response = Twitter.getApiClient().getStatusesService().
                homeTimeline(50, lastTweet == null ? null : lastTweet.id, null, null, null, null, null).execute();
        if (Utils.notEmpty(response.body())) {
            dbHelper.saveTweetsBG(response.body(), false);
        }
        return response.body();
    }


    public void getNewTweetsFromNetwork(final ActionCallback<List<Tweet>> callback, final Tweet lastTweet) {
        StatusesService statusesService = Twitter.getApiClient().getStatusesService();

        final Call<List<Tweet>> tweets = statusesService.homeTimeline(50, lastTweet == null ? null : lastTweet.id, null, null, null, null, null);
        tweets.enqueue(new com.twitter.sdk.android.core.Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                callback.success(result.data);
                if (result.data.size() == 0) return;
                boolean clearDBTweets = lastTweet == null;
                saveToRealm(result.data, clearDBTweets);
            }

            public void failure(TwitterException exception) {
                callback.failure(exception);
            }
        });
    }

    private void saveToRealm(final List<Tweet> data, boolean clearDBTweets) {
        if (!Utils.notEmpty(data)) {
            return;
        }
        dbHelper.saveTweetsUI(data, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
            }
        }, clearDBTweets);
    }

    public void postTweet(String status, ActionCallback<Tweet> actionCallback) {
        if (Connectivity.isConnected(AppTwitteer.getInstance())) { //todo strategy
            postTweetNet(status, actionCallback);
        } else {
            postTweetDB(status, actionCallback);
        }

    }

    public void postTweetDB(String status, ActionCallback<Tweet> actionCallback) {
        dbHelper.addTweetUI(new LocalTweet(status, twitterDateFormat.format(System.currentTimeMillis()), System.currentTimeMillis()), false, actionCallback);
    }


    public void postTweetNet(String status, final ActionCallback<Tweet> actionCallback) {
        TwitterApiClient twitterApiClient = Twitter.getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> tweet = statusesService.update(status, null, null, null, null, null, null, null, null);
        tweet.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Tweet tweet = result.data;
                actionCallback.success(tweet);
                dbHelper.addTweetUI(tweet, true, null);
            }

            public void failure(TwitterException exception) {
                actionCallback.failure(exception);
            }
        });
    }

    public void sync(Realm realm) throws IOException {
        int k = 0;
        TwitterApiClient twitterApiClient = Twitter.getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        List<TweetRealm> tweetsRealm = dbHelper.getUnsyncedTweets(realm);

        Tweet lastTweet = dbHelper.getLastSyncedTweet(realm);

        getNewTweetsFromNetworkAndSave(lastTweet);
        Response<Tweet> result;
        for (TweetRealm tweetItem : tweetsRealm) {
            Tweet tweet = gson.fromJson(tweetItem.tweet, Tweet.class);
            final long createdAt = tweetItem.createdAt;
            Call<Tweet> createdTweet = statusesService.update(tweet.text, null, null, null, null, null, null, null, null);
            result = createdTweet.execute();

            if (!result.isSuccessful()) {
                String error = result.errorBody().string();
                if (error.contains("Status is a duplicate")) {
                    dbHelper.deleteDuplicateTweet(createdAt, realm);
                }
                continue;
            }
            final Tweet resultTweet = result.body();
            dbHelper.replaceLocalTweet(resultTweet, createdAt, realm, k++);
        }
    }
}
