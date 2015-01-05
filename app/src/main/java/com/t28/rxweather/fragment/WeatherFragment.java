package com.t28.rxweather.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.t28.rxweather.R;
import com.t28.rxweather.data.model.Coordinate;
import com.t28.rxweather.data.model.MainAttribute;
import com.t28.rxweather.data.model.Weather;
import com.t28.rxweather.data.service.WeatherService;
import com.t28.rxweather.rx.CoordinateEventBus;
import com.t28.rxweather.volley.RequestQueueRetriever;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Observer;
import rx.android.observables.AndroidObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class WeatherFragment extends Fragment {
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

    private WeatherService mWeatherService;

    public WeatherFragment() {
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeatherService = new WeatherService("", RequestQueueRetriever.retrieve());
        AndroidObservable.bindFragment(this, createWeatherObservable())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Weather>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable cause) {
                        onFailure(cause);
                    }

                    @Override
                    public void onNext(Weather result) {
                        onSuccess(result);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_weather, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.reset(this);
        super.onDestroyView();
    }

    private void onSuccess(Weather result) {
        if (isDetached()) {
            return;
        }

        final Activity activity = getActivity();
        Toast.makeText(activity, result.toString(), Toast.LENGTH_SHORT).show();

        final MainAttribute attribute = result.getAttribute();
        mTemperatureView.setText(String.valueOf((int) attribute.getTemperature()));
        mMinTemperatureView.setText(String.valueOf((int) attribute.getMinTemperature()));
        mMaxTemperatureView.setText(String.valueOf((int) attribute.getMaxTemperature()));

        mPressureView.setText(String.valueOf((int) attribute.getPressure()));
        mHumidityView.setText(String.valueOf((int) attribute.getHumidity()));
    }

    private void onFailure(Throwable cause) {
        if (isDetached()) {
            return;
        }

        final Activity activity = getActivity();
        Toast.makeText(activity, cause.toString(), Toast.LENGTH_SHORT).show();
    }

    private Observable<Weather> createWeatherObservable() {
        return CoordinateEventBus.Retriever.retrieve()
                .getEventStream()
                .flatMap(new Func1<Coordinate, Observable<Weather>>() {
                    @Override
                    public Observable<Weather> call(Coordinate coordinate) {
                        return mWeatherService.findWeather(coordinate);
                    }
                });
    }
}
