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

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import ch.xxx.manager.usecase.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class JwtTokenFilter extends GenericFilterBean {

	private JwtTokenService jwtTokenProvider;

	public JwtTokenFilter(JwtTokenService jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		jwtTokenProvider.resolveToken((HttpServletRequest) request).stream()
				.filter(myToken -> this.jwtTokenProvider.validateToken(myToken)).findFirst().ifPresentOrElse(
						myToken -> SecurityContextHolder.getContext()
								.setAuthentication(this.jwtTokenProvider.getAuthentication(myToken)),
						() -> SecurityContextHolder.getContext().setAuthentication(null));

		filterChain.doFilter(request, response);
	}

}
