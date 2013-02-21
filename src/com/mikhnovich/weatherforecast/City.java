package com.mikhnovich.weatherforecast;

import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;

// Current weather and forecast for one city
public class City {
	
	public static final String ATTRIBUTE_NAME_DATE = "date";
	
	public static final String ATTRIBUTE_NAME_CITYNAME = "cityname";
	
	public static final String ATTRIBUTE_NAME_OBSTIME = "lastObservatonTime";
	public static final String ATTRIBUTE_NAME_TEMPERATURE = "temperature";
	public static final String ATTRIBUTE_NAME_WDESRIPTION = "weatherDescription";
	public static final String ATTRIBUTE_NAME_WICON = "weatherIcon";
	public static final String ATTRIBUTE_NAME_WINDSPEED = "windspeedKmph";
	public static final String ATTRIBUTE_NAME_WINDDIR = "windDirection";
	
	// Fields
	public String name;
	
	// Current weather
	public String lastObservatonTime;
	public String temperature;
	public String weatherDescription;
	public int weatherCode;
	public String windspeedKmph;
	public String windDirection;
	
	// Forecast for next five days
	public static final int FORECAST_LENGTH = 5;
	
	public Day[] forecast;
	
	public class Day {
		
		public String date;
		public String tempMax, tempMin;
		public String windspeedKmph;
		public String windDirection;
		public String weatherDescription;
		public int weatherCode;
		
		public Map<String, Object> getContentsAsMap() {
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put(ATTRIBUTE_NAME_DATE, date);
			map.put(ATTRIBUTE_NAME_TEMPERATURE, "Temp. from " + tempMin + CurrentWeatherListActivity.CELSIUS + " to " + tempMax + " " + CurrentWeatherListActivity.CELSIUS);
			map.put(ATTRIBUTE_NAME_WDESRIPTION, weatherDescription);
			map.put(ATTRIBUTE_NAME_WICON, getIconResourceByWCode(weatherCode));
			map.put(ATTRIBUTE_NAME_WINDDIR, windDirection);
			map.put(ATTRIBUTE_NAME_WINDSPEED, "Wind: " + windspeedKmph + " " + CurrentWeatherListActivity.KMPH + " |");
			
			return map;
		}
		
	}
	
	// Methods
	
	public Map<String, Object> getCurWeatherAsMap() {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put(ATTRIBUTE_NAME_CITYNAME, name);
		
		map.put(ATTRIBUTE_NAME_OBSTIME, "Last observation time: " + lastObservatonTime);
		map.put(ATTRIBUTE_NAME_TEMPERATURE, temperature + " " + CurrentWeatherListActivity.CELSIUS);
		map.put(ATTRIBUTE_NAME_WDESRIPTION, weatherDescription + " |");
		map.put(ATTRIBUTE_NAME_WICON, getIconResourceByWCode(weatherCode));
		map.put(ATTRIBUTE_NAME_WINDDIR, windDirection);
		map.put(ATTRIBUTE_NAME_WINDSPEED, "Wind: " + windspeedKmph + " " + CurrentWeatherListActivity.KMPH + " |");
		
		return map;
	}
	
	public static int getIconResourceByWCode(int weatherCode) {
		
		// http://www.worldweatheronline.com/feed/wwoConditionCodes.txt
		
		Resources res = CurrentWeatherListActivity.appResources;
		return res.getIdentifier( "w" + weatherCode , "drawable" , CurrentWeatherListActivity.PACKAGE_NAME );
	}
	
	// Constructor
	public City() {
		
		// Initializing forecast array
		forecast = new Day[ FORECAST_LENGTH ];
		for (int i = 0; i < FORECAST_LENGTH; ++i)
			forecast[i] = new Day();
		
	}
	
}
