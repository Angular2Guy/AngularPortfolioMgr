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

import ch.xxx.manager.dto.SymbolDto;
import ch.xxx.manager.entity.SymbolEntity;
import ch.xxx.manager.repository.SymbolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class SymbolService {
	@Autowired
	private SymbolRepository repository;
	
	public Flux<SymbolDto> getAllSymbols() {
		return this.repository.findAll().flatMap(entity -> this.convert(entity));
	}
	
	public Mono<SymbolDto> getSymbolBySymbol(String symbol) {
		return this.repository.findBySymbol(symbol).flatMap(entity -> this.convert(entity));
	}
	
	public Flux<SymbolDto> getSymbolByName(String name) {
		return this.repository.findByName(name).flatMap(entity -> this.convert(entity));
	}
	
	private Mono<SymbolDto> convert(SymbolEntity entity) {
		return Mono.just(new SymbolDto(entity.getId(), entity.getSymbol(), entity.getName()));
	}
}
