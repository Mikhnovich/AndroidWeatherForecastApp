package com.mikhnovich.weatherforecast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.os.Handler;
import android.util.Log;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;

// This class connects to World Weather Online, gets
// weather information as XML, parses XML and returns CityWeather object
public class WeatherInfoDownloader extends Thread {
	
	public static final String LOG_TAG = "WeatherInfoDownloader thread";
	
	public static final String KEY = "1dcfb55ad0103211131902";
	public static final String REQUEST_URL = "http://free.worldweatheronline.com/feed/weather.ashx?format=xml&num_of_days=5&key=" + KEY + "&q=";
	
	// Hardcode
	public static String[] hardcodedCities = { 
										"Tokyo, Japan", 
										"Seoul, South Korea",
										"Mexico, Mexico", 
										"New York, USA", 
										"Mumbai, India", 
										"Jakarta, Indonesia",
										"Sao Paulo, Brazil", 
										"Delhi, India", 
										"Osaka, Japan", 
										"Shanghai, China",
										"Manila, Philippines", 
										"Los Angeles, USA", 
										"Calcutta, India", 
										"Moscow, Russia", 
										"Cairo, Egypt", 
										"Lagos, Nigeria", 
										"Buenos Aires, Argentina", 
										"London, UK", 
										"Beijing, China", 
										"Karachi, Pakistan",
										"Dhaka, Bangladesh", 
										"Rio de Janeiro, Brazil", 
										"Tianjin, China",
										"Paris, France", 
										"Istanbul, Turkey", 
										"Lima, Peru", 
										"Tehran, Iran", 
										"Bangkok, Thailand", 
										"Chicago, USA", 
										"Bogota, Colombia",
										"Hyderabad, India",
										"Chennai, India", 
										"Essen, Germany", 
										"Ho Chi Minh City, Vietnam", 
										"Hangzhou, China",
										"Hong Kong, Hong Kong", 
										"Lahore, Pakistan", 
										"Shenyang, China", 
										"Changchun, China", 
										"Bangalore, India",
										"Harbin, China", 
										"Chengdu, China", 
										"Santiago, Chile", 
										"Guangzhou, China", 
										"Saint Petersburg, Russia", 
										"Kinshasa, DRC", 
										"Baghdad, Iraq", 
										"Jinan, China",
										"Houston, USA", 
										"Toronto, Canada" };
	
	public static String[] cityNameParams = null;
	public static City[] cities = null;
	
	protected Handler returnHandler;
	protected Runnable returnRunnable;
	
	public WeatherInfoDownloader(Handler returnHandler, Runnable returnRunnable) {
		
		super();
		this.returnHandler = returnHandler;
		this.returnRunnable = returnRunnable;
	}
	
	protected static String sanitize(String s) {
		
		return s.replaceAll(Pattern.quote(" "), "+");
	}
	
	protected void getCityNameParams() {
		
		int tmpsize = 0;
		String[] tmp = new String[ hardcodedCities.length ];
		
		for (int i = 0; i < hardcodedCities.length; ++i) {
			// Is this city checked?
			if ( CurrentWeatherListActivity.appPreferences.getBoolean( hardcodedCities[i], false ) ) 
				tmp[ tmpsize++ ] = hardcodedCities[i];
		}
		
		cityNameParams = new String[ tmpsize ];
		for (int i = 0; i < tmpsize; ++i)
			cityNameParams[i] = tmp[i];
		
	}
	
	public void run() {
		Log.d(LOG_TAG, "Starting WeatherInfoDownloader thread!");
		
		// step 1: get city list into cityNameParams
		getCityNameParams();
		Log.d(LOG_TAG, "Getting cities list: <SharedPreferences> got " + cityNameParams.length + " cities.");
		
		// step 2: get weather info for every city
		cities = new City[ cityNameParams.length ];
		for (int i = 0; i < cityNameParams.length; ++i)
			cities[i] = XMLParser.downloadCityWeatherInfo( sanitize( cityNameParams[i] ) );


		// step 3: notify UI thread that we're done
		returnHandler.post(returnRunnable);
	}
	
}

class XMLParser extends DefaultHandler {
	
	public static final String LOG_TAG = "SAX XML Parser";
	String thisElement = "";

	protected boolean stateCurCond = false;
	protected boolean stateDayForecast = false;
	protected int currentDayNumber = 0;
	protected City city = new City();
	
