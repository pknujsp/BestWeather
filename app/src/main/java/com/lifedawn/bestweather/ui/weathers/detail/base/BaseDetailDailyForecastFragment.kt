package com.lifedawn.bestweather.ui.weathers.detail.base

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto
import com.lifedawn.bestweather.ui.weathers.detail.base.dialogfragments.DetailDailyForecastDialogFragment
import com.lifedawn.bestweather.ui.weathers.detail.base.dialogfragments.DetailHourlyForecastDialogFragment

open class BaseDetailDailyForecastFragment constructor() : BaseDetailForecastFragment() {
    public override fun onClickedItem(position: Int) {
        if (clickableItem) {
            clickableItem = false
            val bundle: Bundle = Bundle()
            bundle.putInt("FirstSelectedPosition", position)
            val detailHourlyForecastDialogFragment: DetailDailyForecastDialogFragment = DetailDailyForecastDialogFragment()
            detailHourlyForecastDialogFragment.setArguments(bundle)
            DetailDailyForecastDialogFragment.Companion.setDailyForecastDtoList(dailyForecastDtoList)
            detailHourlyForecastDialogFragment.show(
                getChildFragmentManager(),
                DetailHourlyForecastDialogFragment::class.java.getName()
            )
        }
    }

    override fun onFragmentStarted(fragment: Fragment?) {
        if (fragment is DetailDailyForecastDialogFragment) {
            super.onFragmentStarted(fragment)
        }
    }

    override fun setDataViewsByList() {}
    override fun setDataViewsByTable() {}

    companion object {
        @JvmField protected var dailyForecastDtoList: List<DailyForecastDto>? = null
        @JvmStatic
        fun setDailyForecastDtoList(dailyForecastDtoList: List<DailyForecastDto>?) {
            Companion.dailyForecastDtoList = dailyForecastDtoList
        }
    }
}