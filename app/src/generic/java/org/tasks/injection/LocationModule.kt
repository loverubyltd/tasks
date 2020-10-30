package org.tasks.injection

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
    fun getPlaceSearchProvider(@ApplicationContext context: Context): PlaceSearchProvider {
        return MapboxSearchProvider(context)
    }

    @Provides
    @ActivityScoped
    fun getMapFragment(@ApplicationContext context: Context): MapFragment {
        return MapboxMapFragment(context)
    }

    @Provides
    @ActivityScoped
    fun getLocationProvider(@ApplicationContext context: Context): LocationProvider =
        MapboxBestLocationProvider(context)
}