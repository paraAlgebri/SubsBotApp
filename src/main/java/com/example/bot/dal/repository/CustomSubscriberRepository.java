package com.example.bot.dal.repository;

import com.example.bot.dal.entity.SubscriberDal;
import com.example.bot.dal.entity.TypeSubscribe;

import java.util.List;

// Інтерфейс для кастомних запитів
public interface CustomSubscriberRepository {
    List<SubscriberDal> findExpiringWithinDays(double days, TypeSubscribe typeSubscribe);
}
