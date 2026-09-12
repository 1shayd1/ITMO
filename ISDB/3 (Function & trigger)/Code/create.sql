set search_path to s502467, public;

create table Band(
    id smallserial primary key,
    name varchar(50) not null,
    area text not null
);

create table Invention(
    id smallserial primary key,
    name varchar(50) not null,
    weight float not null
);

create table Action(
    id smallserial primary key,
    name varchar(50) not null,
    start timestamp,
    finish timestamp,
    constraint start_less_finish check (start <= finish)
);

create table Pithecanthropus(
    id smallserial primary key,
    name varchar(50) not null,
    band_id smallint references Band(id),
    weight float not null,
    height smallint not null,
    sex varchar(6) not null
);

create table Invention_type(
    id smallserial primary key,
    invention_id smallint references Invention(id),
    applicability text not null,
    material text not null,
    suggested_sex_of_user varchar(6) not null
);

create table Invention_pithec_action(
    id smallserial primary key,
    pithec_id smallint references Pithecanthropus(id),
    invention_id smallint references Invention(id),
    action_id smallint references Action(id)
);

create table Symboled_band_invention(
    id smallserial primary key,
    type varchar(50),
    band_id smallint references Band(id),
    invention_id smallint references Invention(id)
);

create table ipaLog(
    id smallserial primary key,
    deleted_action smallint,
    deleted_pithec smallint,
    deleted_invention smallint,
    deleted_pithec_sex varchar(6) not null,
    suggested_sex varchar(6) not null,
    reason text
);
