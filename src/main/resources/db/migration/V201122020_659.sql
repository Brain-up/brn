create table if not exists sub_group
(
    id                 bigint not null
        constraint sub_group_pkey
            primary key,
    exercise_type      varchar(255),
    level              integer,
    name               varchar(255),
    picture            varchar(255),
    template           varchar(255),
    description        varchar(255),
    exercise_series_id bigint
        constraint fkinbeu5e187sfgvxjpk0cc0ils
            references series,
    constraint ukpnpqa2j0bhrehrn7ocfo57lg7
        unique (name, level)
);

alter table exercise_group add column locale varchar(10) not null;

alter table series add column type varchar(30) not null;

alter table exercise drop column exercise_type;
alter table exercise drop column description;
alter table exercise drop column exercise_series_id;
alter table exercise add column sub_group_id  bigint
        constraint fkot3isl5pnpkqc8mwv0gwc98n7
            references sub_group;