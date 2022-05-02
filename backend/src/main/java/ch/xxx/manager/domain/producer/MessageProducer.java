package ch.xxx.manager.domain.producer;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.RevokedTokenDto;

public interface MessageProducer {
	void sendLogoutMsg(RevokedTokenDto revokedTokenDto);
	void sendNewUserMsg(AppUserDto appUserDto);
}
