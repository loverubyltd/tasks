package org.tasks.location

import org.tasks.data.Place

data class PlaceSearchResult @JvmOverloads internal constructor(
    val id: String,
    val name: String,
    val address: String,
    val place: Place? = null
) {
    override fun toString(): String {
        return "PlaceSearchResult{id='$id', name='$name', address='$address', place=$place}"
    }
}