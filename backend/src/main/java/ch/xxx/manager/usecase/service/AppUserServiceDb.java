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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.exception.AuthenticationException;
import ch.xxx.manager.domain.exception.ResourceNotFoundException;
import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.RevokedToken;
import ch.xxx.manager.domain.model.entity.RevokedTokenRepository;
import ch.xxx.manager.domain.utils.Role;
import ch.xxx.manager.usecase.mapping.AppUserMapper;

@Profile("!kafka & !prod-kafka")
@Transactional
@Service
public class AppUserServiceDb extends AppUserServiceBase implements AppUserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppUserServiceDb.class);
	
	public AppUserServiceDb(AppUserRepository repository, AppUserMapper appUserMapper, JavaMailSender javaMailSender,
			RevokedTokenRepository revokedTokenRepository, PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenProvider, AppInfoService myService) {
		super(repository, appUserMapper, javaMailSender, revokedTokenRepository, passwordEncoder, jwtTokenProvider, myService);
	}

	
	public Boolean signin(AppUserDto appUserDto) {
		return Optional.ofNullable(appUserDto.getId()).stream().flatMap(id -> Stream.of(Boolean.FALSE)).findFirst()
				.orElseGet(() -> this.checkSaveSignin(this.appUserMapper.convert(appUserDto,
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
	
	public Boolean logout(String bearerStr) {
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
		if (revokedTokensForUuid == 0) {
			this.revokedTokenRepository.save(new RevokedToken(username, uuid, LocalDateTime.now()));
		} else {
			LOGGER.warn("Duplicate logout for user {}", username);
		}
		return Boolean.TRUE;
	}
}
