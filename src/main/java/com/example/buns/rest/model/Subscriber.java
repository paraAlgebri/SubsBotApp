package com.example.buns.rest.model;

import com.example.buns.dal.entity.TypeSubscribe;
import lombok.Data;

import java.time.LocalDateTime;



@Data
public class Subscriber {

    private Long id;

    private LocalDateTime startDate;

    private LocalDateTime finishDate;

    private String name;

    private String login;

    private Long telegramId;

    private TypeSubscribe typeSubscribe;
}