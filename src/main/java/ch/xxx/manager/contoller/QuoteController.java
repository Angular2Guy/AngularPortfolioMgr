package ch.xxx.manager.contoller;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.xxx.manager.dto.DailyQuoteExportDto;
import ch.xxx.manager.service.QuoteService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("rest/quotes")
public class QuoteController {
	@Autowired
	private QuoteService quoteService;
	
	@GetMapping("/symbol/{symbol}")
	public Flux<DailyQuoteExportDto> getDailyQuotes(@PathParam("symbol") String symbol) {
		return this.quoteService.getDailyQuotes(symbol);
	}
}
