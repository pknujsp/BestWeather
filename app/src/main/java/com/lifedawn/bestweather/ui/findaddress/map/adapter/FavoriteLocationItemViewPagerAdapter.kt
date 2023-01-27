package com.lifedawn.bestweather.ui.findaddress.map.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.databinding.FoundAddressItemViewpagerBinding
import com.lifedawn.bestweather.ui.findaddress.map.adapter.FavoriteLocationItemViewPagerAdapter.FavoriteItemViewHolder
import com.lifedawn.bestweather.ui.findaddress.map.interfaces.OnClickedLocationBtnListener
import com.lifedawn.bestweather.ui.findaddress.map.interfaces.OnClickedScrollBtnListener

class FavoriteLocationItemViewPagerAdapter : RecyclerView.Adapter<FavoriteItemViewHolder>() {
    var addressList: List<FavoriteAddressDto>? = null
        private set
    private var onClickedLocationBtnListener: OnClickedLocationBtnListener<FavoriteAddressDto>? = null
    private var onClickedScrollBtnListener: OnClickedScrollBtnListener? = null
    private var showAddBtn = true
    fun setShowAddBtn(showAddBtn: Boolean): FavoriteLocationItemViewPagerAdapter {
        this.showAddBtn = showAddBtn
        return this
    }

    fun setAddressList(addressList: List<FavoriteAddressDto>?): FavoriteLocationItemViewPagerAdapter {
        this.addressList = addressList
        return this
    }

    fun setOnClickedLocationBtnListener(onClickedLocationBtnListener: OnClickedLocationBtnListener<FavoriteAddressDto>?): FavoriteLocationItemViewPagerAdapter {
        this.onClickedLocationBtnListener = onClickedLocationBtnListener
        return this
    }

    fun setOnClickedScrollBtnListener(onClickedScrollBtnListener: OnClickedScrollBtnListener?): FavoriteLocationItemViewPagerAdapter {
        this.onClickedScrollBtnListener = onClickedScrollBtnListener
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteItemViewHolder {
        return FavoriteItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.found_address_item_viewpager, parent, false))
    }

    override fun onBindViewHolder(holder: FavoriteItemViewHolder, position: Int) {
        holder.setDataView()
    }

    override fun getItemCount(): Int {
        return addressList!!.size
    }

    inner class FavoriteItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        protected var binding: FoundAddressItemViewpagerBinding

        init {
            binding = FoundAddressItemViewpagerBinding.bind(view)
            binding.addBtn.visibility = if (showAddBtn) View.VISIBLE else View.GONE
            binding.addBtn.setOnClickListener { onClickedLocationBtnListener!!.onSelected(addressList!![bindingAdapterPosition], false) }
            binding.removeBtn.setOnClickListener { onClickedLocationBtnListener!!.onSelected(addressList!![bindingAdapterPosition], true) }
            binding.left.setOnClickListener { onClickedScrollBtnListener!!.toLeft() }
            binding.right.setOnClickListener { onClickedScrollBtnListener!!.toRight() }
        }

        fun setDataView() {
            val position = bindingAdapterPosition
            val itemPosition = (position + 1).toString() + " / " + itemCount
            binding.itemPosition.text = itemPosition
            val address = addressList!![position]
            binding.addressName.text = address.displayName
            binding.country.text = address.countryName
        }
    }
}