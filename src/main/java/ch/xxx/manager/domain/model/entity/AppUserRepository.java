package ch.xxx.manager.domain.model.entity;

import java.util.Optional;

public interface AppUserRepository {
	Optional<AppUser> findByUsername(String username);
	Optional<AppUser> findByUuid(String uuid);
}
