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
package ch.xxx.manager.adapter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.AppUser;
import ch.xxx.manager.domain.model.entity.AppUserRepository;

@Repository
public class AppUserRepositoryBean implements AppUserRepository {
	private final JpaAppUserRepository jpaAppUserRepository;
	
	public AppUserRepositoryBean(JpaAppUserRepository jpaAppUserRepository) {
		this.jpaAppUserRepository = jpaAppUserRepository;
	}

	@Override
	public Optional<AppUser> findByUsername(String username) {
		return this.jpaAppUserRepository.findByUsername(username);
	}

	@Override
	public Optional<AppUser> findByUuid(String uuid) {
		return this.jpaAppUserRepository.findByUuid(uuid);
	}

	@Override
	public List<AppUser> findAll() {
		return this.jpaAppUserRepository.findAll();
	}

	@Override
	public Optional<AppUser> findById(Long id) {
		return this.jpaAppUserRepository.findById(id);				
	}

	@Override
	public AppUser save(AppUser appUser) {
		return this.jpaAppUserRepository.save(appUser);
	}

	@Override
	public List<AppUser> findLoggedOut() {
		return this.jpaAppUserRepository.findLoggedOut();
	}

	@Override
	public Iterable<AppUser> saveAll(Iterable<AppUser> users) {
		return this.jpaAppUserRepository.saveAll(users);
	}
}
