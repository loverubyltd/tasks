package org.tasks.injection

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.os.Looper
import com.huawei.hms.location.LocationServices
import com.mapbox.android.core.location.*
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
    fun getPlaceSearchProvider(activity: Activity): PlaceSearchProvider =
        HuaweiSiteKitSearchProvider(activity)

    @Provides
    @ActivityScoped
    fun getMapFragment(@ApplicationContext context: Context): MapFragment =
        HuaweiMapFragment(context)

    @Provides
    @ActivityScoped
    fun getLocationEngine(@ApplicationContext context: Context): LocationEngine =
        LocationEngineDerived(HuaweiLocationEngineImpl(context))
}


