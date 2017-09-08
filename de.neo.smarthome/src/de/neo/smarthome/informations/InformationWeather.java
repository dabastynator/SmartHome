package de.neo.smarthome.informations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.OnLoad;
import de.neo.persist.annotations.Persist;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryBean;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryWeather;
import de.neo.smarthome.api.IWebInformationUnit.InformationEntryWeather.WeatherSun;
import de.neo.smarthome.cronjob.CronScheduler;

@Domain()
public class InformationWeather extends InformationUnit {

	public static String Key = "InformationWeather";
	private static String TenMinutes = "0,10,20,30,40,50 * * * *";

	@Persist(name = "token")
	private String mToken;

	@Persist(name = "query")
	private String mQuery;

	private InformationEntryWeather mWeather;

	public InformationWeather() {
		CronScheduler scheduler = CronScheduler.getInstance();
		try {
			scheduler.scheduleJob(new Runnable() {
				@Override
				public void run() {
					updateWeather();
				}
			}, TenMinutes);
		} catch (ParseException e) {
			RemoteLogger.performLog(LogPriority.ERROR, "Cant schedule weather updater", this.toString());
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	@OnLoad
	public void updateWeather() {
		String url = "http://api.openweathermap.org/data/2.5/weather?q=" + mQuery + "&appid=" + mToken;
		try {
			JSONObject json = readJsonFromUrl(url);
			InformationEntryWeather weather = new InformationEntryWeather();

			double temp = json.getJSONObject("main").getDouble("temp");
			weather.mCelsius = temp - 273.15;

			long now = (long) Math.floor(System.currentTimeMillis() / 1000);
			long sunset = json.getJSONObject("sys").getLong("sunset");
			long sunrise = json.getJSONObject("sys").getLong("sunrise");
			weather.mDayNight = (now > sunrise && now < sunset) ? WeatherSun.Day : WeatherSun.Night;

			weather.mClouds = json.getJSONObject("clouds").getInt("all");

			JSONArray array = json.getJSONArray("weather");
			if (array.length() > 0) {
				String id = String.valueOf(array.getJSONObject(0).getInt("id"));
				weather.mRain = id.startsWith("5");
				weather.mSnow = id.startsWith("6");
			}

			mWeather = weather;
		} catch (IOException | JSONException e) {
			RemoteLogger.performLog(LogPriority.ERROR,
					"Can't update weather: " + e.getClass().getSimpleName() + " " + e.getMessage(), this.toString());
		}
	}

	@Override
	public String getKey() {
		return "weather";
	}

	@Override
	public String getDescription() {
		return "Get current weather basec on openweathermap.org";
	}

	@Override
	public InformationEntryBean getInformationEntry() {
		return mWeather;
	}

}
