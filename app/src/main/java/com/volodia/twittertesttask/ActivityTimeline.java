package com.volodia.twittertesttask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetView;
import com.volodia.twittertesttask.model.LocalTweet;
import com.volodia.twittertesttask.sync.NetworkStateReceiver;
import com.volodia.twittertesttask.sync.SyncListener;
import com.volodia.twittertesttask.sync.SyncService;
import com.volodia.twittertesttask.utils.Utils;
import com.volodia.twittertesttask.utils.VHItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivityTimeline extends AppCompatActivity {

    @BindView(R.id.twitter_login_button)
    TwitterLoginButton loginButton;

    @BindView(R.id.bt_timeline)
    Button bt_timeline;
    @BindView(R.id.rv_twitts)
    RecyclerView rv_twitts;

    @BindView(R.id.rl_timeline)
    View rl_timeline;

    @BindView(R.id.fl_login)
    View fl_login;

    @BindView(R.id.pb_progress)
    View pb_progress;


    SyncService mService;
    boolean mBound = false;

    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    @Inject
    PresenterTimeline presenterTimeline;
    private boolean syncInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        AppTwitteer.getInstance().getComponentApp().plus(new ModuleTimeline(this)).inject(this);

        rv_twitts.setLayoutManager(new LinearLayoutManager(this));

        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                // TODO: Remove toast and use the TwitterSession's userID
                // with your app's user model
                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                Log.d("TwitterKit", "Login with Twitter success");
                Utils.fadeInView(rl_timeline);
                Utils.fadeOutView(fl_login);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });

        if (Twitter.getSessionManager().getActiveSession() == null) {
            fl_login.setVisibility(View.VISIBLE);
            rl_timeline.setVisibility(View.GONE);
        } else {
            fl_login.setVisibility(View.GONE);
            rl_timeline.setVisibility(View.VISIBLE);
        }
    }


    @OnClick(R.id.bt_tweet)
    public void onClickTweet(View view) {
        if (syncInProgress || !mBound) {
            Toast.makeText(this, R.string.sync_in_progress, Toast.LENGTH_SHORT).show();
            return;
        }
        showProgress(true);
        String pattern = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String status = "posted at: " + simpleDateFormat.format(System.currentTimeMillis());
        presenterTimeline.postTweet(status);
    }


    @OnClick(R.id.bt_timeline)
    public void onClickLoadTimeline(View view) {
        if (syncInProgress || !mBound) return;
        showProgress(true);
        TweetAdapter tweetAdapter = (TweetAdapter) rv_twitts.getAdapter();
        Tweet lastTweed = tweetAdapter == null ? null : tweetAdapter.getLastTweet();
        presenterTimeline.getNewTweets(lastTweed);
    }

    @OnClick(R.id.bt_logout)
    public void onClickLogout(View view) {
        Twitter.getSessionManager().clearActiveSession();
        presenterTimeline.clearTweets();
        Utils.fadeOutView(rl_timeline);
        Utils.fadeInView(fl_login);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            SyncService.LocalBinder binder = (SyncService.LocalBinder) service;
            mService = binder.getService();
            syncInProgress = mService.isSyncInProgress();
            mService.setListener(new SyncListener() {
                @Override
                public void onStartSync() {
                    syncInProgress = true;
                    showProgress(true);
                }

                @Override
                public void onFinishSync() {
                    presenterTimeline.getAllTweetsFromRealmBG();
                }
            });
            mBound = true;
            if (!syncInProgress) mService.syncBG();
            else showProgress(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void updateTweets(List<Tweet> result) {
        syncInProgress = false;
        showProgress(false);
        rv_twitts.setAdapter(new TweetAdapter(result));
    }

    public void startSyncTweets() {
        if (syncInProgress) return;
        if (mBound) mService.syncBG();
        else
            bindService(new Intent(this, SyncService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void showProgress(boolean show) {
        pb_progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void addNewTweets(List<Tweet> tweets) {
        showProgress(false);
        if (tweets == null || tweets.size() < 0) return;
        TweetAdapter tweetAdapter = (TweetAdapter) rv_twitts.getAdapter();
        if (tweetAdapter != null) {
            tweetAdapter.addTweets(tweets);
        } else {
            rv_twitts.setAdapter(new TweetAdapter(tweets));
        }
    }

    public void tweetPosted(Tweet tweet) {
        showProgress(false);
        if (rv_twitts.getAdapter() == null) {
            ArrayList<Tweet> tweets = new ArrayList<>();
            tweets.add(tweet);
            rv_twitts.setAdapter(new TweetAdapter(tweets));
        } else {
            ((TweetAdapter) rv_twitts.getAdapter()).addTweet(tweet);
        }
        rv_twitts.scrollToPosition(0);
    }

    public void tweetPostFailed(TwitterException exception) {
        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        showProgress(false);
    }

    public void loadNewTweetsFailure(Throwable exception) {
        showProgress(false);
        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private class TweetAdapter extends RecyclerView.Adapter<TweetViewHolder> {
        List<Tweet> tweets;

        public TweetAdapter(List<Tweet> data) {
            tweets = new ArrayList<>(data);
        }

        public void addTweet(Tweet tweet) {
            tweets.add(0, tweet);
            notifyItemInserted(0);
        }

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TweetViewHolder(new TweetView(parent.getContext(), new LocalTweet(null, null, 0)), this);
        }

        @Override
        public void onBindViewHolder(TweetViewHolder holder, int position) {
            holder.applyData(tweets.get(position));
        }

        @Override
        public int getItemCount() {
            return tweets.size();
        }

        public void addTweets(List<Tweet> tweets) {
            this.tweets.addAll(0, tweets);
            notifyItemRangeInserted(0, tweets.size());
        }

        public Tweet getLastTweet() {
            return tweets.size() == 0 ? null : tweets.get(0);
        }
    }

    class TweetViewHolder extends VHItem<Tweet, TweetAdapter> {

        public TweetViewHolder(View itemView, TweetAdapter adapter) {
            super(itemView, adapter);
        }

        @Override
        public void applyData(Tweet tweet) {
            ((TweetView) itemView).setTweet(tweet);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            if (mService != null) mService.setListener(null);
            mBound = false;
        }
        syncInProgress = false;
        presenterTimeline.release();
        networkStateReceiver.unregister(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenterTimeline.connect(this);
        startSyncTweets();
        networkStateReceiver.register(this);
    }

}
