package com.volodia.twittertesttask.model;


import io.realm.RealmObject;


public class TweetRealm extends RealmObject {
    public static final String FIELD_CREATED_AT = "createdAt";
    public static final String FIELD_SYNCED = "synced";

    public TweetRealm() {}

    public TweetRealm(String tweet, boolean synced, long createdAt) {
        this.tweet = tweet;
        this.synced = synced;
        this.createdAt = createdAt;
    }

    public String tweet;
    public boolean synced;
    public long createdAt;

}
