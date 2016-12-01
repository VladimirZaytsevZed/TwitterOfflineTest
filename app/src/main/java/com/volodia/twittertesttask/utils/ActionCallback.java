package com.volodia.twittertesttask.utils;

/**
 * Created by Volodia on 24.11.2016.
 */

public abstract class ActionCallback<T> {
    public void success(T result){
    };

    public void failure(Throwable exception){
    };
}
