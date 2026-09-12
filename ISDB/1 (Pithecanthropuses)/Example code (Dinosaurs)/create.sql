create table dino_class(
    id smallserial primary key,
    class text not null
);

create table dino(
    id smallserial primary key,
    name text not null,
    weight float not null,
    height float not null,
    class_id smallint references dino_class(id)
);

create table location(
    id smallserial primary key,
    name text not null,
    obstacles text not null
);

create table fossils(
    id smallserial primary key,
    name text not null,
    location_id smallserial references location(id),
    dino_id smallint references dino(id)
);

create table paleo(
    id smallserial primary key,
    name char(15) not null,
    type text not null
);

create table paleo_actions(
    id smallserial primary key,
    name text not null ,
    "start" timestamp not null ,
    "end" timestamp not null constraint end_less_start check ("end" >= "start"),
    paleo_id smallint references paleo(id)
);



create table discovery(
    id smallserial primary key,
    actions_id smallint references paleo_actions(id),
    fossil_id smallint references fossils(id),
    dollars_gained smallint
)