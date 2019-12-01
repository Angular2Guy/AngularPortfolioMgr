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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.dto.PortfolioDto;
import ch.xxx.manager.service.PortfolioService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("rest/portfolio")
public class PortfolioController {
	@Autowired
	private PortfolioService portfolioService;
	
	@GetMapping("/userid/{userId}")
	public Flux<PortfolioDto> getPortfolioByUserId(@PathVariable("userId") Long userId) {
		return this.portfolioService.getPortfolioByUserId(userId);
	}
	
	@PostMapping
	public Mono<Boolean> createPortfolio(@RequestBody PortfolioDto dto) {
		return this.portfolioService.addPortfolio(dto);
	}
	
	@PostMapping("/symbol/{symbolId}")
	public Mono<Boolean> addSymbolToPortfolio(@RequestBody PortfolioDto dto, Long symbolId) {
		return this.portfolioService.addSymbolToPortfolio(dto, symbolId);
	}
}
