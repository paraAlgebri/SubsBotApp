package com.example.bot.service;

import com.example.bot.dal.entity.TypeSubscribe;
import com.example.bot.rest.model.Subscriber;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionFactory {

    public Subscription create(Subscriber subscriber) {
        Long defaultDays = subscriber.getTypeSubscribe() == TypeSubscribe.DEMO ? 3L : 30L;
        return new BasicSubscription(subscriber, defaultDays);
    }

    public Subscription createWithCustomDays(Subscriber subscriber, Long days) {
        return new BasicSubscription(subscriber, days);
    }
}
