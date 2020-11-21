package org.tasks.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.HuaweiDebugNetworkInterceptor
import org.tasks.locale.Locale
import org.tasks.location.Geocoder
import org.tasks.location.HuaweiSiteKitGeocoder
import org.tasks.location.support.HuaweiSiteKitClient
import org.tasks.preferences.Preferences
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class ApplicationGeocodingModule {

    @Singleton
    @Provides
    fun getGeocodingClient(
        interceptor: HuaweiDebugNetworkInterceptor,
        preferences: Preferences
    ): HuaweiSiteKitClient = HuaweiSiteKitClient(interceptor, preferences)

    @Provides
    fun getGeocoder(
        @ApplicationContext context: Context,
        client: HuaweiSiteKitClient,
        locale: Locale
    ): Geocoder = HuaweiSiteKitGeocoder(context, client, locale)
}
