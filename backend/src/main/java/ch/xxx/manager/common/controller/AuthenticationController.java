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
package ch.xxx.manager.common.controller;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.common.dto.AppUserDto;
import ch.xxx.manager.common.dto.AuthCheckDto;
import ch.xxx.manager.common.dto.KafkaEventDto;
import ch.xxx.manager.common.dto.RefreshTokenDto;
import ch.xxx.manager.common.utils.DataHelper;
import ch.xxx.manager.common.AppUserService;

@RestController
@RequestMapping("/rest/auth")
public class AuthenticationController {
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);
	
	private final AppUserService appUserService;

	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	private String confirmUrl;
	@Value("${spring.profiles.active:}")
	private String activeProfile;
	
	public AuthenticationController(AppUserService appUserService) {
		this.appUserService = appUserService;
	}
	
	@PostMapping("/authorize")
	public AuthCheckDto postAuthorize(@RequestBody AuthCheckDto authcheck, @RequestHeader Map<String, String> header) {
		String tokenRoles = this.appUserService.getTokenRoles(header).role();
		if (tokenRoles != null && tokenRoles.contains(DataHelper.Role.USERS.name()) && !tokenRoles.contains(DataHelper.Role.GUEST.name())) {
			return new AuthCheckDto(authcheck.getPath(), true);
		} else {
			return new AuthCheckDto(authcheck.getPath(), false);
		}
	}
	
	@PostMapping("/signin")
	public Boolean postUserSignin(@RequestBody AppUserDto myUser) {
		return this.appUserService.signin(myUser);
	}
	
	@GetMapping("/confirm/{uuid}")
	public Boolean getConfirmUuid(@PathVariable(name="uuid") String uuid) {
		return this.appUserService.confirmUuid(uuid);
	}

	@PostMapping("/login")
	public AppUserDto postUserLogin(@RequestBody AppUserDto myUser) {
		return this.appUserService.login(myUser);
	}
	
	@PutMapping("/logout")
	public Boolean putUserLogout(@RequestHeader(value =  HttpHeaders.AUTHORIZATION) String bearerStr) {
		return this.appUserService.logout(bearerStr);
	}
	
	@GetMapping("/refreshToken")
	public RefreshTokenDto getRefreshToken(@RequestHeader(value =  HttpHeaders.AUTHORIZATION) String bearerStr) {
		return this.appUserService.refreshToken(bearerStr);
	}
	
	@GetMapping("/id/{id}")
	public AppUserDto getUser(@PathVariable(name = "id") Long id) {
		AppUserDto appUserDto = this.appUserService.loadById(id);
		return appUserDto;
	}
	/*
	@GetMapping("/all")
	public List<AppUserDto> getUsers() {
		return this.appUserService.loadAll();
	}
	*/
	@PutMapping()
	public AppUserDto putUser(@RequestBody AppUserDto appUserDto) {
		return this.appUserService.save(appUserDto);
	}
	
	@PutMapping("/kafkaEvent")
	public ResponseEntity<Boolean> putKafkaEvent(@RequestBody KafkaEventDto dto) {
		ResponseEntity<Boolean> result = new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.FORBIDDEN);
		if (!this.activeProfile.toLowerCase().contains("prod")) {
			result = new ResponseEntity<Boolean>(Boolean.TRUE, HttpStatus.ACCEPTED);
			try {
				this.appUserService.sendKafkaEvent(dto);
			} catch (Exception e) {
				result = new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.BAD_REQUEST);
			}
		}
		return result;
	}
}
