package com.publicmethod.eric.stormy.ui;

import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.publicmethod.eric.stormy.R;
import com.publicmethod.eric.stormy.weather.Current;
import com.publicmethod.eric.stormy.weather.Day;
import com.publicmethod.eric.stormy.weather.Forecast;
import com.publicmethod.eric.stormy.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY = "7d6230957a2c09e2f55ebd39f45d1ada";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public OkHttpClient mOkHttpClient;
    @InjectView(R.id.temperatureLabel)
    TextView mTemperatureLabel;
    @InjectView(R.id.timeLabel)
    TextView mTimeLabel;
    @InjectView(R.id.iconImageView)
    ImageView mIconImageView;
    @InjectView(R.id.humididtyValue)
    TextView mHumididtyValue;
    @InjectView(R.id.precipValue)
    TextView mPrecipValue;
    @InjectView(R.id.summaryText)
    TextView mSummaryText;
    @InjectView(R.id.refreshView)
    ImageView mRefreshView;
    @InjectView(R.id.progressBar)
    ProgressBar mProgressBar;
    @InjectView(R.id.precipLabel)
    TextView mPrecipLabel;
    @InjectView(R.id.locationLabel)
    TextView mLocationLabel;
    private double mLatitude;
    private double mLongitude;
    private Forecast mForecast;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        buildGoogleApiClient();

//        setLatitude(37.8267);
//        setLongitude(-122.423);


        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getForcast(getLatitude(), getLongitude());
            }
        });


    }

    private void getForcast(double latitude, double longitude) {

        String forecastUrl = "https://api.forecast.io/forecast/" + getApiKey() + "/" + latitude + "," + longitude;
        if (isNetworkAvailable()) {

            toggleRefresh();

            mOkHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();


            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError(AlertDialogFragment.TITLE_KEY,
                            getString(R.string.alert_dialog_error_title),
                            AlertDialogFragment.MESSAGE_KEY,
                            getString(R.string.alert_dialog_error_message),
                            AlertDialogFragment.BUTTON_KEY,
                            getString(R.string.alert_dialog_button_positive));
                }

                @Override
                public void onResponse(Response response) throws IOException {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            mForecast = parseForcastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                            Log.v(TAG, jsonData);
                        } else {
                            Log.v(TAG, jsonData);
                            alertUserAboutError(AlertDialogFragment.TITLE_KEY,
                                    getString(R.string.alert_dialog_error_title),
                                    AlertDialogFragment.MESSAGE_KEY,
                                    getString(R.string.alert_dialog_error_message),
                                    AlertDialogFragment.BUTTON_KEY,
                                    getString(R.string.alert_dialog_button_positive));
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }
                }
            });
        } else {
            alertUserAboutError(AlertDialogFragment.TITLE_KEY,
                    getString(R.string.alert_dialog_error_title),
                    AlertDialogFragment.MESSAGE_KEY,
                    getString(R.string.network_connection_message),
                    AlertDialogFragment.BUTTON_KEY,
                    getString(R.string.alert_dialog_button_positive));

        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshView.setVisibility(View.INVISIBLE);
            Log.d(TAG, "bar visible at line 160");
        } else {

            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshView.setVisibility(View.VISIBLE);
            Log.d(TAG, "bar invisible at line 168");
        }

    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText(current.getTemperature() + "");
        mTimeLabel.setText("At " + current.getFormattedTime() + " it will be");
        mHumididtyValue.setText(current.getHumidity() + "");
        mPrecipValue.setText(current.getPrecipChance() + "%");
        mSummaryText.setText(current.getSummary());
        mLocationLabel.setText(current.getTimeZone());
        mPrecipLabel.setText(current.getPrecipType());


        Drawable icon = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(icon);

    }

    private void alertUserAboutError(String titleKey,
                                     String titleValue,
                                     String messageKey,
                                     String messageValue,
                                     String positiveButtonKey,
                                     String positiveButtonValue) {

        Bundle args = new Bundle();
        args.putString(titleKey, titleValue);
        args.putString(messageKey, messageValue);
        args.putString(positiveButtonKey, positiveButtonValue);

        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "error_dialog");
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void handleNewLocation(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        getForcast(mLatitude, mLongitude);
    }

    private Forecast parseForcastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForcast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData)throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];
        
        for (int i = 0; i < data.length(); i++){
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();
            
            day.setSummary(jsonDay.getString("summary"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setTimeZone(timezone);
            
            days[i] = day;
            
        }
        return days;
    }

    private Hour[] getHourlyForcast(String jsonData) throws JSONException {

        JSONObject forcast = new JSONObject(jsonData);
        String timezone = forcast.getString("timezone");
        JSONObject hourly = forcast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        
        Hour[] hours = new Hour[data.length()];
        for (int i = 0; i < data.length(); i++){
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();
            
            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimeZone(timezone);
            
            hours[i] = hour;
        }
        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);


        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setHumidity(currently.getDouble("humidity"));
        current.setPrecipChance(currently.getDouble("precipProbability"));
        current.setTemperature(currently.getDouble("temperature"));
        current.setSummary(currently.getString("summary"));
        current.setTimeZone(timezone);

        try {
            if (currently.getString("precipType").isEmpty()) {
                current.setPrecipType("PRECIPITATION");
            } else {
                current.setPrecipType(currently.getString("precipType").toUpperCase());
                Log.i(TAG, "From JSON: " + current.getFormattedTime());
            }
        }catch (JSONException e){
            Log.e(TAG,e.getMessage());
        }


        return current;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        } else {
            handleNewLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }


}
