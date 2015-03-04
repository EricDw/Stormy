package com.publicmethod.eric.stormy.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Owner on 2/24/2015.
 */
public class Current {

    private long mTime;
    private double mTemperature;
    private double mHumidity;
    private double mPrecipChance;
    private String mIcon;
    private String mSummary;
    private String mTimeZone;
    private String mPrecipType = "PRECIPITATION";

    public int getIconId() {
        return Forecast.getIconId(mIcon);
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getFormattedTime() {
        SimpleDateFormat formater = new SimpleDateFormat("h:mm a");
        formater.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime() * 1000);
        String timeString = formater.format(dateTime);
        return timeString;

    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public int getPrecipChance() {
        double precipPrecentage = mPrecipChance * 100;
        return (int) Math.round(precipPrecentage);
    }

    public void setPrecipChance(double precipitation) {
        mPrecipChance = precipitation;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public String getPrecipType() {
        return mPrecipType;

    }

    public void setPrecipType(String precipType) {
        mPrecipType = precipType;

    }
}
