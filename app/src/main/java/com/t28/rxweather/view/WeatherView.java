package com.t28.rxweather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.t28.rxweather.R;
import com.t28.rxweather.data.model.MainAttribute;
import com.t28.rxweather.data.model.Weather;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WeatherView extends LinearLayout {
    @InjectView(R.id.weather_temperature)
    TextView mTemperatureView;
    @InjectView(R.id.weather_min_temperature)
    TextView mMinTemperatureView;
    @InjectView(R.id.weather_max_temperature)
    TextView mMaxTemperatureView;
    @InjectView(R.id.weather_pressure)
    TextView mPressureView;
    @InjectView(R.id.weather_humidity)
    TextView mHumidityView;

    public WeatherView(Context context) {
        super(context);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.inject(this);
    }

    public void update(Weather weather) {
        final MainAttribute attribute = weather.getAttribute();
        mTemperatureView.setText(String.valueOf((int) attribute.getTemperature()));
        mMinTemperatureView.setText(String.valueOf((int) attribute.getMinTemperature()));
        mMaxTemperatureView.setText(String.valueOf((int) attribute.getMaxTemperature()));

        mPressureView.setText(String.valueOf((int) attribute.getPressure()));
        mHumidityView.setText(String.valueOf((int) attribute.getHumidity()));
    }
}
