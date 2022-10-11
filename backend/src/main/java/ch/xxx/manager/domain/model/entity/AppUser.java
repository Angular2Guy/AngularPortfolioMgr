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
package ch.xxx.manager.domain.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Entity
public class AppUser {
	@Id	
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @SequenceGenerator(name="seq", sequenceName="hibernate_sequence", allocationSize = 50)
	private Long id;
	@NotBlank
	@Size(max=255)
	private String userName;
	private LocalDate birthDate;
	private LocalDateTime updatedAt;
	@NotBlank
	@Size(max=255)
	private String password;
	private String emailAddress;
	@NotBlank
	private String userRole;
	private boolean locked;
	private boolean enabled;
	private String uuid;
	@OneToMany(mappedBy = "appUser")
	private Set<Portfolio> portfolios = new HashSet<>();
	
	
	public AppUser(Long id, String userName, LocalDate birthdate, String password, String emailAddress, String userRole,
			boolean locked, boolean enabled, String uuid, Set<Portfolio> portfolios) {
		super();
		this.id = id;
		this.userName = userName;
		this.birthDate = birthdate;
		this.password = password;
		this.emailAddress = emailAddress;
		this.userRole = userRole;
		this.locked = locked;
		this.enabled = enabled;
		this.uuid = uuid;
		this.portfolios = portfolios;
	}

	public AppUser() {}

	@PrePersist
	@PreUpdate
	void init() {
		this.updatedAt = LocalDateTime.now();
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public LocalDate getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(LocalDate birthDate) {
		this.birthDate = birthDate;
	}

	public Set<Portfolio> getPortfolios() {
		return portfolios;
	}

	public void setPortfolios(Set<Portfolio> portfolios) {
		this.portfolios = portfolios;
	}
}
