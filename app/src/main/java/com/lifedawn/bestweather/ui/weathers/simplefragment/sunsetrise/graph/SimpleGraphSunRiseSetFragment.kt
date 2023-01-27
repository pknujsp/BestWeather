package com.lifedawn.bestweather.ui.weathers.simplefragment.sunsetrise.graph

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lifedawn.bestweather.R
import com.lifedawn.bestweather.commons.constants.BundleKey
import com.lifedawn.bestweather.commons.constants.SunRiseSetType
import com.lifedawn.bestweather.commons.constants.ValueUnits
import com.lifedawn.bestweather.commons.constants.WeatherProviderType
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding
import com.lifedawn.bestweather.ui.weathers.WeatherFragment
import com.lifedawn.bestweather.ui.weathers.detailfragment.sunsetrise.DetailSunRiseSetFragment
import com.lifedawn.bestweather.ui.weathers.simplefragment.interfaces.IWeatherValues
import com.luckycatlabs.sunrisesunset.dto.Location
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SimpleGraphSunRiseSetFragment : Fragment(), IWeatherValues {
    private var binding: FragmentSunsetriseBinding? = null
    private var location: Location? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var addressName: String? = null
    private var countryCode: String? = null
    private var mainWeatherProviderType: WeatherProviderType? = null
    private var zoneId: ZoneId? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        latitude = bundle!!.getDouble(BundleKey.Latitude.name)
        longitude = bundle.getDouble(BundleKey.Longitude.name)
        addressName = bundle.getString(BundleKey.AddressName.name)
        countryCode = bundle.getString(BundleKey.CountryCode.name)
        mainWeatherProviderType = bundle.getSerializable(
            BundleKey.WeatherProvider.name
        ) as WeatherProviderType?
        zoneId = bundle.getSerializable(BundleKey.TimeZone.name) as ZoneId?
        location = Location(latitude!!, longitude!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSunsetriseBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.weatherCardViewHeader.detailForecast.visibility = View.VISIBLE
        binding!!.weatherCardViewHeader.compareForecast.visibility = View.INVISIBLE
        binding!!.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise)
        binding!!.weatherCardViewHeader.detailForecast.setOnClickListener {
            val detailSunRiseSetFragment = DetailSunRiseSetFragment()
            detailSunRiseSetFragment.arguments = arguments
            val tag = DetailSunRiseSetFragment::class.java.name
            val fragmentManager = parentFragment!!.parentFragmentManager
            fragmentManager.beginTransaction().hide(
                fragmentManager.findFragmentByTag(WeatherFragment::class.java.name)!!
            ).add(
                R.id.fragment_container,
                detailSunRiseSetFragment, tag
            ).addToBackStack(tag).commit()
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        requireActivity().registerReceiver(broadcastReceiver, intentFilter)
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != null) {
                if (Intent.ACTION_TIME_TICK == intent.action) {
                    //sunSetRiseViewGroup.refresh();
                }
            }
        }
    }

    override fun setValuesToViews() {}
    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    private class GraphBackgroundView(context: Context, private val CLOCK_UNIT: ValueUnits, private val ZONE_OFFSET: ZoneOffset) :
        FrameLayout(context) {
        private val DATETIME_FORMATTER: DateTimeFormatter
        private val GRAPH_HEIGHT: Int
        private var currentViewBoxRect: Rect? = null

        init {
            DATETIME_FORMATTER = DateTimeFormatter.ofPattern(if (CLOCK_UNIT === ValueUnits.clock12) "M.d E\na h:mm" else "M.d E\nH:mm")
            GRAPH_HEIGHT = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, context.resources.displayMetrics).toInt()
            setWillNotDraw(false)
        }

        @SuppressLint("DrawAllocation")
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val graphCurrentView = GraphCurrentView(context, CLOCK_UNIT)
            currentViewBoxRect = graphCurrentView.getCurrentTimeInfoBoxSize()
            val height = GRAPH_HEIGHT * 2 + currentViewBoxRect!!.height() + graphCurrentView.BOX_MARGIN
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
        }

        override fun onDraw(canvas: Canvas) {}
    }

    private class GraphCurrentView(context: Context, clockUnit: ValueUnits) : View(context) {
        private var img: Drawable? = null
        private val DATETIME_FORMATTER: DateTimeFormatter
        private val BOX_PADDING: Int
        private val TEXT_SPACING: Int
        val BOX_MARGIN: Int
        private val CURRENT_TEXT: String
        private var currentTimeMap: Map<String, Point>? = null
        private var baselineY = 0
        private val boxPaint = Paint()
        private val linePaint = Paint()
        private val timeTextPaint = TextPaint()
        private val currentTextPaint = TextPaint()

        init {
            DATETIME_FORMATTER = DateTimeFormatter.ofPattern(if (clockUnit === ValueUnits.clock12) "M.d E\na h:mm" else "M.d E\nH:mm")
            val displayMetrics = context.resources.displayMetrics
            boxPaint.color = ContextCompat.getColor(context, R.color.gray)
            linePaint.color = Color.BLUE
            timeTextPaint.color = Color.BLACK
            timeTextPaint.textAlign = Paint.Align.CENTER
            timeTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, displayMetrics)
            currentTextPaint.color = Color.BLACK
            currentTextPaint.textAlign = Paint.Align.CENTER
            currentTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, displayMetrics)
            BOX_PADDING = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, displayMetrics).toInt()
            TEXT_SPACING = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, displayMetrics).toInt()
            BOX_MARGIN = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, displayMetrics).toInt()
            CURRENT_TEXT = context.getString(R.string.current)
        }

        fun setCurrentTimeMap(currentTimeMap: Map<String, Point>?) {
            this.currentTimeMap = currentTimeMap
        }

        fun setBaselineY(baselineY: Int) {
            this.baselineY = baselineY
        }

        fun setImg(sunRiseSetType: SunRiseSetType) {
            img = ContextCompat.getDrawable(
                context,
                if (sunRiseSetType === SunRiseSetType.RISE) R.drawable.day_clear else R.drawable.night_clear
            )
        }

        val currentTimeInfoBoxSize: Rect
            get() {
                val currentTextRect = Rect()
                currentTextPaint.getTextBounds(CURRENT_TEXT, 0, CURRENT_TEXT.length, currentTextRect)
                val timeTextRect = Rect()
                val time = ZonedDateTime.now().format(DATETIME_FORMATTER)
                val separatedText = time.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (separatedText[0].length > separatedText[1].length) {
                    timeTextPaint.getTextBounds(separatedText[0], 0, separatedText[0].length, timeTextRect)
                } else {
                    timeTextPaint.getTextBounds(separatedText[1], 0, separatedText[1].length, timeTextRect)
                }
                val columnWidth = timeTextRect.width()
                val builder = StaticLayout.Builder.obtain(time, 0, time.length, timeTextPaint, columnWidth)
                val staticLayout = builder.build()
                val boxRect = Rect()
                boxRect.left = 0
                boxRect.right = BOX_PADDING * 2 + timeTextRect.width()
                boxRect.top = 0
                boxRect.bottom = BOX_PADDING * 2 + currentTextRect.height() + TEXT_SPACING + staticLayout.height
                return boxRect
            }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

        override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
            super.onLayout(changed, left, top, right, bottom)
        }

        override fun onDraw(canvas: Canvas) {}
    }
}