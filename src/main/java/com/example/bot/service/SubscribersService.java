package com.example.bot.service;

import com.example.bot.dal.entity.SubscriberDal;
import com.example.bot.dal.entity.TypeSubscribe;
import com.example.bot.dal.repository.SubscriberRepository;
import com.example.bot.rest.model.Subscriber;
import lombok.Data;
import ma.glasnost.orika.MapperFacade;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
@Data
public class SubscribersService {

    private final SubscriberRepository subscriberRepository;
    private final MapperFacade mapperFacade;



    public Subscriber add(Subscriber subscriber) {

        SubscriberDal subscriberDal = new SubscriberDal();
        subscriberDal.setTelegramId(subscriber.getTelegramId());
        subscriberDal.setName(subscriber.getName());
        subscriberDal.setLogin(subscriber.getLogin());
        subscriberDal.setStartDate(subscriber.getStartDate());
        subscriberDal.setTypeSubscribe(subscriber.getTypeSubscribe());
        subscriberDal.setEnable(true);

        if (subscriber.getTypeSubscribe().equals(TypeSubscribe.DEMO)) {
            subscriberDal.setFinishDate(subscriber.getStartDate().plusDays(3));
        } else if (subscriber.getTypeSubscribe().equals(TypeSubscribe.FULL)) {
            subscriberDal.setFinishDate(subscriber.getStartDate().plusDays(30));
        }

        subscriberDal = subscriberRepository.save(subscriberDal);


        return mapperFacade.map(subscriberDal, Subscriber.class);
    }

    public Subscriber addByCertainAmountOfDays(Subscriber subscriber, Long days) {

        SubscriberDal subscriberDal = new SubscriberDal();
        subscriberDal.setTelegramId(subscriber.getTelegramId());
        subscriberDal.setName(subscriber.getName());
        subscriberDal.setLogin(subscriber.getLogin());
        subscriberDal.setStartDate(subscriber.getStartDate());
        subscriberDal.setTypeSubscribe(subscriber.getTypeSubscribe());
        subscriberDal.setEnable(true);
        subscriberDal.setFinishDate(subscriber.getStartDate().plusDays(days));

        subscriberDal = subscriberRepository.save(subscriberDal);

        return mapperFacade.map(subscriberDal, Subscriber.class);
    }


    public void update(Long id, LocalDateTime finishdate) {
        subscriberRepository.updateSubscriber(finishdate, id);

    }

    public boolean checkSub(Long telegramId, List<Subscriber> subscribers) {
        for (Subscriber subscriber : subscribers) {
            if (Objects.equals(subscriber.getTelegramId(), telegramId)) {
                return true;
            }
        }
        return false;
    }

    public void disable(Long id) throws Exception {
        Optional<SubscriberDal> subscriber = subscriberRepository.findById(id);

        if (!subscriber.isPresent()) {
            throw new Exception("Subscriber not found");
        }

        subscriberRepository.updateEnableSubscriber(false, id);

    }

    @Transactional
    public List<Subscriber> getExpired() {
        return mapperFacade.mapAsList(subscriberRepository.findAllExpired(), Subscriber.class);
    }

    @Transactional
    public List<Subscriber> getNotExpired() {
        return mapperFacade.mapAsList(subscriberRepository.findAllNotExpired(), Subscriber.class);
    }


    @Transactional
    public List<Subscriber> getExpiredIn1Day() {
        return mapperFacade.mapAsList(subscriberRepository.findAllIn1DayExpired(), Subscriber.class);
    }

    public List<Subscriber> getExpiredIn3Days() {
        return mapperFacade.mapAsList(subscriberRepository.findAllIn3DaysExpired(), Subscriber.class);
    }
    @Transactional
    public List<Subscriber> getExpiredIn5Days() {
        return mapperFacade.mapAsList(subscriberRepository.findAllIn5DaysExpired(), Subscriber.class);
    }




    public void remove(Long id) {
        subscriberRepository.deleteById(id);
    }

    public boolean isDemoAccess(Long chatId) {
        SubscriberDal result = subscriberRepository.findByTelegramIdAndTypeSubscribe(chatId, TypeSubscribe.DEMO);

        return result == null;
    }

    public boolean isInDb(Long chatId) {
        SubscriberDal result = subscriberRepository.findByTelegramIdAndTypeSubscribe(chatId, TypeSubscribe.FULL);

        return result == null;
    }

    //get subscriber by telegram id
    public Subscriber getSubscriberByTelegramId(Long telegramId, TypeSubscribe typeSubscribe) {
        SubscriberDal subscriberDal = subscriberRepository.findByTelegramIdAndTypeSubscribe(telegramId,
                typeSubscribe);
        return mapperFacade.map(subscriberDal, Subscriber.class);
    }
}
