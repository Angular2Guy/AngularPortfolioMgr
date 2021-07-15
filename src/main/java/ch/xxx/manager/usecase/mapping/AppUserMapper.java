package ch.xxx.manager.usecase.mapping;

import java.util.Optional;

import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.entity.AppUser;

@Component
public class AppUserMapper {
	public AppUserDto convert(Optional<AppUser> entityOpt) {
		AppUserDto dto = entityOpt.isEmpty() ? null : new AppUserDto(entityOpt.get().getId(), entityOpt.get().getUserName(), entityOpt.get().getBirthDate(),
				entityOpt.get().getPassword(), entityOpt.get().getEmailAddress(), entityOpt.get().getUserRole(), entityOpt.get().isLocked(),
				entityOpt.get().isEnabled(), entityOpt.get().getUuid());
		return dto;
	}
	
	public AppUser convert(AppUserDto dto, Optional<AppUser> entityOpt) {
		final AppUser myEntity = entityOpt.orElse(new AppUser());
		myEntity.setBirthDate(dto.getBirthdate());
		myEntity.setEmailAddress(dto.getEmailAddress());
		myEntity.setPassword(dto.getPassword());
		myEntity.setUserName(dto.getUsername());
		myEntity.setUserRole(dto.getUserRole());
		myEntity.setUuid(dto.getUuid());		
		return myEntity;
	}
}
