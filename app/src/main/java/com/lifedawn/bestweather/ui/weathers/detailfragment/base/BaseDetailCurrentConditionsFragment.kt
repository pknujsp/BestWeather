package com.lifedawn.bestweather.ui.weathers.detailfragment.base

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.ViewModelProvider
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.data.MyApplication
import com.lifedawn.bestweather.databinding.BaseLayoutDetailCurrentConditionsBinding
import com.lifedawn.bestweather.ui.weathers.simplefragment.interfaces.IWeatherValues
import com.lifedawn.bestweather.ui.weathers.viewmodels.WeatherFragmentViewModel
import java.time.ZoneId

open class BaseDetailCurrentConditionsFragment constructor() : Fragment(), IWeatherValues {
    @JvmField protected var binding: BaseLayoutDetailCurrentConditionsBinding? = null
    protected var layoutInflater: LayoutInflater? = null
    protected var tempUnit: ValueUnits? = null
    protected var windUnit: ValueUnits? = null
    protected var visibilityUnit: ValueUnits? = null
    protected var clockUnit: ValueUnits? = null
    protected var latitude: Double? = null
    protected var longitude: Double? = null
    protected var zoneId: ZoneId? = null
    protected var mainWeatherProviderType: WeatherProviderType? = null
    @JvmField protected var bundle: Bundle? = null
    @JvmField protected var weatherFragmentViewModel: WeatherFragmentViewModel? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = if (savedInstanceState != null) savedInstanceState else getArguments()
        zoneId = bundle!!.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle!!.getDouble(BundleKey.Longitude.name)
        mainWeatherProviderType = bundle!!.getSerializable(
            BundleKey.WeatherProvider.name
        ) as WeatherProviderType?
        tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit()
        windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit()
        visibilityUnit = MyApplication.VALUE_UNIT_OBJ.getVisibilityUnit()
        clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit()
        weatherFragmentViewModel = ViewModelProvider(requireParentFragment()).get(
            WeatherFragmentViewModel::class.java
        )
    }

    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BaseLayoutDetailCurrentConditionsBinding.inflate(inflater, container, false)
        binding!!.weatherCardViewHeader.forecastName.setText(R.string.current_conditions)
        binding!!.weatherCardViewHeader.compareForecast.setVisibility(View.GONE)
        binding!!.weatherCardViewHeader.detailForecast.setVisibility(View.GONE)
        return binding!!.getRoot()
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layoutInflater = getLayoutInflater()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    public override fun onDestroyView() {
        super.onDestroyView()
        binding!!.conditionsGrid.removeAllViews()
        layoutInflater = null
        binding = null
    }

    public override fun setValuesToViews() {}
    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(bundle)
    }

    protected fun addGridItem(labelDescriptionId: Int, value: String?, labelIconId: Int?): View {
        val gridItem: View = layoutInflater!!.inflate(R.layout.view_detail_weather_data_item, null, false)
        (gridItem.findViewById<View>(R.id.label) as TextView).setText(labelDescriptionId)
        (gridItem.findViewById<View>(R.id.value) as TextView).setText(value)
        if (labelIconId == null) {
            gridItem.findViewById<View>(R.id.label_icon).setVisibility(View.GONE)
        } else {
            (gridItem.findViewById<View>(R.id.label_icon) as ImageView).setImageResource(labelIconId)
        }
        (gridItem.findViewById<View>(R.id.value) as TextView).setTextColor(Color.WHITE)
        val cellCount: Int = binding!!.conditionsGrid.getChildCount()
        val row: Int = cellCount / binding!!.conditionsGrid.getColumnCount()
        val column: Int = cellCount % binding!!.conditionsGrid.getColumnCount()
        val layoutParams: GridLayout.LayoutParams = GridLayout.LayoutParams()
        layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1f)
        layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1f)
        binding!!.conditionsGrid.addView(gridItem, layoutParams)
        return gridItem
    }

    protected fun addGridItem(labelDescriptionId: Int, value: String?, labelIconId: Int?, defaultIconColor: Boolean): View {
        val gridItem: View = addGridItem(labelDescriptionId, value, labelIconId)
        if (!defaultIconColor) (gridItem.findViewById<View>(R.id.label_icon) as ImageView).setImageTintList(ColorStateList.valueOf(Color.WHITE))
        return gridItem
    }
}