package com.lifedawn.bestweather.ui.findaddress

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.Geocoding
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.displayName
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.latitude
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.longitude
import com.lifedawn.bestweather.databinding.FoundAddressItemBinding
import com.lifedawn.bestweather.ui.findaddress.FindAddressFragment.OnAddressListListener
import com.lifedawn.bestweather.ui.findaddress.FindAddressFragment.OnListListener

class FoundAddressesAdapter() : RecyclerView.Adapter<FoundAddressesAdapter.ViewHolder>(), Filterable {
    private val itemList: MutableList<Geocoding.AddressDto> = ArrayList<Geocoding.AddressDto>()
    private val filteredAddressList: MutableList<Geocoding.AddressDto?> = ArrayList<Geocoding.AddressDto?>()
    private val favoriteAddressSet: MutableSet<String> = HashSet()
    private var filter: Filter? = null
    private var onClickedAddressListener: OnClickedAddressListener? = null
    private var onAddressListListener: OnAddressListListener? = null
    private var onListListener: OnListListener? = null
    fun setFavoriteAddressSet(favoriteAddressSet: Set<String>?): FoundAddressesAdapter {
        this.favoriteAddressSet.clear()
        this.favoriteAddressSet.addAll((favoriteAddressSet)!!)
        return this
    }

    fun setOnListListener(onListListener: OnListListener?): FoundAddressesAdapter {
        this.onListListener = onListListener
        return this
    }

    fun setOnAddressListListener(onAddressListListener: OnAddressListListener?): FoundAddressesAdapter {
        this.onAddressListListener = onAddressListListener
        return this
    }

    fun setOnClickedAddressListener(onClickedAddressListener: OnClickedAddressListener?) {
        this.onClickedAddressListener = onClickedAddressListener
    }

    fun setItemList(itemList: List<Geocoding.AddressDto>?) {
        this.itemList.clear()
        this.itemList.addAll((itemList)!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FoundAddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.root.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return filteredAddressList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = ListFilter()
        }
        return filter!!
    }

    inner class ViewHolder(private val binding: FoundAddressItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        init {
            binding.root.setOnClickListener(View.OnClickListener {
                onListListener!!.onPOIItemSelectedByList(
                    bindingAdapterPosition
                )
            })
            binding.addBtn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    onClickedAddressListener!!.onClickedAddress(filteredAddressList[bindingAdapterPosition])
                }
            })
        }

        fun onBind() {
            val address: Geocoding.AddressDto? = filteredAddressList[bindingAdapterPosition]
            binding.country.setText(address.country)
            binding.addressName.setText(address.displayName)
            if (favoriteAddressSet.contains(address.latitude + "" + address.longitude)) {
                binding.addBtn.isClickable = false
                binding.addBtn.setText(R.string.duplicate)
                binding.addBtn.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
            } else {
                binding.addBtn.isClickable = true
                binding.addBtn.setText(R.string.add)
                binding.addBtn.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
            }
        }
    }

    interface OnClickedAddressListener {
        fun onClickedAddress(addressDto: Geocoding.AddressDto?)
    }

    private inner class ListFilter() : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filterResults = FilterResults()
            if (constraint == null || constraint.length == 0) {
                filterResults.count = itemList.size
                filterResults.values = itemList
            } else {
                val newAddressList: MutableList<Geocoding.AddressDto> = ArrayList<Geocoding.AddressDto>()
                for (address: Geocoding.AddressDto in itemList) {
                    if (address.displayName.contains(constraint.toString())) {
                        newAddressList.add(address)
                    }
                }
                filterResults.values = newAddressList
                filterResults.count = newAddressList.size
            }
            return filterResults
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filteredAddressList.clear()
            filteredAddressList.addAll((results.values as Collection<Geocoding.AddressDto?>))
            onAddressListListener!!.onSearchedAddressList(filteredAddressList)
            notifyDataSetChanged()
        }
    }
}