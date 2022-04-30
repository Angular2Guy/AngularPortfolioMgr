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

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.RefreshTokenDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.RevokedToken;
import ch.xxx.manager.domain.model.entity.RevokedTokenRepository;
import ch.xxx.manager.domain.utils.Role;
import ch.xxx.manager.domain.utils.TokenSubjectRole;
import ch.xxx.manager.usecase.mapping.AppUserMapper;


public class AppUserServiceBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppUserServiceBase.class);
	private static final long LOGOUT_TIMEOUT = 185L;
	protected final AppUserRepository repository;
	protected final AppUserMapper appUserMapper;
	private final JavaMailSender javaMailSender;
	protected final PasswordEncoder passwordEncoder;
	protected final JwtTokenService jwtTokenService;
	private final AppInfoService myService;
	protected final RevokedTokenRepository revokedTokenRepository;
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	protected String confirmUrl;

	public AppUserServiceBase(AppUserRepository repository, AppUserMapper appUserMapper, JavaMailSender javaMailSender,
			RevokedTokenRepository revokedTokenRepository, PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenProvider, AppInfoService myService) {
		this.repository = repository;
		this.javaMailSender = javaMailSender;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenProvider;
		this.appUserMapper = appUserMapper;
		this.myService = myService;
		this.revokedTokenRepository = revokedTokenRepository;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("Profiles: {}, Classname: {}", this.myService.getProfiles(), this.myService.getClassName());
	}

	public void updateLoggedOutUsers() {
		final List<RevokedToken> revokedTokens = new ArrayList<RevokedToken>(this.revokedTokenRepository.findAll());
		this.jwtTokenService.updateLoggedOutUsers(revokedTokens.stream()
				.filter(myRevokedToken -> myRevokedToken.getLastLogout() == null
						|| !myRevokedToken.getLastLogout().isBefore(LocalDateTime.now().minusSeconds(LOGOUT_TIMEOUT)))
				.toList());
		this.revokedTokenRepository.deleteAll(revokedTokens.stream()
				.filter(myRevokedToken -> myRevokedToken.getLastLogout() != null
						&& myRevokedToken.getLastLogout().isBefore(LocalDateTime.now().minusSeconds(LOGOUT_TIMEOUT)))
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

	protected void sendConfirmMail(AppUser entity) {
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
}