package org.tasks.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.location.Geocoder
import org.tasks.location.MapboxGeocoder

@Module
@InstallIn(ApplicationComponent::class)
class ApplicationGeocodingModule {

    @Provides
    fun getGeocoder(@ApplicationContext context: Context): Geocoder = MapboxGeocoder(context)

}