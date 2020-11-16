package org.tasks.location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.tasks.R
import org.tasks.Strings.isNullOrEmpty
import org.tasks.billing.Inventory
import org.tasks.data.Place
import org.tasks.data.PlaceUsage
import org.tasks.location.LocationPickerAdapter.PlaceViewHolder
import org.tasks.themes.ColorProvider
import org.tasks.themes.CustomIcons.getIconResId
import org.tasks.themes.DrawableUtil

class LocationPickerAdapter internal constructor(
    private val context: Context,
    private val inventory: Inventory,
    private val colorProvider: ColorProvider,
    private val callback: OnLocationPicked
) : ListAdapter<PlaceUsage, PlaceViewHolder>(DiffCallback()) {
    override fun getItemId(position: Int): Long = getItem(position)!!.place.id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_place, parent, false),
            callback
        )
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place, getColor(place!!.color), getIcon(place.icon))
    }

    private fun getColor(@ColorInt tint: Int): Int {
        if (tint != 0) {
            val color = colorProvider.getThemeColor(tint, true)
            if (color.isFree || inventory.purchasedThemes()) {
                return color.primaryColor
            }
        }
        return context.getColor(R.color.text_primary)
    }

    private fun getIcon(index: Int): Int {
        if (index < 1000 || inventory.hasPro) {
            val icon = getIconResId(index)
            if (icon != null) {
                return icon
            }
        }
        return R.drawable.ic_outline_place_24px
    }

    interface OnLocationPicked {
        fun picked(place: Place)
        fun settings(place: Place)
    }

    class PlaceViewHolder internal constructor(val itemView: View, private val onLocationPicked: OnLocationPicked) :
        RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.name)
        private val address: TextView = itemView.findViewById(R.id.address)
        private val icon: ImageView = itemView.findViewById(R.id.place_icon)
        private lateinit var place: Place

        fun bind(placeUsage: PlaceUsage, color: Int, icon: Int) {
            place = placeUsage.place

            itemView.setOnClickListener { v: View? -> onLocationPicked.picked(place) }
            itemView
                .findViewById<View>(R.id.location_settings)
                .setOnClickListener { onLocationPicked.settings(place) }

            val name = place!!.displayName
            val address = place!!.displayAddress
            val wrapped = DrawableUtil.getWrapped(itemView.context, icon)
            this.icon.setImageDrawable(wrapped)
            this.icon.drawable.setTint(color)
            this.name.text = name
            if (address.isNullOrEmpty() || address == name) {
                this.address.visibility = View.GONE
            } else {
                this.address.text = address
                this.address.visibility = View.VISIBLE
            }
        }

        init {

        }
    }

    internal class DiffCallback : DiffUtil.ItemCallback<PlaceUsage>() {
        override fun areItemsTheSame(oldItem: PlaceUsage, newItem: PlaceUsage): Boolean {
            return oldItem.place.uid == newItem.place.uid
        }

        override fun areContentsTheSame(oldItem: PlaceUsage, newItem: PlaceUsage): Boolean {
            return oldItem == newItem
        }
    }
}