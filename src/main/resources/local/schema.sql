create sequence mainseq start with 1000 increment by 100;
create table appuser (id bigint identity primary key, name varchar(50), firstname varchar(50), birthdate date);
create table dailyquote (id bigint identity primary key,symbol varchar(15), open decimal(12,4), high decimal(12,4), low decimal(12,4), close decimal(12,4), volume bigint, day date);
create table intradayquote (id bigint identity primary key, symbol varchar(15), open decimal(12,4), high decimal(12,4), low decimal(12,4), close decimal(12,4), volume bigint, localdatetime timestamp);
create table symbol (id bigint identity primary key, symbol varchar(15) unique, name varchar(100));