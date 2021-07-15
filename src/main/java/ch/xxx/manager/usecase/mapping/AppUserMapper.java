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
