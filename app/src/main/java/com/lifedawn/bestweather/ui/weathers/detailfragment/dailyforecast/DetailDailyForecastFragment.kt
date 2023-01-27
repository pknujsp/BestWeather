package com.lifedawn.bestweather.ui.weathers.detailfragment.dailyforecast

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback.clear
import com.lifedawn.bestweather.ui.weathers.detailfragment.base.BaseDetailDailyForecastFragment

class DetailDailyForecastFragment : BaseDetailDailyForecastFragment() {
    private var adapter: DailyForecastListAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.fragmentTitle.setText(R.string.detail_daily_forecast)
        binding.listview.addItemDecoration(DividerItemDecoration(requireContext().applicationContext, DividerItemDecoration.VERTICAL))
    }

    override fun setDataViewsByList() {
        var hasPrecipitationVolume = false
        for ((_, valuesList) in dailyForecastDtoList!!) {
            for ((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, isHasRainVolume, isHasSnowVolume, isHasPrecipitationVolume) in valuesList) {
                if (isHasPrecipitationVolume || isHasRainVolume || isHasSnowVolume) {
                    hasPrecipitationVolume = true
                    break
                }
            }
            if (hasPrecipitationVolume) {
                break
            }
        }
        adapter = DailyForecastListAdapter(this)
        adapter!!.setDailyForecastDtoList(dailyForecastDtoList!!, hasPrecipitationVolume)
        binding.listview.adapter = adapter
    }

    override fun setDataViewsByTable() {}
    override fun onDestroy() {
        adapter!!.dailyForecastDtoList.clear()
        adapter = null
        super.onDestroy()
    }

    override fun onClickedItem(position: Int) {
        super.onClickedItem(position)
    }
}