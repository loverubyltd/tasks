package org.tasks.location

import android.location.Location
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * A wrapper class representing location result from the location engine.
 *
 *
 * TODO: Override default equals(), hashCode() and toString()
 *
 * @since 1.0.0
 */
class LocationResult private constructor(locations: List<Location>) {

    private val _locations = locations.toImmutableList()

    /**
     * Returns locations computed, ordered from oldest to newest.
     *
     * @return ordered list of locations.
     * @since 1.0.0
     */
    val locations: ImmutableList<Location>
        get() = _locations

    /**
     * Returns most recent location available in this result.
     *
     * @return the most recent location [Location] or null.
     * @since 1.0.0
     */
    val lastLocation: Location?
        get() = if (_locations.isEmpty()) null else _locations[0]

    companion object {


        /**
         * Creates [LocationResult] instance for location.
         *
         * @param location default location added to the result.
         * @return instance of the new location result.
         * @since 1.0.0
         */
        fun create(location: Location?): LocationResult {
            val locations = if (location != null) listOf(location) else emptyList()
            return LocationResult(locations)
        }

        /**
         * Creates [LocationResult] instance for given list of locations.
         *
         * @param locations list of locations.
         * @return instance of the new location result.
         * @since 1.0.0
         */
        fun create(locations: List<Location?>?): LocationResult {
            val locationsList = locations?.filterNotNull() ?: emptyList()
            return LocationResult(locationsList)
        }

    }
}
