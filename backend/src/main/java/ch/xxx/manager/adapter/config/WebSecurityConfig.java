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
package ch.xxx.manager.adapter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue;

import ch.xxx.manager.domain.utils.DataHelper;
import ch.xxx.manager.usecase.service.JwtTokenService;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)
public class WebSecurityConfig {
	private static final String DEVPATH = "/rest/dev/**";
	private static final String PRODPATH = "/rest/prod/**";
	private final JwtTokenService jwtTokenService;
	@Value("${spring.profiles.active:}")
	private String activeProfile;

	public WebSecurityConfig(JwtTokenService jwtTokenProvider) {
		this.jwtTokenService = jwtTokenProvider;
	}

	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenService);
		final String blockedPath = this.activeProfile.toLowerCase().contains("prod") ? DEVPATH : PRODPATH;
		HttpSecurity httpSecurity = http
				.authorizeHttpRequests(authorize -> authorize.requestMatchers("/rest/config/**").permitAll()
						.requestMatchers("/rest/kedatest/**").permitAll().requestMatchers("/rest/auth/**").permitAll()
						.requestMatchers("/rest/**").hasAuthority(DataHelper.Role.USERS.toString())
						.requestMatchers(blockedPath).denyAll())
				.authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll())
				.csrf(myCsrf -> myCsrf.disable())
				.sessionManagement(mySm -> mySm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(myHeaders -> myHeaders.contentSecurityPolicy(myCsp -> myCsp.policyDirectives(
						"default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';")))
				.headers(myHeaders -> myHeaders.xssProtection(myXss -> myXss.headerValue(HeaderValue.ENABLED)))
				.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
