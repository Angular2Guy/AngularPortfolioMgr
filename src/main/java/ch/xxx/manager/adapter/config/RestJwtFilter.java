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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.utils.Role;
import ch.xxx.manager.domain.utils.TokenSubjectRole;
import ch.xxx.manager.usecase.service.JwtTokenService;

@Component
public class RestJwtFilter implements Filter {
	private static final Logger LOG = LoggerFactory.getLogger(RestJwtFilter.class);
	
	private final JwtTokenService jwtTokenService;
	
	public RestJwtFilter(JwtTokenService jwtTokenService) {
		this.jwtTokenService = jwtTokenService;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		if (httpReq.getRequestURI().contains("/rest") && !httpReq.getRequestURI().contains("/rest/auth")) {			
			TokenSubjectRole tokenTuple = this.jwtTokenService.getTokenUserRoles(createHeaderMap(request));
			if (tokenTuple.role() == null || !tokenTuple.role().contains(Role.USERS.name())) {
				HttpServletResponse httpRes = (HttpServletResponse) response;
				httpRes.setStatus(401);
				LOG.info("Request denied: ",httpReq.getRequestURL().toString());
				return;
			}
		}
		chain.doFilter(request, response);
	}

	private Map<String,String> createHeaderMap(ServletRequest req) {
		Map<String, String> header = new HashMap<>();
		HttpServletRequest httpReq = (HttpServletRequest) req;
		for(Iterator<String> iter = httpReq.getHeaderNames().asIterator(); iter.hasNext();) {
			String key = iter.next();
			header.put(key, httpReq.getHeader(key));			
		}
		return header;
	}
}
