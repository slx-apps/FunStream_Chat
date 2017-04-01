package com.slx.funstream.utils;


import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxBus {
    private final Relay<Object> bus = PublishRelay.create().toSerialized();

    public void send(Object event) {
        bus.accept(event);
    }

    public Observable<Object> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }
}
