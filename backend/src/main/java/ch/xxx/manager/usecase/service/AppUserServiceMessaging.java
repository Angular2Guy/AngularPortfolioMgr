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

import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.RevokedTokenRepository;
import ch.xxx.manager.usecase.mapping.AppUserMapper;

@Profile("kafka | prod-kafka")
@Transactional
@Service
public class AppUserServiceMessaging extends AppUserServiceBase implements AppUserService {

	public AppUserServiceMessaging(AppUserRepository repository, AppUserMapper appUserMapper,
			JavaMailSender javaMailSender, RevokedTokenRepository revokedTokenRepository,
			PasswordEncoder passwordEncoder, JwtTokenService jwtTokenProvider, AppInfoService myService) {
		super(repository, appUserMapper, javaMailSender, revokedTokenRepository, passwordEncoder, jwtTokenProvider, myService);
	}

	@Override
	public Boolean signin(AppUserDto appUserDto) {
		// TODO Auto-generated method stub
		return Boolean.TRUE;
	}

	@Override
	public Boolean logout(String bearerStr) {
		// TODO Auto-generated method stub
		return Boolean.TRUE;
	}

}
