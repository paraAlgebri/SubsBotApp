package com.example.buns.dal.entity;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.time.LocalDateTime;

@Transactional
@Entity
@Data
@Table(name = "subscriber")
public class SubscriberDal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_subscribe")
    private LocalDateTime startDate;

    @Column(name = "end_subscribe")
    private LocalDateTime finishDate;

    @Column(name = "name")
    private String name;

    @Column(name = "login")
    private String login;

    @Column(name = "telegram_id")
    private Long telegramId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_subscribe")
    private TypeSubscribe typeSubscribe;

    private Boolean enable;
}