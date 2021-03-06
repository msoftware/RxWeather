package com.t28.rxweather.volley;

import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class RxSupport {
    private RxSupport() {
    }

    public static <T> Observable<T> newRequest(@NonNull final RequestQueue queue,
                                               @NonNull final ListenableRequest<T> request) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                final RequestFuture<T> future = RequestFuture.newFuture();
                subscriber.add(Subscriptions.from(future));

                request.setListener(future);
                request.setErrorListener(future);
                future.setRequest(queue.add(request));

                try {
                    final T result = future.get(request.getTimeoutMs(), TimeUnit.MILLISECONDS);
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    subscriber.onNext(result);
                    subscriber.onCompleted();
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    subscriber.onError(e);
                }
            }
        });
    }
}
