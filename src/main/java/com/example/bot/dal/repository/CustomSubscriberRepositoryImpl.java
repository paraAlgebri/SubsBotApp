package com.example.bot.dal.repository;

import com.example.bot.dal.entity.SubscriberDal;
import com.example.bot.dal.entity.TypeSubscribe;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

// Реалізація CustomSubscriberRepository
@Repository
public class CustomSubscriberRepositoryImpl implements CustomSubscriberRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SubscriberDal> findExpiringWithinDays(double days, TypeSubscribe typeSubscribe) {
        String query = "select s from SubscriberDal s " +
                "where s.finishDate <= current_date + cast(:days as integer) " +
                "and s.finishDate > current_date " +
                "and s.typeSubscribe = :typeSubscribe";

        return entityManager.createQuery(query, SubscriberDal.class)
                .setParameter("days", days)
                .setParameter("typeSubscribe", typeSubscribe)
                .getResultList();
    }

}
