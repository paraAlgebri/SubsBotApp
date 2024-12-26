package com.example.bot.service;

import com.example.bot.dal.entity.SubscriberDal;
import com.example.bot.rest.model.Subscriber;

public class BasicSubscription implements Subscription {
    private final Subscriber subscriber;
    private final Long customDays;

    BasicSubscription(Subscriber subscriber, Long customDays) {
        this.subscriber = subscriber;
        this.customDays = customDays;
    }

    @Override
    public SubscriberDal toDalEntity() {
        SubscriberDal dal = new SubscriberDal();
        dal.setTelegramId(subscriber.getTelegramId());
        dal.setName(subscriber.getName());
        dal.setLogin(subscriber.getLogin());
        dal.setStartDate(subscriber.getStartDate());
        dal.setTypeSubscribe(subscriber.getTypeSubscribe());
        dal.setEnable(true);
        dal.setFinishDate(subscriber.getStartDate().plusDays(customDays));
        return dal;
    }
}
