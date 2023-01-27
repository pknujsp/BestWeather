package com.lifedawn.bestweather.ui.weathers.detailfragment.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.weathers.detailfragment.dto.GridItemDto

class DetailCurrentConditionsAdapter(private val context: Context) : BaseAdapter() {
    private var gridItemDtoList: List<GridItemDto>? = null
    private val layoutInflater: LayoutInflater

    init {
        layoutInflater = LayoutInflater.from(context)
    }

    fun setGridItemDtoList(gridItemDtoList: List<GridItemDto>?): DetailCurrentConditionsAdapter {
        this.gridItemDtoList = gridItemDtoList
        return this
    }

    override fun getCount(): Int {
        return gridItemDtoList!!.size
    }

    override fun getItem(i: Int): Any {
        return null
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            view = layoutInflater.inflate(R.layout.view_detail_weather_data_item, viewGroup, false)
        }
        val gridItemDto = gridItemDtoList!![i]
        if (gridItemDto.img == null) {
            view.findViewById<View>(R.id.label_icon).visibility = View.GONE
        } else {
            view.findViewById<View>(R.id.label_icon).visibility = View.VISIBLE
            (view.findViewById<View>(R.id.label_icon) as ImageView).setImageDrawable(gridItemDto.img)
        }
        (view.findViewById<View>(R.id.label) as TextView).text = gridItemDto.label
        (view.findViewById<View>(R.id.label) as TextView).setTextColor(Color.WHITE)
        (view.findViewById<View>(R.id.value) as TextView).text = gridItemDto.value
        (view.findViewById<View>(R.id.value) as TextView).setTextColor(Color.WHITE)
        return view
    }
}