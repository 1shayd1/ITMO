set search_path to s502467, public;

create table Band(
    id smallserial primary key ,
    name varchar(50) not null,
    area text not null
);

create table Invention(
    id smallserial primary key,
    name varchar(50) not null,
    weight float not null
);

create table Pithecanthropus(
    id smallserial primary key,
    name varchar(50) not null,
    band_id smallint,
    weight float not null,
    height smallint not null
);

create table Action(
    id smallserial primary key,
    name varchar(50) not null,
    start timestamp,
    finish timestamp constraint start_less_finish check (start <= finish)
);

create table Pithec_action(
    pithec_id smallint references Pithecanthropus(id),
    action_id smallint references Action(id)
);

create table Invention_in_action(
    action_id smallint references Action(id),
    invention_id smallint references Invention(id)
);

create table Invention_type(
    invention_id smallint references Invention(id),
    applicability text not null,
    material text not null,
    suggested_sex_of_user varchar(6)
);

create table Symboled_band_invention(
    id smallserial primary key,
    type varchar(50),
    band_id smallint references Band(id),
    invention_id smallint references Invention(id)
);

