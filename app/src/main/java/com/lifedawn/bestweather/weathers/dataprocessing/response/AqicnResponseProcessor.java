package com.lifedawn.bestweather.weathers.dataprocessing.response;

import android.content.Context;

import com.lifedawn.bestweather.R;

public class AqicnResponseProcessor {
	private static final int[] AQI_GRADES = new int[5];
	private static final int[] AQI_GRADE_COLORS = new int[6];
	private static final String[] AQI_GRADE_DESCRIPTIONS = new String[6];
	
	private AqicnResponseProcessor() {}
	
	public static void init(Context context) {
		int[] aqiGrades = context.getResources().getIntArray(R.array.AqiGrades);
		int[] aqiGradeColors = context.getResources().getIntArray(R.array.AqiGradeColors);
		String[] aqiGradeDescriptions = context.getResources().getStringArray(R.array.AqiGradeDescriptions);
		
		for (int i = 0; i < aqiGrades.length; i++) {
			AQI_GRADES[i] = aqiGrades[i];
		}
		for (int i = 0; i < aqiGradeColors.length; i++) {
			AQI_GRADE_COLORS[i] = aqiGradeColors[i];
		}
		for (int i = 0; i < aqiGradeDescriptions.length; i++) {
			AQI_GRADE_DESCRIPTIONS[i] = aqiGradeDescriptions[i];
		}
	}
	
	public static int getGradeColorId(int grade) {
		/*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
		for (int i = 0; i < AQI_GRADES.length; i++) {
			if (grade <= AQI_GRADES[i]) {
				return AQI_GRADE_COLORS[i];
			}
		}
		//if hazardous
		return AQI_GRADE_COLORS[5];
	}
	
	public static String getGradeDescription(int grade) {
		/*
		<item>50</item>
        <item>100</item>
        <item>150</item>
        <item>200</item>
        <item>300</item>
		 */
		for (int i = 0; i < AQI_GRADES.length; i++) {
			if (grade <= AQI_GRADES[i]) {
				return AQI_GRADE_DESCRIPTIONS[i];
			}
		}
		//if hazardous
		return AQI_GRADE_DESCRIPTIONS[5];
	}
	
}
