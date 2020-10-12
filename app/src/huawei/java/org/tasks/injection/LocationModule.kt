package org.tasks.injection

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import org.tasks.location.*

@Module
@InstallIn(ActivityComponent::class)
class LocationModule {
    @Provides
    @ActivityScoped
    fun getPlaceSearchProvider(activity: Activity): PlaceSearchProvider {
        return HuaweiSiteKitSearchProvider(activity)
    }

    @Provides
    @ActivityScoped
    fun getMapFragment(@ApplicationContext context: Context): MapFragment {
        return HuaweiMapFragment(context)
    }
}