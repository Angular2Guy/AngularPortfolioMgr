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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;

import ch.xxx.manager.domain.exception.AuthenticationException;
import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.KafkaEventDto;
import ch.xxx.manager.domain.model.dto.RefreshTokenDto;
import ch.xxx.manager.domain.model.dto.RevokedTokenDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.RevokedToken;
import ch.xxx.manager.domain.model.entity.RevokedTokenRepository;
import ch.xxx.manager.domain.utils.Role;
import ch.xxx.manager.domain.utils.TokenSubjectRole;
import ch.xxx.manager.usecase.mapping.AppUserMapper;
import ch.xxx.manager.usecase.mapping.RevokedTokenMapper;

public class AppUserServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppUserServiceBase.class);
	private static final long LOGOUT_TIMEOUT = 185L;
	private final AppUserRepository repository;
	protected final AppUserMapper appUserMapper;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;
	private final AppInfoService myService;
	private final RevokedTokenRepository revokedTokenRepository;
	protected final RevokedTokenMapper revokedTokenMapper;
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	protected String confirmUrl;

	public AppUserServiceBase(AppUserRepository repository, AppUserMapper appUserMapper, JavaMailSender javaMailSender,
			RevokedTokenRepository revokedTokenRepository, PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenProvider, AppInfoService myService, RevokedTokenMapper revokedTokenMapper) {
		this.repository = repository;
		this.javaMailSender = javaMailSender;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenProvider;
		this.appUserMapper = appUserMapper;
		this.myService = myService;
		this.revokedTokenRepository = revokedTokenRepository;
		this.revokedTokenMapper = revokedTokenMapper;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("Profiles: {}, Classname: {}", this.myService.getProfiles(), this.myService.getClassName());
	}

	public void updateLoggedOutUsers() {
		this.updateLoggedOutUsers(LOGOUT_TIMEOUT);
	}

	protected void updateLoggedOutUsers(Long timeout) {
		final List<RevokedToken> revokedTokens = new ArrayList<RevokedToken>(this.revokedTokenRepository.findAll());
		this.jwtTokenService.updateLoggedOutUsers(revokedTokens.stream()
				.filter(myRevokedToken -> myRevokedToken.getLastLogout() == null
						|| !myRevokedToken.getLastLogout().isBefore(LocalDateTime.now().minusSeconds(timeout)))
				.toList());
		this.revokedTokenRepository.deleteAll(revokedTokens.stream()
				.filter(myRevokedToken -> myRevokedToken.getLastLogout() != null
						&& myRevokedToken.getLastLogout().isBefore(LocalDateTime.now().minusSeconds(timeout)))
				.toList());
	}
	
	public RefreshTokenDto refreshToken(String bearerToken) {
		Optional<String> tokenOpt = this.jwtTokenService.resolveToken(bearerToken);
		if (tokenOpt.isEmpty()) {
			throw new AuthorizationServiceException("Invalid token");
		}
		String newToken = this.jwtTokenService.refreshToken(tokenOpt.get());
		LOGGER.info("Jwt Token refreshed.");
		return new RefreshTokenDto(newToken);
	}

	public AppUserDto save(AppUserDto appUser) {
		return this.appUserMapper.convert(
				Optional.of(this.repository
						.save(this.appUserMapper.convert(appUser, this.repository.findById(appUser.getId())))),
				null, 10L);
	}

	public Boolean confirmUuid(String uuid) {
		return this.confirmUuid(this.repository.findByUuid(uuid), uuid);
	}

	private Boolean confirmUuid(Optional<AppUser> entityOpt, final String uuid) {
		return entityOpt.map(entity -> {
			entity.setEnabled(true);
			return this.repository.save(entity).isEnabled();
		}).orElseGet(() -> {
			LOGGER.warn("Uuid confirm failed: {}", uuid);
			return Boolean.FALSE;
		});
	}

	public AppUserDto login(AppUserDto appUserDto) {
		return this.loginHelp(this.repository.findByUsername(appUserDto.getUsername()), appUserDto.getPassword());
	}

	private AppUserDto loginHelp(Optional<AppUser> entityOpt, String passwd) {
		AppUserDto user = new AppUserDto();
		Optional<Role> myRole = entityOpt.stream().flatMap(myUser -> Arrays.stream(Role.values())
				.filter(role1 -> role1.name().equalsIgnoreCase(myUser.getUserRole()))).findAny();
		if (myRole.isPresent() && entityOpt.get().isEnabled()
				&& this.passwordEncoder.matches(passwd, entityOpt.get().getPassword())) {
			Callable<String> callableTask = () -> this.jwtTokenService.createToken(entityOpt.get().getUserName(),
					Arrays.asList(myRole.get()), Optional.empty());
			try {
				String jwtToken = executorService.schedule(callableTask, 3, TimeUnit.SECONDS).get();
				user = this.appUserMapper.convert(entityOpt, jwtToken, 0L);
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("Login failed", e);
			}
		}
		return user;
	}

	public Boolean signin(AppUserDto appUserDto) {
		return this.signin(appUserDto, true, true).isPresent();
	}

	public Optional<AppUser> signin(AppUserDto appUserDto, boolean persist, boolean check) {	
		if(appUserDto.getId() != null) {
			return Optional.empty();
		}
		Optional<AppUser> result = check ? this.checkSaveSignin(this.appUserMapper.convert(appUserDto,
				this.repository.findByUsername(appUserDto.getUsername()))) : Optional.of(this.appUserMapper.convert(appUserDto));
		result = result.stream().map(myAppUser -> persist ? this.repository.save(myAppUser) : myAppUser).findAny();
		return result;
	}

	private Optional<AppUser> checkSaveSignin(AppUser entity) {
		Optional<AppUser> result = Optional.empty();
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
			result = Optional.of(entity);
		} else {
			LOGGER.warn("Username multiple signin: {}", entity.getUserName());
		}
		return result;
	}

	public Boolean logout(String bearerStr) {
		Optional<RevokedToken> revokedTokenOpt = this.logoutToken(bearerStr).stream()
				.peek(revokedToken -> this.revokedTokenRepository.save(revokedToken)).findAny();
		return revokedTokenOpt.isPresent();
	}

	public Boolean logout(RevokedTokenDto revokedTokenDto) {
//		this.revokedTokenRepository.findAll().stream()
//		.filter(myRevokedToken -> myRevokedToken.getUuid().equals(revokedTokenDto.getUuid())
//				&& myRevokedToken.getName().equalsIgnoreCase(revokedTokenDto.getName()))
//		.findAny().or(() -> Optional.of(this.revokedTokenRepository.save(this.revokedTokenMapper.convert(revokedTokenDto))));
		this.revokedTokenRepository.save(this.revokedTokenMapper.convert(revokedTokenDto));
		return Boolean.TRUE;
	}

	protected Optional<RevokedToken> logoutToken(String bearerStr) {
		if (!this.jwtTokenService.validateToken(this.jwtTokenService.resolveToken(bearerStr).orElse(""))) {
			throw new AuthenticationException("Invalid token.");
		}
		String username = this.jwtTokenService.getUsername(this.jwtTokenService.resolveToken(bearerStr)
				.orElseThrow(() -> new AuthenticationException("Invalid bearer string.")));
		String uuid = this.jwtTokenService.getUuid(this.jwtTokenService.resolveToken(bearerStr)
				.orElseThrow(() -> new AuthenticationException("Invalid bearer string.")));
		this.repository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("Username not found: " + username));
		long revokedTokensForUuid = this.revokedTokenRepository.findAll().stream()
				.filter(myRevokedToken -> myRevokedToken.getUuid().equals(uuid)
						&& myRevokedToken.getName().equalsIgnoreCase(username))
				.count();
		Optional<RevokedToken> result = Optional.empty();
		if (revokedTokensForUuid == 0) {
			result = Optional.of(new RevokedToken(username, uuid, LocalDateTime.now()));			
		} else {
			LOGGER.warn("Duplicate logout for user {} logout: {}", username, revokedTokensForUuid);
		}
		return result;
	}

	private void sendConfirmMail(AppUser entity) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(entity.getEmailAddress());
		msg.setSubject("AngularPortfolioMgr Account Confirmation Mail");
		String url = this.confirmUrl + "/" + entity.getUuid();
		msg.setText(String
				.format("Welcome to the AngularPwaMessenger please use this link(%s) to confirm your account.", url));
		this.javaMailSender.send(msg);
		LOGGER.info("Confirm Mail send to: " + entity.getEmailAddress());
	}

	public TokenSubjectRole getTokenRoles(Map<String, String> headers) {
		return jwtTokenService.getTokenUserRoles(headers);
	}

	public AppUserDto load(Long id) {
		return this.appUserMapper.convert(this.repository.findById(id), null, 10L);
	}

	public List<AppUserDto> loadAll() {
		return this.repository.findAll().stream()
				.flatMap(entity -> Stream.of(this.appUserMapper.convert(Optional.of(entity), null, 10L)))
				.collect(Collectors.toList());
	}
	
	public void sendKafkaEvent(KafkaEventDto kafkaEventDto) {
		LOGGER.info("KafkaEvent not send.");
	}
}
