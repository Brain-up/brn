create table if not exists audiometry_task_resources
(
    audiometry_task_id bigint not null
        constraint fk9vweip61bewch1w80e8hwl35q
            references audiometry_task,
    resource_id        bigint not null
        constraint fkp32gf05b74rt0soaaitcv58rk
            references resource,
    constraint audiometry_task_resources_pkey
        primary key (audiometry_task_id, resource_id)
);