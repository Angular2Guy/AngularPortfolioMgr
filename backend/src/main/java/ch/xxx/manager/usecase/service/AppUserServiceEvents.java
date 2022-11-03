/**
 *    Copyright 2019 Sven Loesekann
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package ch.xxx.manager.usecase.service;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.KafkaEventDto;
import ch.xxx.manager.domain.model.dto.RevokedTokenDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.RevokedToken;
import ch.xxx.manager.domain.model.entity.RevokedTokenRepository;
import ch.xxx.manager.domain.producer.EventProducer;
import ch.xxx.manager.usecase.mapping.AppUserMapper;
import ch.xxx.manager.usecase.mapping.RevokedTokenMapper;

@Profile("kafka | prod-kafka")
@Transactional
@Service
public class AppUserServiceEvents extends AppUserServiceBase implements AppUserService {
	private static final long LOGOUT_TIMEOUT = 95L;
	private final EventProducer eventProducer;
	
	public AppUserServiceEvents(AppUserRepository repository, AppUserMapper appUserMapper, RevokedTokenMapper revokedTokenMapper,
			JavaMailSender javaMailSender, RevokedTokenRepository revokedTokenRepository, EventProducer messageProducer,
			PasswordEncoder passwordEncoder, JwtTokenService jwtTokenProvider, AppInfoService myService) {
		super(repository, appUserMapper, javaMailSender, revokedTokenRepository, passwordEncoder, jwtTokenProvider, myService, revokedTokenMapper);
		this.eventProducer = messageProducer;
	}

	@Override
	public void updateLoggedOutUsers() {
		this.updateLoggedOutUsers(LOGOUT_TIMEOUT);
	}
	
	@Override
	public Boolean signin(AppUserDto appUserDto) {
		Optional<AppUser> appUserOpt = super.signin(appUserDto, false, true);
		appUserOpt.ifPresent(myAppUser -> this.eventProducer.sendNewUserMsg(this.appUserMapper.convert(myAppUser)));
		return appUserOpt.isPresent();
	}

	public Boolean signinMsg(AppUserDto appUserDto) {
		return super.signin(appUserDto, true, false).isPresent();
	}
	
	@Override
	public Boolean logout(String bearerStr) {
		Optional<RevokedToken> logoutTokenOpt = this.logoutToken(bearerStr);
		logoutTokenOpt.ifPresent(revokedToken -> 
			this.eventProducer.sendLogoutMsg(this.revokedTokenMapper.convert(revokedToken)));		
		return logoutTokenOpt.isPresent();
	}

	public Boolean logoutMsg(RevokedTokenDto revokedTokenDto) {
		Boolean result = super.logout(revokedTokenDto);
		this.updateLoggedOutUsers();
		return result;
	}
	
	public void sendKafkaEvent(KafkaEventDto kafkaEventDto) {
		this.eventProducer.sendKafkaEvent(kafkaEventDto);
	}
}
