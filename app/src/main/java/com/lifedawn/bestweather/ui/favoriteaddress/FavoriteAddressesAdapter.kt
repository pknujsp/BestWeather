package com.lifedawn.bestweather.ui.favoriteaddress

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto
import com.lifedawn.bestweather.databinding.FavoriteAddressItemBinding

class FavoriteAddressesAdapter(private val showCheckBtn: Boolean) : RecyclerView.Adapter<FavoriteAddressesAdapter.ViewHolder>() {
    private val favoriteAddressDtoList: MutableList<FavoriteAddressDto> = ArrayList()
    private var onClickedAddressListener: OnClickedAddressListener? = null
    fun setOnClickedAddressListener(onClickedAddressListener: OnClickedAddressListener?) {
        this.onClickedAddressListener = onClickedAddressListener
    }

    fun setFavoriteAddressDtoList(favoriteAddressDtoList: List<FavoriteAddressDto>?) {
        this.favoriteAddressDtoList.clear()
        this.favoriteAddressDtoList.addAll(favoriteAddressDtoList!!)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FavoriteAddressItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind()
    }

    override fun getItemCount(): Int {
        return favoriteAddressDtoList.size
    }

    fun getFavoriteAddressDtoList(): List<FavoriteAddressDto> {
        return favoriteAddressDtoList
    }

    protected inner class ViewHolder(private val binding: FavoriteAddressItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        private var favoriteAddressDto: FavoriteAddressDto? = null

        init {
            if (!showCheckBtn) binding.check.visibility = View.GONE
            binding.check.setOnClickListener { onClickedAddressListener!!.onClicked(favoriteAddressDtoList[bindingAdapterPosition]) }
            binding.delete.setOnClickListener {
                val position = bindingAdapterPosition
                onClickedAddressListener!!.onClickedDelete(favoriteAddressDtoList[position], position)
            }
            binding.markerOnMap.setOnClickListener {
                val position = bindingAdapterPosition
                onClickedAddressListener!!.onShowMarker(favoriteAddressDtoList[position], position)
            }
        }

        fun onBind() {
            favoriteAddressDto = favoriteAddressDtoList[bindingAdapterPosition]
            binding.addressName.text = favoriteAddressDto!!.displayName
            binding.countryName.text = favoriteAddressDto!!.countryName
        }
    }

    interface OnClickedAddressListener {
        fun onClickedDelete(favoriteAddressDto: FavoriteAddressDto, position: Int)
        fun onClicked(favoriteAddressDto: FavoriteAddressDto?)
        fun onShowMarker(favoriteAddressDto: FavoriteAddressDto?, position: Int)
    }
}