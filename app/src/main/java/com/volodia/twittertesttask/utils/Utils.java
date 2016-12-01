package com.volodia.twittertesttask.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.List;

/**
 * Created by Volodia on 30.11.2016.
 */

public class Utils {
    public static void fadeInView(View view) {
        if (view.getVisibility() != View.VISIBLE)
            fadeAnimation(view, 0, 1, 300, 0, View.VISIBLE, null);
    }

    public static void fadeAnimation(final View view, final float from, float to, int duration, int offset, final int finalVisibility, final OnAnimationEndListener listener) {
        if ((view.getVisibility() == View.INVISIBLE || view.getVisibility() == View.GONE) && from == 1) {
            return;
        }

        Animation a = new AlphaAnimation(from, to);
        a.setDuration(duration);
        a.setStartOffset(offset);
        a.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(finalVisibility);

                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
        });

        if (view.getAnimation() != null) {
            view.setAnimation(a);
        } else view.startAnimation(a);
    }

    public static void fadeOutView(View view) {
        fadeAnimation(view, 1, 0, 300, 0, View.INVISIBLE, null);
    }

    public static boolean notEmpty(List list) {
        return (list != null && list.size() > 0);
    }
}
