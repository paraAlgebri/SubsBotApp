create table subsciber (
                           id serial primary key,
                           name varchar(255),
                           telegram_id bigint not null ,
                           start_subscribe timestamp ,
                           end_subscribe timestamp ,
                           type_subscribe varchar(50) not null,
                           enable boolean not null default true

)