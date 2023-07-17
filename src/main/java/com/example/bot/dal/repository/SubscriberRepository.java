package com.example.bot.dal.repository;

import com.example.bot.dal.entity.SubscriberDal;
import com.example.bot.dal.entity.TypeSubscribe;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface SubscriberRepository extends CrudRepository<SubscriberDal, Long>,
        JpaSpecificationExecutor<SubscriberDal> {

    SubscriberDal findByTelegramIdAndTypeSubscribe(Long chatId, TypeSubscribe type);

    SubscriberDal findByTelegramId(Long chatId);

    List<SubscriberDal> findAllByTypeSubscribe(TypeSubscribe typeSubscribe);



    @Query("select s from SubscriberDal s where s.finishDate <= CURRENT_TIMESTAMP")
    List<SubscriberDal> findAllExpired();

    @Query("select s from SubscriberDal s where s.finishDate >= CURRENT_TIMESTAMP")
    List<SubscriberDal> findAllNotExpired();

    //find all subscribers with finish date in 3 days
    @Query("select s from SubscriberDal s where s.finishDate <= current_date + 5 and s.finishDate > current_date + 4" +
            " and s.typeSubscribe = 'FULL'")
    List<SubscriberDal> findAllIn5DaysExpired();

    @Query("select s from SubscriberDal s where s.finishDate <= current_date + 3 and s.finishDate > current_date + 2" +
            " and  s.typeSubscribe = 'FULL'")
    List<SubscriberDal> findAllIn3DaysExpired();

    @Query("select s from SubscriberDal s where s.finishDate <= current_date + 1 and s.finishDate > current_date " +
            " and  s.typeSubscribe = 'FULL'")
    List<SubscriberDal> findAllIn1DayExpired();
    //write query to update subscriber info on postgres
    @Transactional
    @Modifying
    @Query("update SubscriberDal s set s.finishDate = ?1 where  s.id = ?2")
    void updateSubscriber( LocalDateTime finishDate, Long id);

    @Transactional
    @Modifying
    @Query("update SubscriberDal s set s.enable = ?1 where  s.id = ?2")
    void updateEnableSubscriber( boolean enable, Long id);


}
