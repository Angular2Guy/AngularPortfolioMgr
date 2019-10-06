create table appuser (id bigint identity primary key, name varchar(50), firstname varchar(50), birthdate date);
insert into appuser (name, firstname, birthdate) values ('Max', 'Smith', now());