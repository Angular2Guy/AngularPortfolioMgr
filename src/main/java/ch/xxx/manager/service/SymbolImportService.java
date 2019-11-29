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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.contoller.HkexConnector;
import ch.xxx.manager.contoller.NasdaqConnector;
import ch.xxx.manager.dto.HkSymbolImportDto;
import ch.xxx.manager.entity.SymbolEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Transactional
public class SymbolImportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolImportService.class);
	@Autowired
	private NasdaqConnector nasdaqConnector;
	@Autowired
	private HkexConnector hkexConnector;
	@Autowired
	private SymbolRepository repository;			
	
	public Mono<Long> importUSSymbols() {
		return this.nasdaqConnector.importSymbols().filter(str -> filter(str))
				.flatMap(symbolStr -> this.convert(symbolStr))
			.flatMap(entity -> this.repository.save(entity)).count();
	}
	
	public Mono<Long> importHkSymbols() {
		return this.hkexConnector.importSymbols().filter(dto -> filter(dto))
				.flatMap(myDto -> this.convert(myDto))
				.flatMap(entity -> this.repository.save(entity)).count();
	}
	
	private Flux<SymbolEntity> convert(HkSymbolImportDto dto) {		
		return Flux.just(new SymbolEntity(null, String.format("%s.HKG", dto.getSymbol()), dto.getName()));
	}
	
	private boolean filter(HkSymbolImportDto dto) {
		long symbol = Long.parseLong(dto.getSymbol());
		return symbol < 10000;
	}
	
	private boolean filter(String line) {
		if(line.isBlank() 
			|| line.contains("ACT Symbol|Security Name|Exchange|")
			|| line.contains("File Creation Time:")
			|| line.contains("Symbol|Security Name|Market Category|")) {
			return false;
		}
		return true;
	}
	
	private Mono<SymbolEntity> convert(String symbolLine) {
		String[] strParts = symbolLine.split("\\|");
		SymbolEntity entity = new SymbolEntity(null, 
				strParts[0].substring(0, strParts[0].length() < 15 ? strParts[0].length() : 15), 
				strParts[1].substring(0, strParts[1].length() < 100 ? strParts[1].length() : 100));		
		return Mono.just(entity);
	}
}
