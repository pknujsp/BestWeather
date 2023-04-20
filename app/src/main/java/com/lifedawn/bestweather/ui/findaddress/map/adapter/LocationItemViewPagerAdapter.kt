package com.lifedawn.bestweather.ui.findaddress.map.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.classes.Geocoding
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.displayName
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.latitude
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto.longitude
import com.lifedawn.bestweather.databinding.FoundAddressItemViewpagerBinding
import com.lifedawn.bestweather.ui.findaddress.map.adapter.LocationItemViewPagerAdapter.LocationItemViewHolder
import com.lifedawn.bestweather.ui.findaddress.map.interfaces.OnClickedLocationBtnListener
import com.lifedawn.bestweather.ui.findaddress.map.interfaces.OnClickedScrollBtnListener

class LocationItemViewPagerAdapter : RecyclerView.Adapter<LocationItemViewHolder>() {
    private var favoriteAddressSet: Set<String>? = null
    private var addressList: List<Geocoding.AddressDto>? = null
    private var onClickedLocationBtnListener: OnClickedLocationBtnListener<Geocoding.AddressDto>? = null
    private var onClickedScrollBtnListener: OnClickedScrollBtnListener? = null
    fun setOnClickedLocationBtnListener(onClickedLocationBtnListener: OnClickedLocationBtnListener<Geocoding.AddressDto>?): LocationItemViewPagerAdapter {
        this.onClickedLocationBtnListener = onClickedLocationBtnListener
        return this
    }

    fun setOnClickedScrollBtnListener(onClickedScrollBtnListener: OnClickedScrollBtnListener?): LocationItemViewPagerAdapter {
        this.onClickedScrollBtnListener = onClickedScrollBtnListener
        return this
    }

    fun setAddressList(addressList: List<Geocoding.AddressDto>?): LocationItemViewPagerAdapter {
        this.addressList = addressList
        return this
    }

    fun getAddressList(): List<Geocoding.AddressDto>? {
        return addressList
    }

    fun setFavoriteAddressSet(favoriteAddressSet: Set<String>?): LocationItemViewPagerAdapter {
        this.favoriteAddressSet = favoriteAddressSet
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationItemViewHolder {
        return LocationItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.found_address_item_viewpager, parent, false))
    }

    override fun onBindViewHolder(holder: LocationItemViewHolder, position: Int) {
        holder.setDataView()
    }

    override fun getItemCount(): Int {
        return addressList!!.size
    }

    inner class LocationItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        protected var binding: FoundAddressItemViewpagerBinding

        init {
            binding = FoundAddressItemViewpagerBinding.bind(view)
            binding.removeBtn.visibility = View.GONE
            binding.addBtn.setOnClickListener { onClickedLocationBtnListener!!.onSelected(addressList!![bindingAdapterPosition], false) }
            binding.left.setOnClickListener { onClickedScrollBtnListener!!.toLeft() }
            binding.right.setOnClickListener { onClickedScrollBtnListener!!.toRight() }
        }

        fun setDataView() {
            val position = bindingAdapterPosition
            val itemPosition = (position + 1).toString() + " / " + itemCount
            binding.itemPosition.text = itemPosition
            val address: Geocoding.AddressDto = addressList!![position]
            binding.addressName.setText(address.displayName)
            binding.country.setText(address.country)
            if (favoriteAddressSet!!.contains(address.latitude + "" + address.longitude)) {
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
}