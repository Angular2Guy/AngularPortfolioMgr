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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional
@Service
public class AppUserService {
	@Autowired
	private AppUserRepository repository;
	@Autowired
	private IdService idService;
		
	public Mono<AppUserEntity> save(AppUserDto appUser) {
//		if(appUser.getId() == null) {
//			appUser.setId(this.idService.getId());
//		}
		return this.repository.save(this.convert(appUser));
	}
	
	public Mono<AppUserDto> load(Long id) {
		return this.repository.findById(id).flatMap(entity -> Mono.just(convert(entity)));
	}
		
	public Flux<AppUserDto> loadAll() {
		return this.repository.findAll().flatMap(entity -> Flux.just(convert(entity)));
	}
	
	private AppUserDto convert(AppUserEntity entity) {
		AppUserDto dto = new AppUserDto();
		dto.setBirthdate(entity.getBirthdate());
		dto.setFirstname(entity.getFirstname());
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		return dto;
	}
	
	private AppUserEntity convert(AppUserDto dto) {
		AppUserEntity entity = new AppUserEntity();
		entity.setBirthdate(dto.getBirthdate());
		entity.setFirstname(dto.getFirstname());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		return entity;
	}
}
