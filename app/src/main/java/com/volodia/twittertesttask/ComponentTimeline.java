package com.volodia.twittertesttask;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(
        modules = ModuleTimeline.class
)
public interface ComponentTimeline {
     void inject(ActivityTimeline activityTimeline);
}