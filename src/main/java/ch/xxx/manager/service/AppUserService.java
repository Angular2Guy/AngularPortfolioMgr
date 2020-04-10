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
package ch.xxx.manager.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.dto.AppUserDto;
import ch.xxx.manager.entity.AppUserEntity;
import ch.xxx.manager.jwt.JwtTokenProvider;
import ch.xxx.manager.jwt.Role;
import ch.xxx.manager.repository.AppUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@Service
public class AppUserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppUserService.class);
	@Autowired
	private AppUserRepository repository;
	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	private String confirmUrl;
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;
	
	public Mono<String> refreshToken(String bearerToken) {
		Optional<String> tokenOpt = this.jwtTokenProvider.resolveToken(bearerToken);
		if(tokenOpt.isEmpty()) {
			throw new AuthorizationServiceException("Invalid token");
		}
		String newToken = this.jwtTokenProvider.refreshToken(tokenOpt.get());
		return Mono.just(newToken);
	}

	public Mono<AppUserEntity> save(AppUserDto appUser) {
		return this.repository.save(this.convert(appUser));
	}

	public Mono<Boolean> signin(AppUserDto appUserDto) {
		if (appUserDto.getId() != null) {
			return Mono.just(Boolean.FALSE);
		}
		return this.repository.findByUsername(appUserDto.getUsername()).defaultIfEmpty(this.convert(appUserDto))
				.flatMap(entity -> this.checkSaveSignin(entity));
	}

	private Mono<Boolean> checkSaveSignin(AppUserEntity entity) {
		if (entity.getId() == null) {
			String encryptedPassword = this.passwordEncoder.encode(entity.getPassword());
			entity.setPassword(encryptedPassword);
			UUID uuid = UUID.randomUUID();
			entity.setUuid(uuid.toString());
			entity.setLocked(false);
			entity.setUserRole(Role.USERS.name());
			boolean emailConfirmEnabled = this.confirmUrl != null && !this.confirmUrl.isBlank();
			entity.setEnabled(!emailConfirmEnabled);
			if (emailConfirmEnabled) {
				this.sendConfirmMail(entity);
			}
			return this.repository.save(entity).flatMap(myEntity -> Mono.just(myEntity.getId() != null));
		}
		LOGGER.warn("Username multiple signin: {}", entity.getUsername());
		return Mono.just(Boolean.FALSE);
	}

	public Mono<Boolean> confirmUuid(String uuid) {
		return this.repository.findByUuid(uuid).defaultIfEmpty(new AppUserEntity())
				.flatMap(entity -> this.confirmUuid(entity));
	}

	private Mono<Boolean> confirmUuid(AppUserEntity entity) {
		if (entity.getId() != null) {
			entity.setEnabled(true);
			entity.setUpdatedAt(LocalDateTime.now());
			return this.repository.save(entity).flatMap(myEntity -> Mono.just(myEntity.isEnabled()));
		}
		LOGGER.warn("Uuid confirm failed: {}", entity.getUuid());
		return Mono.just(Boolean.FALSE);
	}

	public Mono<AppUserDto> login(AppUserDto appUserDto) {
		return this.repository.findByUsername(appUserDto.getUsername()).defaultIfEmpty(new AppUserEntity()).flatMap(entity -> Mono.just(loginHelp(entity, appUserDto.getPassword())));
	}

	private AppUserDto loginHelp(AppUserEntity entity, String passwd) {
		AppUserDto user = this.convert(entity);
		Optional<Role> myRole = Arrays.stream(Role.values()).filter(role1 -> role1.name().equals(user.getUserRole())).findAny();
		if (user.getId() != null && myRole.isPresent() && entity.isEnabled()) {
			if (this.passwordEncoder.matches(passwd, user.getPassword())) {
				String jwtToken = this.jwtTokenProvider.createToken(user.getUsername(), Arrays.asList(myRole.get()),
						Optional.empty());
				user.setToken(jwtToken);
				user.setPassword("XXX");
				user.setUuid("XXX");
				return user;
			}
		}
		return new AppUserDto();
	}

	private void sendConfirmMail(AppUserEntity entity) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(entity.getEmailAddress());
		msg.setSubject("AngularPortfolioMgr Account Confirmation Mail");
		String url = this.confirmUrl + "/" + entity.getUuid();
		msg.setText(String
				.format("Welcome to the AngularPwaMessenger please use this link(%s) to confirm your account.", url));
		this.javaMailSender.send(msg);
		LOGGER.info("Confirm Mail send to: " + entity.getEmailAddress());
	}

	public Mono<AppUserDto> load(Long id) {
		return this.repository.findById(id).flatMap(entity -> Mono.just(convert(entity)));
	}

	public Flux<AppUserDto> loadAll() {
		return this.repository.findAll().flatMap(entity -> Flux.just(convert(entity)));
	}

	private AppUserDto convert(AppUserEntity entity) {
		AppUserDto dto = new AppUserDto(entity.getId(), entity.getUsername(), entity.getBirthdate(),
				entity.getPassword(), entity.getEmailAddress(), entity.getUserRole(), entity.isLocked(),
				entity.isEnabled(), entity.getUuid());
		return dto;
	}

	private AppUserEntity convert(AppUserDto dto) {
		AppUserEntity entity = new AppUserEntity(dto.getId(), dto.getUsername(), dto.getBirthdate(), dto.getPassword(),
				dto.getEmailAddress(), dto.getUserRole(), dto.isLocked(), dto.isEnabled(), dto.getUuid());
		return entity;
	}
}
