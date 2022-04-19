insert into appuser (username, birthdate, updated_at, password, email_address, user_role, locked, enabled, uuid) 
	values ('max', now(), now(), 'pwd', 'sven@gmx.de', 'USERS', false, true, 'uuid');
--insert into dailyquote (symbol, open, high, low, close, volume, day) values ('MSFT',92.7500,94.5000,91.9380,29.8767,46349000,now());