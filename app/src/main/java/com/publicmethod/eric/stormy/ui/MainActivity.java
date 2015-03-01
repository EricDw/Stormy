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
import com.publicmethod.eric.stormy.utils.CurrentWeather;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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
    @InjectView(R.id.locationLabel)
    TextView mLocationLabel;
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
    private double mLatitude;
    private double mLongitude;
    private CurrentWeather mCurrentWeather;
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

        String forcastUrl = "https://api.forecast.io/forecast/" + getApiKey() + "/" + latitude + "," + longitude;
        if (isNetworkAvailable()) {

            toggleRefresh();

            mOkHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forcastUrl)
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
                            mCurrentWeather = getCurrentDetails(jsonData);
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
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumididtyValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryText.setText(mCurrentWeather.getSummary());
        mLocationLabel.setText("Where you are standing!");
        mPrecipLabel.setText(mCurrentWeather.getPrecipType());


        Drawable icon = getResources().getDrawable(mCurrentWeather.getIconId());
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

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forcast = new JSONObject(jsonData);
        String timezone = forcast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);


        JSONObject currently = forcast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTimeZone(timezone);

        if (currently.getString("precipType") != null) {
            currentWeather.setPrecipType(currently.getString("precipType").toUpperCase());
        }
        Log.i(TAG, "From JSON: " + currentWeather.getFormattedTime());


        return currentWeather;
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
