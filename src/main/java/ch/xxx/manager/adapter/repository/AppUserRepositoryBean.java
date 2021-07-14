package ch.xxx.manager.adapter.repository;

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
	
	
}
