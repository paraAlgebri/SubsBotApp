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

// Основний інтерфейс SubscriberRepository
@Repository
public interface SubscriberRepository extends CrudRepository<SubscriberDal, Long>,
        JpaSpecificationExecutor<SubscriberDal>, CustomSubscriberRepository {

    SubscriberDal findByTelegramIdAndTypeSubscribe(Long chatId, TypeSubscribe type);

    SubscriberDal findByTelegramId(Long chatId);

    List<SubscriberDal> findAllByTypeSubscribe(TypeSubscribe typeSubscribe);

    @Query("select s from SubscriberDal s where s.finishDate <= CURRENT_TIMESTAMP")
    List<SubscriberDal> findAllExpired();

    @Query("select s from SubscriberDal s where s.finishDate >= CURRENT_TIMESTAMP")
    List<SubscriberDal> findAllNotExpired();

    @Transactional
    @Modifying
    @Query("update SubscriberDal s set s.finishDate = :finishDate where s.id = :id")
    void updateSubscriber(LocalDateTime finishDate, Long id);

    @Transactional
    @Modifying
    @Query("update SubscriberDal s set s.enable = :enable where s.id = :id")
    void updateEnableSubscriber(boolean enable, Long id);
}

