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
package ch.xxx.manager.jwt;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

public class JwtUtils {
	public static final String AUTHORIZATION = HttpHeaders.AUTHORIZATION;
	public static final String TOKENAUTHKEY = "auth";
	public static final String TOKENLASTMSGKEY = "lastmsg";
	public static final String BEARER = "Bearer ";
	public static final String AUTHORITY = "authority";

	public static Optional<String> extractToken(Map<String,String> headers) {
		String authStr = headers.get(AUTHORIZATION);
		return extractToken(Optional.ofNullable(authStr));
	}

	private static Optional<String> extractToken(Optional<String> authStr) {
		if (authStr.isPresent()) {
			authStr = Optional.ofNullable(authStr.get().startsWith(BEARER) ? authStr.get().substring(7) : null);
		}
		return authStr;
	}

	public static String getTokenRoles(Map<String,String> headers, JwtTokenProvider jwtTokenProvider) {
		Optional<String> tokenStr = extractToken(headers);
		Optional<Jws<Claims>> claims = jwtTokenProvider.getClaims(tokenStr);
		if (claims.isPresent() && new Date().before(claims.get().getBody().getExpiration())) {
			return claims.get().getBody().get(TOKENAUTHKEY).toString();
		}
		return "";
	}

	public static Tuple<String, String> getTokenUserRoles(Map<String,String> headers,
			JwtTokenProvider jwtTokenProvider) {
		Optional<String> tokenStr = extractToken(headers);
		Optional<Jws<Claims>> claims = jwtTokenProvider.getClaims(tokenStr);
		if (claims.isPresent() && new Date().before(claims.get().getBody().getExpiration())) {
			return new Tuple<String, String>(claims.get().getBody().getSubject(),
					claims.get().getBody().get(TOKENAUTHKEY).toString());
		}
		return new Tuple<String, String>(null, null);
	}

	public static boolean checkToken(HttpServletRequest request, JwtTokenProvider jwtTokenProvider) {
		Optional<String> tokenStr = JwtUtils
				.extractToken(Optional.ofNullable(request.getHeader(JwtUtils.AUTHORIZATION)));
		Optional<Jws<Claims>> claims = jwtTokenProvider.getClaims(tokenStr);
		if (claims.isPresent() && new Date().before(claims.get().getBody().getExpiration())
				&& claims.get().getBody().get(TOKENAUTHKEY).toString().contains(Role.USERS.name())
				&& !claims.get().getBody().get(TOKENAUTHKEY).toString().contains(Role.GUEST.name())) {
			return true;
		}
		return false;
	}
}
