package org.tasks.location

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.*
import org.tasks.R
import org.tasks.Strings.isNullOrEmpty

internal class LocationSearchAdapter(
    @param:DrawableRes private val attributionRes: Int,
    private val callback: OnPredictionPicked
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ListUpdateCallback {
    private val differ: AsyncListDiffer<PlaceSearchResult> = AsyncListDiffer(this, AsyncDifferConfig.Builder(DiffCallback()).build())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.row_place, parent, false),
            callback
        ) else FooterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_place_footer, parent, false),
            attributionRes
        )
    }

    fun submitList(list: List<PlaceSearchResult>) {
        differ.submitList(list)
    }

    override fun getItemCount(): Int = differ.currentList.size + 1

    override fun getItemViewType(position: Int): Int = if (position < itemCount - 1) 0 else 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 0) {
            (holder as SearchViewHolder).bind(differ.currentList[position])
        } else {
            (holder as FooterViewHolder).bind(position)
        }
    }

    override fun onInserted(position: Int, count: Int) {
        notifyItemRangeInserted(position, count)
        updateFooter()
    }

    override fun onRemoved(position: Int, count: Int) {
        notifyItemRangeRemoved(position, count)
        updateFooter()
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        notifyItemRangeChanged(position, count, payload)
    }

    private fun updateFooter() {
        notifyItemChanged(itemCount - 1)
    }

    fun interface OnPredictionPicked {
        fun picked(prediction: PlaceSearchResult)
    }

    internal class SearchViewHolder(itemView: View, onPredictionPicked: OnPredictionPicked) :
        RecyclerView.ViewHolder(itemView) {
        private val name: TextView= itemView.findViewById(R.id.name)
        private val address: TextView = itemView.findViewById(R.id.address)
        private var prediction: PlaceSearchResult? = null

        fun bind(prediction: PlaceSearchResult) {
            this.prediction = prediction
            val predictedName = prediction.name
            val predictedAddress = prediction.address
            name.text = predictedName
            if (predictedAddress.isNullOrEmpty() || predictedAddress == predictedName) {
                address.visibility = View.GONE
            } else {
                address.text = predictedAddress
                address.visibility = View.VISIBLE
            }
        }

        init {
            itemView.setOnClickListener { prediction?.let { onPredictionPicked.picked(it) } }
            itemView.findViewById<View>(R.id.place_icon).visibility = View.INVISIBLE
        }
    }

    internal class FooterViewHolder(itemView: View, @DrawableRes attributionRes: Int) :
        RecyclerView.ViewHolder(itemView) {
        val divider: View = itemView.findViewById(R.id.divider)

        fun bind(position: Int) {
            divider.visibility = if (position == 0) View.GONE else View.VISIBLE
        }

        init {
            (itemView.findViewById<View>(R.id.place_attribution) as ImageView).setImageResource(
                attributionRes
            )
        }
    }

    internal class DiffCallback : DiffUtil.ItemCallback<PlaceSearchResult>() {
        override fun areItemsTheSame(
            oldItem: PlaceSearchResult, newItem: PlaceSearchResult
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PlaceSearchResult, newItem: PlaceSearchResult
        ): Boolean = oldItem == newItem
    }
}