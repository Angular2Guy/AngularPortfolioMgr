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
package ch.xxx.manager.contoller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.service.PortfolioService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("rest/portfolio")
public class PortfolioController {
	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioController.class);
	
	@Autowired
	private PortfolioService portfolioService;

	@GetMapping("/userid/{userId}")
	public Flux<PortfolioDto> getPortfoliosByUserId(@PathVariable("userId") Long userId) {
		return this.portfolioService.getPortfoliosByUserId(userId);
	}

	@GetMapping("/id/{portfolioId}")
	public Mono<PortfolioDto> getPortfoliosById(@PathVariable("portfolioId") Long portfolioId) {
		return this.portfolioService.getPortfolioById(portfolioId);
	}

	@PostMapping
	public Mono<PortfolioDto> createPortfolio(@RequestBody PortfolioDto dto) {
		return this.portfolioService.addPortfolio(dto);
	}

	@PostMapping("/symbol/{symbolId}/weight/{weight}")
	public Mono<PortfolioDto> addSymbolToPortfolio(@RequestBody PortfolioDto dto, @PathVariable("symbolId") Long symbolId,
			@PathVariable("weight") Long weight, @RequestParam String changedAt) {
		return this.portfolioService.addSymbolToPortfolio(dto, symbolId, weight, this.isoDateTimeToLocalDateTime(changedAt));
	}

	@PutMapping("/symbol/{symbolId}/weight/{weight}")
	public Mono<PortfolioDto> updateSymbolToPortfolio(@RequestBody PortfolioDto dto, @PathVariable("symbolId") Long symbolId,
			@PathVariable("weight") Long weight, @RequestParam String changedAt) {
		return this.portfolioService.updatePortfolioSymbolWeight(dto, symbolId, weight, this.isoDateTimeToLocalDateTime(changedAt));
	}

	@DeleteMapping("/{id}/symbol/{symbolId}")
	public Mono<PortfolioDto> deleteSymbolFromPortfolio(@PathVariable("id") Long portfolioId,
			@PathVariable("symbolId") Long symbolId, @RequestParam String removedAt) {
		return this.portfolioService.removeSymbolFromPortfolio(portfolioId, symbolId, this.isoDateTimeToLocalDateTime(removedAt));
	}
	
	private LocalDateTime isoDateTimeToLocalDateTime(String isoString) {
		if(isoString == null || isoString.trim().isBlank()) {
			return LocalDateTime.now();
		}
		String changedAtStr = UriUtils.decode(isoString, StandardCharsets.UTF_8.name());
		LOGGER.info(changedAtStr);
		return LocalDateTime.parse(changedAtStr, DateTimeFormatter.ISO_DATE_TIME);
	}
}
