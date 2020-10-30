package org.tasks.injection

import android.app.Activity
import android.content.Context
import com.mapbox.android.core.location.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import org.tasks.location.*
import org.tasks.location.support.HuaweiLocationEngineImpl
import org.tasks.location.support.LocationEngineDerived

@Module
@InstallIn(ActivityComponent::class)
class LocationModule {
    @Provides
    @ActivityScoped
    fun getPlaceSearchProvider(activity: Activity): PlaceSearchProvider =
        HuaweiSiteKitSearchProvider(activity)

    @Provides
    @ActivityScoped
    fun getMapFragment(@ApplicationContext context: Context): MapFragment =
        HuaweiMapFragment(context)

    @Provides
    @ActivityScoped
    fun getLocationProvider(@ApplicationContext context: Context): LocationProvider =
        LocationEngineDerived(HuaweiLocationEngineImpl(context))
}


