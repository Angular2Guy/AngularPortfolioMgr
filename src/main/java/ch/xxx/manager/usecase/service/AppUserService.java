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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.RefreshTokenDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.usecase.mapping.AppUserMapper;

@Transactional
@Service
public class AppUserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppUserService.class);
	private final AppUserRepository repository;
	private final AppUserMapper appUserMapper;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenProvider;

	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	private String confirmUrl;

	public AppUserService(AppUserRepository repository, AppUserMapper appUserMapper, JavaMailSender javaMailSender,
			PasswordEncoder passwordEncoder, JwtTokenService jwtTokenProvider) {
		this.repository = repository;
		this.javaMailSender = javaMailSender;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.appUserMapper = appUserMapper;
	}

	public RefreshTokenDto refreshToken(String bearerToken) {
		Optional<String> tokenOpt = this.jwtTokenProvider.resolveToken(bearerToken);
		if (tokenOpt.isEmpty()) {
			throw new AuthorizationServiceException("Invalid token");
		}
		String newToken = this.jwtTokenProvider.refreshToken(tokenOpt.get());
		LOGGER.info("Jwt Token refreshed.");
		return new RefreshTokenDto(newToken);
	}

	public AppUserDto save(AppUserDto appUser) {
		return this.appUserMapper.convert(Optional.of(
				this.repository.save(this.appUserMapper.convert(appUser, this.repository.findById(appUser.getId())))));
	}

//	public Mono<Boolean> signin(AppUserDto appUserDto) {
//		if (appUserDto.getId() != null) {
//			return Mono.just(Boolean.FALSE);
//		}
//		return this.repository.findByUsername(appUserDto.getUsername()).defaultIfEmpty(this.convert(appUserDto))
//				.flatMap(entity -> this.checkSaveSignin(entity));
//	}
//
//	private Mono<Boolean> checkSaveSignin(AppUserEntity entity) {
//		if (entity.getId() == null) {
//			String encryptedPassword = this.passwordEncoder.encode(entity.getPassword());
//			entity.setPassword(encryptedPassword);
//			UUID uuid = UUID.randomUUID();
//			entity.setUuid(uuid.toString());
//			entity.setLocked(false);
//			entity.setUserRole(Role.USERS.name());
//			boolean emailConfirmEnabled = this.confirmUrl != null && !this.confirmUrl.isBlank();
//			entity.setEnabled(!emailConfirmEnabled);
//			if (emailConfirmEnabled) {
//				this.sendConfirmMail(entity);
//			}
//			return this.repository.save(entity).flatMap(myEntity -> Mono.just(myEntity.getId() != null));
//		}
//		LOGGER.warn("Username multiple signin: {}", entity.getUsername());
//		return Mono.just(Boolean.FALSE);
//	}
//
//	public Mono<Boolean> confirmUuid(String uuid) {
//		return this.repository.findByUuid(uuid).defaultIfEmpty(new AppUserEntity())
//				.flatMap(entity -> this.confirmUuid(entity));
//	}
//
//	private Mono<Boolean> confirmUuid(AppUserEntity entity) {
//		if (entity.getId() != null) {
//			entity.setEnabled(true);
//			entity.setUpdatedAt(LocalDateTime.now());
//			return this.repository.save(entity).flatMap(myEntity -> Mono.just(myEntity.isEnabled()));
//		}
//		LOGGER.warn("Uuid confirm failed: {}", entity.getUuid());
//		return Mono.just(Boolean.FALSE);
//	}
//
//	public Mono<AppUserDto> login(AppUserDto appUserDto) {
//		return this.repository.findByUsername(appUserDto.getUsername()).defaultIfEmpty(new AppUserEntity()).flatMap(entity -> Mono.just(loginHelp(entity, appUserDto.getPassword())));
//	}
//
//	private AppUserDto loginHelp(AppUserEntity entity, String passwd) {
//		AppUserDto user = this.convert(entity);
//		Optional<Role> myRole = Arrays.stream(Role.values()).filter(role1 -> role1.name().equals(user.getUserRole())).findAny();
//		if (user.getId() != null && myRole.isPresent() && entity.isEnabled()) {
//			if (this.passwordEncoder.matches(passwd, user.getPassword())) {
//				String jwtToken = this.jwtTokenProvider.createToken(user.getUsername(), Arrays.asList(myRole.get()),
//						Optional.empty());
//				user.setToken(jwtToken);
//				user.setPassword("XXX");
//				user.setUuid("XXX");
//				return user;
//			}
//		}
//		return new AppUserDto();
//	}
//
//	private void sendConfirmMail(AppUserEntity entity) {
//		SimpleMailMessage msg = new SimpleMailMessage();
//		msg.setTo(entity.getEmailAddress());
//		msg.setSubject("AngularPortfolioMgr Account Confirmation Mail");
//		String url = this.confirmUrl + "/" + entity.getUuid();
//		msg.setText(String
//				.format("Welcome to the AngularPwaMessenger please use this link(%s) to confirm your account.", url));
//		this.javaMailSender.send(msg);
//		LOGGER.info("Confirm Mail send to: " + entity.getEmailAddress());
//	}
//
	public AppUserDto load(Long id) {
		return this.appUserMapper.convert(this.repository.findById(id));
	}

	public List<AppUserDto> loadAll() {
		return this.repository.findAll().stream()
				.flatMap(entity -> Stream.of(this.appUserMapper.convert(Optional.of(entity))))
				.collect(Collectors.toList());
	}
}
