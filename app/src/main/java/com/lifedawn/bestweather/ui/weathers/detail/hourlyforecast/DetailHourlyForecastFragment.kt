package com.lifedawn.bestweather.ui.weathers.detail.hourlyforecast

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.ui.weathers.detail.base.BaseDetailForecastFragment.HourlyForecastListAdapter.ShowDataType
import com.lifedawn.bestweather.ui.weathers.detail.base.BaseDetailHourlyForecastFragment

class DetailHourlyForecastFragment : BaseDetailHourlyForecastFragment() {
    private var adapter: HourlyForecastListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.fragmentTitle.setText(R.string.detail_hourly_forecast)
        binding.listview.addItemDecoration(DividerItemDecoration(requireContext().applicationContext, DividerItemDecoration.VERTICAL))
    }

    override fun setDataViewsByList() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val selectedItem = sharedPreferences.getInt(getString(R.string.pref_key_detail_hourly_forecast_show_data), 0)
        var showDataType: ShowDataType? = null
        when (selectedItem) {
            0 -> showDataType = ShowDataType.Precipitation
            1 -> showDataType = ShowDataType.Wind
            2 -> showDataType = ShowDataType.Humidity
        }
        adapter = HourlyForecastListAdapter(this, showDataType)
        adapter!!.setHourlyForecastDtoList(hourlyForecastDtoList)
        binding.listview.setHasFixedSize(true)
        binding.listview.adapter = adapter
    }

    override fun setDataViewsByTable() {}
    override fun onDestroy() {
        adapter!!.hourlyForecastDtoList.clear()
        adapter = null
        super.onDestroy()
    }

    override fun onClickedItem(position: Int) {
        super.onClickedItem(position)
    }
}