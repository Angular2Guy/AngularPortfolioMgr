create sequence mainseq start with 1000 increment by 100;
create table appuser (id bigint identity primary key, username varchar(50), birthdate timestamp, updated_at timestamp, password varchar(255), email_address varchar(100), user_role varchar(20), locked boolean, enabled boolean, uuid varchar(50));
create table symbol (id bigint identity primary key, symbol varchar(15) unique, name varchar(100));
create table dailyquote (id bigint identity primary key,symbol varchar(15), open decimal(12,4), high decimal(12,4), low decimal(12,4), close decimal(12,4), volume bigint, local_day date, symbol_id bigint, foreign key (symbol_id) references symbol(id));
create table intradayquote (id bigint identity primary key, symbol varchar(15), open decimal(12,4), high decimal(12,4), low decimal(12,4), close decimal(12,4), volume bigint, local_date_time timestamp, symbol_id bigint, foreign key (symbol_id) references symbol(id));
create table portfolio (id bigint identity primary key, user_id bigint, name varchar(50), foreign key (user_id) references appuser(id));
create table portfoliotosymbol (id bigint identity primary key, portfolio_id bigint, symbol_id bigint, weight bigint, foreign key (portfolio_id) references portfolio(id), foreign key (symbol_id) references symbol(id));