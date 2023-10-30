create table IF NOT EXISTS u_role (
    id serial primary key not null,
    role varchar(2000)
);

create table IF NOT EXISTS person (
    id serial primary key not null,
    login varchar(2000),
    password varchar(2000)
);

create table IF NOT EXISTS persons_roles (
    id serial primary key not null,
    person_id int not null references person(id),
    role_id int not null references u_role(id)
);

create table IF NOT EXISTS room (
    id serial primary key not null,
    name varchar(2000),
    person_id int not null references person(id)
);

create table IF NOT EXISTS message (
    id serial primary key not null,
    message varchar(2000),
    person_id int not null references person(id),
    room_id int not null references room(id)
);

insert into u_role (role) values ('ROLE_ADMIN');
insert into u_role (role) values ('ROLE_ANONYMOUS');
insert into u_role (role) values ('ROLE_USER');
insert into person (login, password) values ('root', '123');
insert into person (login, password) values ('Thomas', '123');
insert into persons_roles (person_id, role_id) values (1, 1);
insert into persons_roles (person_id, role_id) values (2, 3);
insert into room (name, person_id) values ('tasks', 1);
insert into message (message, person_id, room_id) values ('add new model RoleRoom', 1, 1);
insert into message (message, person_id, room_id) values ('ок', 2, 1);