create sequence mainseq start with 1000 increment by 100;
create table appuser (id bigint identity primary key, name varchar(50), firstname varchar(50), birthdate date);