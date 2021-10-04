package com.lifedawn.bestweather.retrofit.util;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Call;

public final class RetrofitCallListManager {
	private static Map<Long, CallObj> callObjMap = new HashMap<>();
	
	private RetrofitCallListManager() {}
	
	public static CallObj newCalls() {
		final long callBeginTime = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
		CallObj callObj = new CallObj(callBeginTime);
		callObjMap.put(callBeginTime, callObj);
		return callObj;
	}
	
	public static CallObj getCallObj(long callBeginTime) {
		return callObjMap.get(callBeginTime);
	}
	
	public static void removeCallObj(CallObj callObj) {
		callObj.clear();
		callObjMap.remove(callObj.CALL_BEGIN_TIME);
	}
	
	public static void clear() {
		Set<Map.Entry<Long, CallObj>> entrySet = callObjMap.entrySet();
		
		for (Map.Entry<Long, CallObj> entry : entrySet) {
			entry.getValue().clear();
		}
		
		callObjMap.clear();
	}
	
	public interface CallManagerListener {
		void clear();
	}
	
	public static class CallObj {
		private List<Call<JsonObject>> callList = new ArrayList<>();
		public final long CALL_BEGIN_TIME;
		
		public CallObj(long callBeginTime) {
			this.CALL_BEGIN_TIME = callBeginTime;
		}
		
		
		public void add(Call<JsonObject> call) {
			callList.add(call);
		}
		
		public Call get(Call<JsonObject> call) {
			return callList.get(callList.indexOf(call));
		}
		
		public void remove(Call<JsonObject> call) {
			callList.remove(call);
		}
		
		public void clear() {
			for (Call<JsonObject> call : callList) {
				call.cancel();
			}
			callList.clear();
		}
		
		public boolean isEmpty() {
			return callList.isEmpty();
		}
	}
}