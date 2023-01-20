package com.example.project6;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private float lat = 0;
    private float lon = 0;
    private final String key = "5d9875b327d5f66617c5b4fd60e5fea5";
    private final String API = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude="
            + lon + "&hourly=temperature_2m,relativehumidity_2m,surface_pressure,windspeed_10m&daily=temperature_2m_max,temperature_2m_min&current_weather=true&temperature_unit=fahrenheit&windspeed_unit=mph&timeformat=unixtime&timezone=America%2FChicago";

   // "https://api.openweathermap.org/data/2.5/onecall?lat="
        //    &lon=
    private RequestQueue rQueue;
    private TextView currTempText;
    private TextView highTempText;
    private TextView lowTempText;
    private TextView humidityText;
    private TextView windSpeedText;
    private TextView pressureText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rQueue = Volley.newRequestQueue(this);
        currTempText = (TextView) findViewById(R.id.currTempText);
        humidityText = (TextView) findViewById(R.id.humidityText);
        pressureText = (TextView) findViewById(R.id.pressureText);
        windSpeedText = (TextView) findViewById(R.id.windSpeedText);

    }


    public void setHourly(int[] temps, int[] hours, int nextHours) {
        for (int i = 0; i < nextHours; i++) {
            String tempName = "hour" + (i + 1);
            String hourName = "hour" + (i + 1) + "temp";
            int hid = getResources().getIdentifier(hourName, "id", getPackageName());
            int tid = getResources().getIdentifier(tempName, "id", getPackageName());

            TextView hour = (TextView) findViewById(hid);
            TextView temp = (TextView) findViewById(tid);
            int currHour = hours[i];
            int currTemp = temps[i];

            String period;
            if (currHour >= 12) {
                period = "PM";
                if (currHour > 12) {
                    currHour -= 12;
                }
            } else {
                period = "AM";
                if (currHour == 0) {
                    currHour = 12;
                }
            }
            hour.setText(String.valueOf(currHour) + " " + period);
            temp.setText(String.valueOf(currTemp) + " °F");
        }
    }


    public void setWeek(String[] dates, int[] max, int[] min, int days) {
        for (int i = 0; i < days; i++) {
            String date = "day" + i;
            String dateTemp = "day" + i + "temp";
            int dayID = getResources().getIdentifier(date, "id", getPackageName());
            int tempID = getResources().getIdentifier(dateTemp, "id", getPackageName());

            TextView day = (TextView) findViewById(dayID);
            TextView dayTemp = (TextView) findViewById(tempID);
            String currDay = dates[i];
            String hiLoTemp = "H: " + max[i] + "°F" + "\n" + "L: " + min[i] + " °F";

            day.setText(currDay);
            dayTemp.setText(hiLoTemp);
        }
    }


    public void getWeatherDetails() {
        String url = API;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // current
                            long unixOffset = response.getLong("utc_offset_seconds");
                            JSONObject currentDetails = response.getJSONObject("current_weather");
                            long currTime = currentDetails.getLong("time");
                            int currTemp = (int) Math.round(currentDetails.getDouble("temperature")); // in Fahrenheit
                            double windSpeed = currentDetails.getDouble("windspeed");  // mph
                            currTempText.setText(String.valueOf(currTemp) + " °F");
                            windSpeedText.setText(String.valueOf(windSpeed) + " mph");

                            // hourly
                            int nextHours = 10;
                            int startingTime = 0;
                            int[] hourlyTemps = new int[nextHours];
                            int[] hours = new int[nextHours];
                            JSONObject hourlyArray = response.getJSONObject("hourly");
                            JSONArray timeArray = hourlyArray.getJSONArray("time");
                            JSONArray tempArray = hourlyArray.getJSONArray("temperature_2m");
                            JSONArray humidArray = hourlyArray.getJSONArray("relativehumidity_2m");
                            JSONArray pressureArray = hourlyArray.getJSONArray("surface_pressure");

                            int timeAsize = timeArray.length();
                            for(int j = 0; j<timeAsize; j++){
                                long startTime = currTime;
                                Integer temptime = (Integer) timeArray.get(j);
                                long arraytime = temptime;
                                if(startTime == arraytime){ //finds starting index
                                    startingTime = j;
                                }
                            }
                            int temps = 0;
                            for (int i = startingTime; i < startingTime+10; i++) {

                                Integer timeDetails = (Integer) timeArray.get(i); //time
                                double tempDetails = (double) tempArray.get(i); //temp
                                Integer humidDetails = (Integer) humidArray.get(i); //humid
                                double pressureDetails = (double) pressureArray.get(i); //pressure
                                long unixHourlyTime = timeDetails;
                                hourlyTemps[temps] = (int) Math.round(tempDetails);
                                if(unixHourlyTime == currTime){
                                    humidityText.setText(String.valueOf(humidDetails + "%"));
                                    pressureText.setText(String.valueOf(pressureDetails + " hPa"));
                                }
                                hours[temps] = (int) Math.floor(((unixHourlyTime + unixOffset) % 86400) / 3600);
                                temps++;
                            }
                            setHourly(hourlyTemps, hours, nextHours);

                            // week
                            int days = 6;
                            JSONObject weekArray = response.getJSONObject("daily");
                            JSONArray timeArrayW = weekArray.getJSONArray("time");
                            JSONArray tempHArrayW = weekArray.getJSONArray("temperature_2m_max");
                            JSONArray tempLArrayW = weekArray.getJSONArray("temperature_2m_min");
                            int[] weekMax = new int[days];
                            int[] weekMin = new int[days];
                            String[] dates = new String[days];
                            for (int i = 0; i < days; i++) {
                                Integer timeDetailsW = (Integer) timeArrayW.get(i);
                                long unixWeekTime = timeDetailsW;
                                java.util.Date date = new java.util.Date(unixWeekTime*1000);
                                dates[i] = date.toString().substring(4, 10);
                                double tempHDetails = (double) tempHArrayW.get(i);
                                double tempLDetails = (double) tempLArrayW.get(i);

                                int maxTemp = (int) Math.round(tempHDetails);
                                int minTemp = (int) Math.round(tempLDetails);
                                weekMax[i] = maxTemp;
                                weekMin[i] = minTemp;
                            }
                            setWeek(dates, weekMax, weekMin, days);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

//                    private void findTimeInd(double currTime) {
//
//                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        rQueue.add(request);
    }

    public void onClick(View view) {
        getWeatherDetails();
    }


    public void SendLocationOnclick(View view) {
        Button longButton = (Button) view;
        longButton.setText("sent!");

        Log.i("UI Event Handler", "The OK button was tapped"); //tells when button is tapped in Logcat

        EditText editLatitude = (EditText) findViewById(R.id.editTextLatitude);
        EditText editLongitude = (EditText) findViewById(R.id.editTextLongitude);//gets object of both texts ^
        TextView Location = (TextView) findViewById(R.id.Location);
        lat = Float.parseFloat(editLatitude.getText().toString());
        lon = Float.parseFloat(editLongitude.getText().toString());
        Location.setText(editLatitude.getText()+","+editLongitude.getText()); //swaps the text put into the editText into the title

        getWeatherDetails();//starts the data retrieval
    }
}
