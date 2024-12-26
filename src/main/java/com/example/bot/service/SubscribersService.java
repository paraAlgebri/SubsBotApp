package com.example.bot.service;

import com.example.bot.dal.entity.SubscriberDal;
import com.example.bot.dal.entity.TypeSubscribe;
import com.example.bot.dal.repository.SubscriberRepository;
import com.example.bot.rest.model.Subscriber;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscribersService {

    private final SubscriberRepository subscriberRepository;
    private final MapperFacade mapperFacade;
    private final SubscriptionFactory subscriptionFactory;

    @Autowired
    public SubscribersService(SubscriberRepository subscriberRepository, MapperFacade mapperFacade, SubscriptionFactory subscriptionFactory) {
        this.subscriberRepository = subscriberRepository;
        this.mapperFacade = mapperFacade;
        this.subscriptionFactory = subscriptionFactory;
    }

    public Subscriber add(Subscriber subscriber) {
        Subscription subscription = subscriptionFactory.create(subscriber);
        SubscriberDal subscriberDal = subscription.toDalEntity();
        subscriberDal = subscriberRepository.save(subscriberDal);
        return mapperFacade.map(subscriberDal, Subscriber.class);
    }

    public Subscriber addByCertainAmountOfDays(Subscriber subscriber, Long days) {
        Subscription subscription = subscriptionFactory.createWithCustomDays(subscriber, days);
        SubscriberDal subscriberDal = subscription.toDalEntity();
        subscriberDal = subscriberRepository.save(subscriberDal);
        return mapperFacade.map(subscriberDal, Subscriber.class);
    }

    public void update(Long id, LocalDateTime finishDate) {
        subscriberRepository.updateSubscriber(finishDate, id);
    }

    public boolean checkSub(Long telegramId, List<Subscriber> subscribers) {
        return subscribers.stream()
                .anyMatch(subscriber -> Objects.equals(subscriber.getTelegramId(), telegramId));
    }

    public void disable(Long id) {
        Optional<SubscriberDal> optionalSubscriber = subscriberRepository.findById(id);
        optionalSubscriber.ifPresent(subscriber -> subscriberRepository.updateEnableSubscriber(false, id));
        if (!optionalSubscriber.isPresent()) {
            throw new RuntimeException("Subscriber not found");
        }

    }

    @Transactional
    public List<Subscriber> getExpired() {
        return mapSubscriberList(subscriberRepository.findAllExpired());
    }

    @Transactional
    public List<Subscriber> getNotExpired() {
        return mapSubscriberList(subscriberRepository.findAllNotExpired());
    }

    public List<Subscriber> getExpiredWithinDays(double days, TypeSubscribe typeSubscribe) {
        return mapSubscriberList(subscriberRepository.findExpiringWithinDays(days, typeSubscribe));
    }

    public void remove(Long id) {
        subscriberRepository.deleteById(id);
    }

    public boolean isDemoAccess(Long chatId) {
        return subscriberRepository.findByTelegramIdAndTypeSubscribe(chatId, TypeSubscribe.DEMO) == null;
    }

    public boolean isInDb(Long chatId) {
        return subscriberRepository.findByTelegramIdAndTypeSubscribe(chatId, TypeSubscribe.FULL) == null;
    }

    public Subscriber getSubscriberByTelegramId(Long telegramId, TypeSubscribe typeSubscribe) {
        SubscriberDal subscriberDal = subscriberRepository.findByTelegramIdAndTypeSubscribe(telegramId, typeSubscribe);
        return mapperFacade.map(subscriberDal, Subscriber.class);
    }

    private List<Subscriber> mapSubscriberList(List<SubscriberDal> dalList) {
        return dalList.stream()
                .map(dal -> mapperFacade.map(dal, Subscriber.class))
                .collect(Collectors.toList());
    }
}

