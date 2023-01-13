package com.lifedawn.bestweather.data.remote.weather.dataprocessing.util;

import android.content.Context;

import com.lifedawn.bestweather.R;

public class UvIndexProcessor {
	private static final int[] UV_INDEX_GRADES = new int[4];
	private static final int[] UV_INDEX_GRADE_COLORS = new int[5];
	private static final String[] UV_INDEX_GRADE_DESCRIPTIONS = new String[5];

	private UvIndexProcessor() {
	}

	public static void init(Context context) {
		int[] aqiGrades = context.getResources().getIntArray(R.array.UvIndexGrades);
		int[] aqiGradeColors = context.getResources().getIntArray(R.array.UvIndexGradeColors);
		String[] aqiGradeDescriptions = context.getResources().getStringArray(R.array.UvIndexGradeDescriptions);

		for (int i = 0; i < aqiGrades.length; i++) {
			UV_INDEX_GRADES[i] = aqiGrades[i];
		}
		for (int i = 0; i < aqiGradeColors.length; i++) {
			UV_INDEX_GRADE_COLORS[i] = aqiGradeColors[i];
		}
		for (int i = 0; i < aqiGradeDescriptions.length; i++) {
			UV_INDEX_GRADE_DESCRIPTIONS[i] = aqiGradeDescriptions[i];
		}
	}

	public static int getGradeColorId(int grade) {
		/*
       <item>2</item>
        <item>5</item>
        <item>7</item>
        <item>10</item>
		 */
		for (int i = 0; i < UV_INDEX_GRADES.length; i++) {
			if (grade <= UV_INDEX_GRADES[i]) {
				return UV_INDEX_GRADE_COLORS[i];
			}
		}
		//if extreme
		return UV_INDEX_GRADE_COLORS[4];
	}

	public static String getGradeDescription(int grade) {
		/*
       <item>2</item>
        <item>5</item>
        <item>7</item>
        <item>10</item>
		 */
		for (int i = 0; i < UV_INDEX_GRADES.length; i++) {
			if (grade <= UV_INDEX_GRADES[i]) {
				return UV_INDEX_GRADE_DESCRIPTIONS[i];
			}
		}
		//if extreme
		return UV_INDEX_GRADE_DESCRIPTIONS[4];
	}

}
