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

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import ch.xxx.manager.usecase.service.JwtTokenService;

public class JwtTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	private JwtTokenService jwtTokenProvider;

	  public JwtTokenFilterConfigurer(JwtTokenService jwtTokenProvider) {
	    this.jwtTokenProvider = jwtTokenProvider;
	  }

	  @Override
	  public void configure(HttpSecurity http) throws Exception {
	    JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenProvider);
	    http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
