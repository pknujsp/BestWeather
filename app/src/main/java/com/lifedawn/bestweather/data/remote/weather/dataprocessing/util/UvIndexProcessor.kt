package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util

import android.content.Context
import com.lifedawn.bestweather.R

object UvIndexProcessor {
    private val UV_INDEX_GRADES = IntArray(4)
    private val UV_INDEX_GRADE_COLORS = IntArray(5)
    private val UV_INDEX_GRADE_DESCRIPTIONS = arrayOfNulls<String>(5)
    fun init(context: Context) {
        val aqiGrades = context.resources.getIntArray(R.array.UvIndexGrades)
        val aqiGradeColors = context.resources.getIntArray(R.array.UvIndexGradeColors)
        val aqiGradeDescriptions = context.resources.getStringArray(R.array.UvIndexGradeDescriptions)
        for (i in aqiGrades.indices) {
            UV_INDEX_GRADES[i] = aqiGrades[i]
        }
        for (i in aqiGradeColors.indices) {
            UV_INDEX_GRADE_COLORS[i] = aqiGradeColors[i]
        }
        for (i in aqiGradeDescriptions.indices) {
            UV_INDEX_GRADE_DESCRIPTIONS[i] = aqiGradeDescriptions[i]
        }
    }

    fun getGradeColorId(grade: Int): Int {
        /*
       <item>2</item>
        <item>5</item>
        <item>7</item>
        <item>10</item>
		 */
        for (i in UV_INDEX_GRADES.indices) {
            if (grade <= UV_INDEX_GRADES[i]) {
                return UV_INDEX_GRADE_COLORS[i]
            }
        }
        //if extreme
        return UV_INDEX_GRADE_COLORS[4]
    }

    fun getGradeDescription(grade: Int): String? {
        /*
       <item>2</item>
        <item>5</item>
        <item>7</item>
        <item>10</item>
		 */
        for (i in UV_INDEX_GRADES.indices) {
            if (grade <= UV_INDEX_GRADES[i]) {
                return UV_INDEX_GRADE_DESCRIPTIONS[i]
            }
        }
        //if extreme
        return UV_INDEX_GRADE_DESCRIPTIONS[4]
    }
}