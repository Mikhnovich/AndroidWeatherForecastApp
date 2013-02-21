package com.mikhnovich.weatherforecast;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class CitiesChoosingActivity extends Activity implements OnItemClickListener {
	
	public static final String LOG_TAG = "CitiesChoosingActivity";
	
	protected ListView lvCitiesSelecter;
	protected ArrayAdapter<CharSequence> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cities_choosing);
		
		// Finding views
		lvCitiesSelecter = (ListView) findViewById(R.id.lvCitiesSelecter);
		
		// Tuning everything up
		lvCitiesSelecter.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
	    adapter = new ArrayAdapter<CharSequence>( this, 
	    									      android.R.layout.simple_list_item_multiple_choice, 
	    									      WeatherInfoDownloader.hardcodedCities );
	    lvCitiesSelecter.setAdapter(adapter);

	    // Setting up click actions
	    lvCitiesSelecter.setOnItemClickListener(this);
	    
	    // Setting some items checked
	    boolean isInPreferences;
	    for (int i = 0; i < WeatherInfoDownloader.hardcodedCities.length; ++i) {

			isInPreferences = CurrentWeatherListActivity.appPreferences.getBoolean( WeatherInfoDownloader.hardcodedCities[i], false );
			lvCitiesSelecter.setItemChecked( i, isInPreferences );
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		// We will need this to put information to preferences
		Editor editor = CurrentWeatherListActivity.appPreferences.edit();
		
		// "This API is a mess"
		SparseBooleanArray sbArray = lvCitiesSelecter.getCheckedItemPositions();
		
		// Adding or removing city to preferences
	    editor.putBoolean( WeatherInfoDownloader.hardcodedCities[position], sbArray.get(position) );
	    editor.commit();
    }
}
