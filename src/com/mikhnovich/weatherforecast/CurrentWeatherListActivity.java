package com.mikhnovich.weatherforecast;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class CurrentWeatherListActivity extends Activity implements OnItemClickListener {
	
	public static String PACKAGE_NAME;
	public static Resources appResources;
	public static SharedPreferences appPreferences;
	
	public static final String LOG_TAG = "CurWetherList Activity";
	public static final String CELSIUS = "\u00B0C";
	public static final String KMPH = "km/h";
	
	ListView lvCurWeather;
	SimpleAdapter adapter;
	
	// Handler is needed to allow net thread call UI thread
	public final Handler mainHandler = new Handler();
	public final Runnable changeLayout = new Runnable() {
        public void run() {
            
        	onCitiesDownloaded();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PACKAGE_NAME = getApplicationContext().getPackageName();
		appResources = getResources();
		appPreferences = getPreferences(MODE_PRIVATE);
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refresh();
	}
	
	public void onCitiesDownloaded() {
		
		Log.d(LOG_TAG, "Looks like weather info was downloaded successfully.");
		
		// Here we continue and build weather list from received information
		
	    // Switching to layout with list
	    setContentView(R.layout.activity_current_weather_list);
		
		// Finding views
		lvCurWeather = (ListView) findViewById( R.id.lvCurWeather );
		
		// Setting up click actions
		lvCurWeather.setOnItemClickListener(this);
		
		// Creating items in the list
		fillCurWeatherList();
	}
	
	protected void fillCurWeatherList() {
		
		// Shorter label
		City[] cities = WeatherInfoDownloader.cities;
		
		// Packing data into structure
	    ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>( cities.length );
	    
	    for (int i = 0; i < cities.length; i++) 
		    data.add( cities[i].getCurWeatherAsMap() );
	    
	    // Connecting everything to a SimpleAdapter
	    String[] from = { City.ATTRIBUTE_NAME_CITYNAME, City.ATTRIBUTE_NAME_TEMPERATURE, 
	    				  City.ATTRIBUTE_NAME_WICON, City.ATTRIBUTE_NAME_WDESRIPTION, 
	    				  City.ATTRIBUTE_NAME_WINDDIR, City.ATTRIBUTE_NAME_WINDSPEED,
	    				  City.ATTRIBUTE_NAME_OBSTIME };
	    int[] to = { R.id.tvCityName, R.id.tvCurTemperature, 
	    			 R.id.ivCurWeather, R.id.tvWeatherDescription, 
	    			 R.id.tvWindDirection, R.id.tvWindSpeed,
	    			 R.id.tvLastObservationTime };

	    adapter = new SimpleAdapter(this, data, R.layout.lv_cur_weather_item, from, to);
	    lvCurWeather.setAdapter(adapter);
	}
	
	protected void refresh() {
		
		// We'll ask user to wait some time while our net thread downloads data
		setContentView(R.layout.activity_please_wait);
				
		// Starting the thread
		WeatherInfoDownloader downloader = new WeatherInfoDownloader(mainHandler, changeLayout);
		downloader.start();
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		// Prepare an Intent and launch ForecastDayListActivity
		Intent intent = new Intent(this, ForecastDayListActivity.class);
    	intent.putExtra(ForecastDayListActivity.INTENT_PARAM_CITYNUMBER, position);
    	
    	startActivity(intent);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_current_weather_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.citiesAddAction) {
			
			Intent intent = new Intent(this, CitiesChoosingActivity.class);
			startActivity(intent);
			
		} else if (item.getItemId() == R.id.refreshAction) {
			
			refresh();
		}
		return true;
	}

}
