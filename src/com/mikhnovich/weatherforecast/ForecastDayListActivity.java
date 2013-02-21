package com.mikhnovich.weatherforecast;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;

// In this activity we will show forecast for city chosen by user
public class ForecastDayListActivity extends Activity {
	
	public static final String LOG_TAG = "ForecastDayListActivity";
	public static final String INTENT_PARAM_CITYNUMBER = "cityNumber";
	
	protected int cityNumber;
	protected City city;
	protected ListView lvDaysForecast;
	protected SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forecast_day_list);
		
		// Receiving chosen city's number
        cityNumber = getIntent().getIntExtra(INTENT_PARAM_CITYNUMBER, 0);
        Log.d(LOG_TAG, "cityNumber = " + cityNumber);
        
        // Finding views
     	lvDaysForecast = (ListView) findViewById( R.id.lvDaysForecast );
     	
     	// Shorter label
     	city = WeatherInfoDownloader.cities[ cityNumber ];
		
     	// Packing data into structure
	    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>( city.forecast.length );
	    
	    for (int i = 0; i < city.forecast.length; i++) 
		    data.add( city.forecast[i].getContentsAsMap() );
	    
	    // Connecting everything to a SimpleAdapter
	    String[] from = { City.ATTRIBUTE_NAME_DATE, City.ATTRIBUTE_NAME_TEMPERATURE, City.ATTRIBUTE_NAME_WICON, 
	    				  City.ATTRIBUTE_NAME_WDESRIPTION, City.ATTRIBUTE_NAME_WINDDIR, City.ATTRIBUTE_NAME_WINDSPEED };
	    int[] to = { R.id.tvDayDate, R.id.tvDayTemperature, R.id.ivDayWeather,
	    			 R.id.tvDayWeatherDescription, R.id.tvDayWindDirection, R.id.tvDayWindSpeed };
	    
	    adapter = new SimpleAdapter(this, data, R.layout.lv_day_forecast_item, from, to);
	    lvDaysForecast.setAdapter(adapter);
	    
	    // Set Activity title
	    setTitle("Forecast for " + city.name);
	}

}