	public static City downloadCityWeatherInfo(String cityNameParam) {
		
		City city = null;
		
		// This code should run in separate thread
		// Here we can use the Internet
		try {
			
		    HttpURLConnection conn = (HttpURLConnection) ( new URL(WeatherInfoDownloader.REQUEST_URL + cityNameParam) ).openConnection();
		    
		    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    	
		    	SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		    	XMLParser myXMLParser = new XMLParser(); 
		    	parser.parse( conn.getInputStream(), myXMLParser );
		        
		    	city = myXMLParser.getResult();
		    }
		    
		} catch(IOException e) {
			
			// stubs
			e.printStackTrace();
			
		} catch(SAXException e) {
			
			e.printStackTrace();
			
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} 
		
		
		return city;
	}
	
	public City getResult() {
		
		return city;
	}

	@Override
	public void startDocument() throws SAXException {
		
		// Log.d(LOG_TAG, "Starting document.");
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		
		if ( qName.equals("current_condition") ) {
			
			stateCurCond = true;
			
		} else if ( qName.equals("weather") ) {
			
			if ( currentDayNumber < City.FORECAST_LENGTH )
				stateDayForecast = true;
			
		}
		
		thisElement = qName;
		// Log.d(LOG_TAG, "Starting element <" + qName + ">.");
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		
		if ( qName.equals("current_condition") ) {
			
			stateCurCond = false;
			
		} else if ( qName.equals("weather") ) {
			
			stateDayForecast = false;
			++currentDayNumber;
			
		}
		
		thisElement = "";
		// Log.d(LOG_TAG, "Finishing element <" + qName + ">.");
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		// Log.d(LOG_TAG, "\"" + (new String(ch, start, length)) + "\", start = " + start + ", length = " + length + ".");
		
		if ( stateCurCond ) {
			
			// Reading current winter conditions in City
			
			if ( thisElement.equals("observation_time") ) {
				
				city.lastObservatonTime = new String(ch, start, length);
				
			} else if ( thisElement.equals("temp_C") ) {
				
				city.temperature = new String(ch, start, length);
				
			} else if ( thisElement.equals("weatherDesc") ) {
				
				city.weatherDescription = new String(ch, start, length);
				
			} else if ( thisElement.equals("windspeedKmph") ) {
				
				city.windspeedKmph = new String(ch, start, length);
				
			} else if ( thisElement.equals("winddir16Point") ) {
				
				city.windDirection = new String(ch, start, length);
				
			} else if ( thisElement.equals("weatherCode") ) {
				
				String tmp = new String(ch, start, length);
				city.weatherCode = Integer.parseInt( tmp );
				
			}
		
		} else if ( stateDayForecast ) {
			
			// Reading forecast for currentDayNumber day
			
			if ( thisElement.equals("date") ) {
				
				try {
					Date date = ( new SimpleDateFormat("yyyy-M-dd") ).parse( new String(ch, start, length) );
					city.forecast[currentDayNumber].date = new SimpleDateFormat("EEEE, dd.MM.yyyy").format(date);
					
				} catch (ParseException e) {
					
					// stub
					e.printStackTrace();
				}
				
			} else if ( thisElement.equals("tempMaxC") ) {
				
				city.forecast[currentDayNumber].tempMax = new String(ch, start, length);
				
			} else if ( thisElement.equals("tempMinC") ) {
				
				city.forecast[currentDayNumber].tempMin = new String(ch, start, length);
				
			} else if ( thisElement.equals("windspeedKmph") ) {
				
				city.forecast[currentDayNumber].windspeedKmph = new String(ch, start, length);
				
			} else if ( thisElement.equals("winddir16Point") ) {
				
				city.forecast[currentDayNumber].windDirection = new String(ch, start, length);
				
			} else if ( thisElement.equals("weatherDesc") ) {
				
				city.forecast[currentDayNumber].weatherDescription = new String(ch, start, length);
				
			} else if ( thisElement.equals("weatherCode") ) {
				
				String tmp = new String(ch, start, length);
				city.forecast[currentDayNumber].weatherCode = Integer.parseInt( tmp );
				
			}
			
		} else if ( thisElement.equals("query") ) {
			
			city.name = new String(ch, start, length);
		}
		
	}

	@Override
	public void endDocument() {
	  
		/* Log.d(LOG_TAG, "Finishing document.");
		
		Log.d(LOG_TAG, "");
		Log.d(LOG_TAG, "--- Results ---");
		
		Log.d(LOG_TAG, "--- --- Current conditions --- ---");
		Log.d(LOG_TAG, "Observation time: " + city.lastObservatonTime);
		Log.d(LOG_TAG, "Temperature: " + city.temperature + " " + CurrentWeatherListActivity.CELSIUS);
		Log.d(LOG_TAG, "Weather decription: " + city.weatherDescription);
		Log.d(LOG_TAG, "Wind speed: " + city.windspeedKmph + " km/h");
		Log.d(LOG_TAG, "Wind direction: " + city.windDirection);
		Log.d(LOG_TAG, "WEATHER CODE: " + city.weatherIcon);
		
		for (int i = 0; i < City.FORECAST_LENGTH; ++i) {
			
			Log.d(LOG_TAG, "--- --- Forecast for day " + i + ", date " + city.forecast[i].date + " --- ---");
			Log.d(LOG_TAG, "Temperature: " + city.forecast[i].tempMin + " - " + city.forecast[i].tempMax + " " + CurrentWeatherListActivity.CELSIUS);
			Log.d(LOG_TAG, "Weather decription: " + city.forecast[i].weatherDescription);
			Log.d(LOG_TAG, "Wind speed: " + city.forecast[i].windspeedKmph + " km/h");
			Log.d(LOG_TAG, "Wind direction: " + city.forecast[i].windDirection);
			Log.d(LOG_TAG, "WEATHER CODE: " + city.forecast[i].weatherIcon);
			
		} */
		
	}
}
