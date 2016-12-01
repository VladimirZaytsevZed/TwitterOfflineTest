package com.volodia.twittertesttask.model;

import com.twitter.sdk.android.core.models.Tweet;

/**
 * Created by Volodia on 22.11.2016.
 */

public class LocalTweet extends Tweet {

    public LocalTweet(String text, String createdAt, long id) {
        super(null, createdAt, null, null, null, null, false, null, id, String.valueOf(id), null, 0, null, 0, null, null, null, false, null, 0, null, null, 0, false, null,
                null, text, null, false, null, false, null, null, null);
    }
}
