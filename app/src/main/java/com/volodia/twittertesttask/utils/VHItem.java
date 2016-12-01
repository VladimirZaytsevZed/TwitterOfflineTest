package com.volodia.twittertesttask.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

public abstract class VHItem<T, A> extends RecyclerView.ViewHolder {

    A adapter;

    public A getAdapter() {
        return adapter;
    }

    public VHItem(View itemView, A adapter) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.adapter = adapter;
    }

    public abstract void applyData(T item);

}