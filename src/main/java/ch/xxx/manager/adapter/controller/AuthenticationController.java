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
package ch.xxx.manager.adapter.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.usecase.service.AppUserService;
import ch.xxx.manager.usecase.service.JwtTokenService;

@RestController
@RequestMapping("/rest/auth")
public class AuthenticationController {
	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);
	
	private final JwtTokenService jwtTokenProvider;
	private final AppUserService appUserService;

	@Value("${spring.mail.username}")
	private String mailuser;
	@Value("${spring.mail.password}")
	private String mailpwd;
	@Value("${messenger.url.uuid.confirm}")
	private String confirmUrl;
	
	public AuthenticationController(JwtTokenService jwtTokenProvider, AppUserService appUserService) {
		this.appUserService = appUserService;
		this.jwtTokenProvider = jwtTokenProvider;
	}
	
//	@PostMapping("/authorize")
//	public Mono<AuthCheckDto> postAuthorize(@RequestBody AuthCheckDto authcheck, @RequestHeader Map<String, String> header) {
//		String tokenRoles = JwtUtils.getTokenRoles(header, jwtTokenProvider);
//		if (tokenRoles.contains(Role.USERS.name()) && !tokenRoles.contains(Role.GUEST.name())) {
//			return Mono.just(new AuthCheckDto(authcheck.getPath(), true));
//		} else {
//			return Mono.just(new AuthCheckDto(authcheck.getPath(), false));
//		}
//	}
//	
//	@PostMapping("/signin")
//	public Mono<Boolean> postUserSignin(@RequestBody AppUserDto myUser) {
//		return this.appUserService.signin(myUser);
//	}
//	
//	@GetMapping("/confirm/{uuid}")
//	public Mono<Boolean> getConfirmUuid(@PathVariable String uuid) {
//		return this.appUserService.confirmUuid(uuid);
//	}
//
//	@PostMapping("/login")
//	public Mono<AppUserDto> postUserLogin(@RequestBody AppUserDto myUser) {
//		return this.appUserService.login(myUser);
//	}
//	
//	@GetMapping("/refreshToken")
//	public Mono<RefreshTokenDto> getRefreshToken(@RequestHeader(value =  HttpHeaders.AUTHORIZATION) String bearerStr) {
//		return this.appUserService.refreshToken(bearerStr);
//	}
	
	@GetMapping("/id/{id}")
	public AppUserDto getUser(@PathVariable Long id) {
		return this.appUserService.load(id);
	}
	
	@GetMapping("/all")
	public List<AppUserDto> getUsers() {
		return this.appUserService.loadAll();
	}
	
	@PutMapping()
	public AppUserDto putUser(@RequestBody AppUserDto appUserDto) {
		return this.appUserService.save(appUserDto);
	}
}
