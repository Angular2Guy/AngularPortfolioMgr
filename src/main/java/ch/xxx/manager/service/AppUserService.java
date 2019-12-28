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
package ch.xxx.manager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.dto.AppUserDto;
import ch.xxx.manager.entity.AppUserEntity;
import ch.xxx.manager.repository.AppUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@Service
public class AppUserService {
	@Autowired
	private AppUserRepository repository;
		
	public Mono<AppUserEntity> save(AppUserDto appUser) {
		return this.repository.save(this.convert(appUser));
	}
	
	public Mono<AppUserDto> load(Long id) {
		return this.repository.findById(id).flatMap(entity -> Mono.just(convert(entity)));
	}
		
	public Flux<AppUserDto> loadAll() {
		return this.repository.findAll().flatMap(entity -> Flux.just(convert(entity)));
	}
	
	private AppUserDto convert(AppUserEntity entity) {
		AppUserDto dto = new AppUserDto(entity.getId(), entity.getUsername(), entity.getBirthdate(), entity.getPassword(), 
				entity.getEmailAddress(), entity.getUserRole(), entity.isLocked(), entity.isEnabled());
		return dto;
	}
	
	private AppUserEntity convert(AppUserDto dto) {
		AppUserEntity entity = new AppUserEntity(dto.getId(), dto.getUsername(), dto.getBirthdate(), dto.getPassword(), dto.getEmailAddress(), dto.getUserRole(), dto.isLocked(), dto.isEnabled());
		return entity;
	}
}
