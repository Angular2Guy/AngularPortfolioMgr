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
package ch.xxx.manager.domain.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ch.xxx.manager.domain.utils.DataHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppUserDto implements UserDetails {
	private static final long serialVersionUID = 821118557600406928L;
	private Long id;
	private String username;
	private LocalDate birthdate;
	private LocalDateTime updatedAt = LocalDateTime.now();
	private String password;
	private String emailAddress;
	private String userRole;
	private boolean locked;
	private boolean enabled;
	private String uuid;
	private String token;
	//500 calls a day and 5 a min
	private String alphavantageKey;
	//500 calls a month (only for company info)
	private String rapidApiKey;
	private Long secUntilNexLogin;

	public AppUserDto(Long id, String userName, LocalDate birthdate, String password, String token, String emailAddress,
			String userRole, boolean locked, boolean enabled, String uuid, String alphavantageKey, String rapidApiKey,Long secUntilNexLogin) {
		super();
		this.id = id;
		this.username = userName;
		this.birthdate = birthdate;
		this.password = password;
		this.emailAddress = emailAddress;
		this.userRole = userRole == null ? DataHelper.Role.GUEST.name() : userRole;
		this.locked = locked;
		this.enabled = enabled;
		this.uuid = uuid;
		this.secUntilNexLogin = secUntilNexLogin;
		this.token = token;
		this.alphavantageKey = alphavantageKey;
		this.rapidApiKey = rapidApiKey;
	}

	public AppUserDto() {
	}

	public String getRapidApiKey() {
		return rapidApiKey;
	}

	public void setRapidApiKey(String rapidApiKey) {
		this.rapidApiKey = rapidApiKey;
	}

	public String getAlphavantageKey() {
		return alphavantageKey;
	}

	public void setAlphavantageKey(String alphavantageKey) {
		this.alphavantageKey = alphavantageKey;
	}

	public Long getSecUntilNexLogin() {
		return secUntilNexLogin;
	}

	public void setSecUntilNexLogin(Long secUntilNexLogin) {
		this.secUntilNexLogin = secUntilNexLogin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(LocalDate birthdate) {
		this.birthdate = birthdate;
	}

	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		GrantedAuthority auth = () -> this.userRole;
		return Arrays.asList(auth);
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !this.locked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return "AppUserDto [id=" + id + ", username=" + username + ", birthdate=" + birthdate + ", updatedAt="
				+ updatedAt + ", password=" + password + ", emailAddress=" + emailAddress + ", userRole=" + userRole
				+ ", locked=" + locked + ", enabled=" + enabled + ", uuid=" + uuid + ", token=" + token
				+ ", secUntilNexLogin=" + secUntilNexLogin + "]";
	}
}
