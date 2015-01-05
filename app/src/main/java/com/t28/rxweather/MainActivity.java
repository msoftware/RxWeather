package com.t28.rxweather;

import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.t28.rxweather.data.model.Coordinate;
import com.t28.rxweather.data.model.Forecast;
import com.t28.rxweather.data.model.Photo;
import com.t28.rxweather.data.model.PhotoSize;
import com.t28.rxweather.data.model.Photos;
import com.t28.rxweather.data.service.FlickerService;
import com.t28.rxweather.data.service.LocationService;
import com.t28.rxweather.data.service.WeatherService;
import com.t28.rxweather.fragment.WeatherFragment;
import com.t28.rxweather.rx.CoordinateEventBus;
import com.t28.rxweather.rx.EventBus;
import com.t28.rxweather.volley.RequestQueueRetriever;

import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends ActionBarActivity {
    private EventBus<Coordinate> mCoordinateEventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_navigation);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "click", Toast.LENGTH_SHORT).show();
            }
        });
        toolbar.setTitle("Tokyo");
        toolbar.setSubtitle("Japan");
        toolbar.inflateMenu(R.menu.menu_main);

        mCoordinateEventBus = CoordinateEventBus.Retriever.retrieve();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.main_weather, new WeatherFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        final LocationService location = new LocationService(manager);

        final RequestQueue queue = RequestQueueRetriever.retrieve();
        final FlickerService flicker = new FlickerService("", queue);
        location.find()
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .subscribe(new Action1<Coordinate>() {
                    @Override
                    public void call(Coordinate coordinate) {
                        mCoordinateEventBus.publish(coordinate);
                    }
                });

        location.find()
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .flatMap(new Func1<Coordinate, Observable<Photos>>() {
                    @Override
                    public Observable<Photos> call(Coordinate coordinate) {
                        return flicker.searchPhotos(coordinate);
                    }
                })
                .compose(new Observable.Transformer<Photos, Photo>() {
                    @Override
                    public Observable<Photo> call(Observable<Photos> source) {
                        return source.map(new Func1<Photos, Photo>() {
                            @Override
                            public Photo call(Photos photos) {
                                return photos.getPhotos().get(0);
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Photo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable cause) {
                        Log.d("TAG", "Thread:" + Thread.currentThread().getName());
                        Log.d("TAG", "onError:" + cause);
                    }

                    @Override
                    public void onNext(Photo result) {
                        Log.d("TAG", "Thread:" + Thread.currentThread().getName());
                        Log.d("TAG", "onNext:" + result.toImageUri(PhotoSize.MEDIUM));
                        final Intent intent = new Intent(Intent.ACTION_VIEW, result.toPhotoUri());
                        MainActivity.this.startActivity(intent);
                    }
                });

        final WeatherService weather = new WeatherService("", queue);
        location.find()
                .subscribeOn(Schedulers.from(AsyncTask.THREAD_POOL_EXECUTOR))
                .flatMap(new Func1<Coordinate, Observable<Forecast>>() {
                    @Override
                    public Observable<Forecast> call(Coordinate coordinate) {
                        return weather.findForecast(coordinate);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Forecast>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable cause) {
                        Log.d("TAG", "Thread:" + Thread.currentThread().getName());
                        Log.d("TAG", "onError:" + cause);
                    }

                    @Override
                    public void onNext(Forecast result) {
                        Log.d("TAG", "Thread:" + Thread.currentThread().getName());
                        Log.d("TAG", "onNext:" + result);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
