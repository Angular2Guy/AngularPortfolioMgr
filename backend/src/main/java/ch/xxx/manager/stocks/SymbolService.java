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
package ch.xxx.manager.stocks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.xxx.manager.common.file.FileClient;
import ch.xxx.manager.stocks.dto.ImportDataDto;
import ch.xxx.manager.stocks.dto.SymbolDto;
import ch.xxx.manager.stocks.entity.SymbolRepository;
import ch.xxx.manager.stocks.mapping.SymbolMapper;

@Service
@Transactional
public class SymbolService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SymbolService.class);
	private final SymbolRepository repository;
	private final SymbolMapper symbolMapper;
	private final FileClient fileClient;

	public SymbolService(SymbolRepository repository, SymbolMapper symbolMapper, @Qualifier("Sec") FileClient fileClient) {
		this.repository = repository;
		this.symbolMapper = symbolMapper;
		this.fileClient = fileClient;
	}

	public List<SymbolDto> getAllSymbols() {
		return this.repository.findAll().stream().flatMap(symbol -> Stream.of(this.symbolMapper.convert(symbol)))
				.collect(Collectors.toList());
	}

	public List<SymbolDto> getSymbolBySymbol(String symbol) {
		return Optional.ofNullable(symbol).filter(mySymbol -> mySymbol.trim().length() >= 2)
				.map(mySymbol -> this.repository.findBySymbol(mySymbol.trim().toLowerCase()).stream()
						.flatMap(entity -> Stream.of(this.symbolMapper.convert(entity))).collect(Collectors.toList()))
				.orElse(List.of());
	}

	public List<SymbolDto> getSymbolByName(String name) {
		return Optional.ofNullable(name).filter(myName -> myName.trim().length() >= 2)
				.map(myName -> this.repository.findByName(myName.trim().toLowerCase()).stream()
						.flatMap(entity -> Stream.of(this.symbolMapper.convert(entity))).collect(Collectors.toList()))
				.orElse(List.of());
	}
	
	@Async
	public void importFinancialData(ImportDataDto importFinancialDataDto) {
		try {
			this.fileClient.importZipFile(importFinancialDataDto.getFilename());
		} catch (Exception e) {
			LOGGER.warn("importFinancialData failed.", e);
		}
		LOGGER.info("Financial Data import finished.");
	}
}
