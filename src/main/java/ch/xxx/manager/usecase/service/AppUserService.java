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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.RefreshTokenDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.utils.Role;
import ch.xxx.manager.domain.utils.TokenSubjectRole;
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
	private final MyService myService;

	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	private String confirmUrl;

	public AppUserService(AppUserRepository repository, AppUserMapper appUserMapper, JavaMailSender javaMailSender,
			PasswordEncoder passwordEncoder, JwtTokenService jwtTokenProvider, MyService myService) {
		this.repository = repository;
		this.javaMailSender = javaMailSender;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.appUserMapper = appUserMapper;
		this.myService = myService;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("Profiles: {}, Classname: {}", this.myService.getProfile(), this.myService.getClassName());
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

	public Boolean signin(AppUserDto appUserDto) {
		return Optional.ofNullable(appUserDto.getId()).stream()
				.flatMap(id -> Stream.of(Boolean.FALSE))
				.findFirst().orElseGet(() ->  this.checkSaveSignin(this.appUserMapper.convert(appUserDto,
						this.repository.findByUsername(appUserDto.getUsername()))));
	}

	private Boolean checkSaveSignin(AppUser entity) {
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
			return this.repository.save(entity).getId() != null;
		}
		LOGGER.warn("Username multiple signin: {}", entity.getUserName());
		return Boolean.FALSE;
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
		AppUserDto user = this.appUserMapper.convert(entityOpt);
		Optional<Role> myRole = Arrays.stream(Role.values()).filter(role1 -> role1.name().equals(user.getUserRole()))
				.findAny();
		if (user.getId() != null && myRole.isPresent() && entityOpt.isPresent() &&  entityOpt.get().isEnabled()) {
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
		return jwtTokenProvider.getTokenUserRoles(headers);
	}

	public AppUserDto load(Long id) {
		return this.appUserMapper.convert(this.repository.findById(id));
	}

	public List<AppUserDto> loadAll() {
		return this.repository.findAll().stream()
				.flatMap(entity -> Stream.of(this.appUserMapper.convert(Optional.of(entity))))
				.collect(Collectors.toList());
	}
}
