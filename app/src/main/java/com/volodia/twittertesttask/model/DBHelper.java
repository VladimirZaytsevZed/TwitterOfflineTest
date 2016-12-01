package com.volodia.twittertesttask.model;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.twitter.sdk.android.core.models.Tweet;
import com.volodia.twittertesttask.utils.ActionCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.volodia.twittertesttask.model.TweetRealm.FIELD_CREATED_AT;
import static com.volodia.twittertesttask.model.TweetRealm.FIELD_SYNCED;

/**
 * Created by Volodia on 23.11.2016.
 */

public class DBHelper {

    Realm realmUI;
    Gson gson;
    Handler uiHandler;
    final SimpleDateFormat twitterDateFormat;

    public DBHelper(Realm realmUI, Gson gson, Handler uiHandler, SimpleDateFormat twitterDateFormat) {
        this.realmUI = realmUI;
        this.gson = gson;
        this.uiHandler = uiHandler;
        this.twitterDateFormat = twitterDateFormat;
    }

    public void saveTweetsBG(final List<Tweet> data, final boolean clearDBTweets) {
        final Realm bgRealm = Realm.getDefaultInstance();
        try {
            bgRealm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    if (clearDBTweets) {
                        bgRealm.where(TweetRealm.class).findAll().deleteAllFromRealm();
                    }
                    for (Tweet tweet : data) {
                        String jsonTweet = gson.toJson(tweet);
                        TweetRealm tweetRealm = null;
                        try {
                            tweetRealm = new TweetRealm(jsonTweet, true, twitterDateFormat.parse(tweet.createdAt).getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        bgRealm.copyToRealm(tweetRealm);
                    }
                }
            });
        } finally {
            bgRealm.close();;
        }

    }

    public void saveTweetsUI(final List<Tweet> data, final Realm.Transaction.OnSuccess onSuccessListener, final boolean clearDBTweets) {
        realmUI.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                if (clearDBTweets) {
                    bgRealm.where(TweetRealm.class).findAll().deleteAllFromRealm();
                }
                for (Tweet tweet : data) {
                    String jsonTweet = gson.toJson(tweet);

                    TweetRealm tweetRealm = null;
                    try {
                        tweetRealm = new TweetRealm(jsonTweet, true, twitterDateFormat.parse(tweet.createdAt).getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    bgRealm.copyToRealm(tweetRealm);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (onSuccessListener != null) {
                    onSuccessListener.onSuccess();
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {

            }
        });
    }

    public void getTweetsFromRealmBG(final ActionCallback<List<Tweet>> callback, final Tweet since) {

        new Thread(new Runnable() { //todo to method
            @Override
            public void run() {
                Realm realmBg = Realm.getDefaultInstance();
                try {
                    RealmQuery<TweetRealm> tweetQuery = realmBg.where(TweetRealm.class);
                    if (since != null) try {
                        tweetQuery = tweetQuery.greaterThan(TweetRealm.FIELD_CREATED_AT, twitterDateFormat.parse(since.createdAt).getTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    RealmResults<TweetRealm> tweetsRealm = tweetQuery.findAll().sort(FIELD_CREATED_AT, Sort.DESCENDING);
                    final List<Tweet> tweets = new ArrayList<>();

                    for (TweetRealm tweetRealm : tweetsRealm) {
                        Tweet tweet = gson.fromJson(tweetRealm.tweet, Tweet.class);
                        tweets.add(tweet);
                    }

                    realmBg.close();
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(tweets);
                        }
                    });
                } finally {
                    realmBg.close();
                }
            }
        }).start();
    }

    public void addTweetUI(final Tweet tweet, final boolean synced, final ActionCallback<Tweet> actionCallback) {
        realmUI.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                String jsonTweet = gson.toJson(tweet);
                TweetRealm tweetRealm = null;
                try {
                    tweetRealm = new TweetRealm(jsonTweet, synced, synced ? twitterDateFormat.parse(tweet.createdAt).getTime() : tweet.id);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                bgRealm.copyToRealm(tweetRealm);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                if (actionCallback != null) {
                    actionCallback.success(tweet);
                }
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                if (actionCallback != null) {
                    actionCallback.failure(error);
                }
            }
        });

    }

    public RealmResults<TweetRealm> getUnsyncedTweets(Realm realm) {
        RealmResults<TweetRealm> tweetRealms = realm.where(TweetRealm.class).equalTo(FIELD_SYNCED, false).findAll();//.sort(FIELD_CREATED_AT); //todo async;
        return tweetRealms;
    }

    public void deleteDuplicateTweet(final long createdAt, Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                TweetRealm tweetRealm = realm.where(TweetRealm.class).equalTo(FIELD_CREATED_AT, createdAt).findFirst();
                tweetRealm.deleteFromRealm();
            }
        });
    }

    public void clearTweets() {
        realmUI.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(TweetRealm.class).findAll().deleteAllFromRealm();
            }
        });
    }


    public void replaceLocalTweet(final Tweet tweet, final long createdAt, Realm realm, final int k) {
        String logPattern = "HH:mm:ss";
        final SimpleDateFormat logPatternFormat = new SimpleDateFormat(logPattern);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String jsonTweet = gson.toJson(tweet);
                TweetRealm tweetRealm = realm.where(TweetRealm.class).equalTo(FIELD_CREATED_AT, createdAt).findFirst();
                tweetRealm.synced = true;
                tweetRealm.tweet = jsonTweet;
                try {
                    long newTimeParsed = twitterDateFormat.parse(tweet.createdAt).getTime() + k; // 'k' for right sorting
                    tweetRealm.createdAt = newTimeParsed;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public Tweet getLastSyncedTweet(Realm realmBg) {
        Number number = realmBg.where(TweetRealm.class).equalTo(FIELD_SYNCED, true).max(FIELD_CREATED_AT);
        if (number == null) return null;
        long createdAt = (long) number;
        TweetRealm tweetRealm = realmBg.where(TweetRealm.class).equalTo(FIELD_CREATED_AT, createdAt).findFirst();
        if (tweetRealm == null) return null;
        return gson.fromJson(tweetRealm.tweet, Tweet.class);
    }
}
