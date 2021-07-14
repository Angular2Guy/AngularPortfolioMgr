package ch.xxx.manager.adapter.cron;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ch.xxx.manager.usecase.service.AlphavatageClient;
import ch.xxx.manager.usecase.service.HkexClient;
import ch.xxx.manager.usecase.service.NasdaqClient;
import ch.xxx.manager.usecase.service.SymbolImportService;
import ch.xxx.manager.usecase.service.XetraClient;
import ch.xxx.manager.usecase.service.YahooClient;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class CronJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(CronJob.class);
	private final HkexClient hkexClient;
	private final XetraClient xetraClient;
	private final NasdaqClient nasdaqClient;
	private final YahooClient yahooClient;
	private final AlphavatageClient alphavatageClient;
	private final SymbolImportService symbolImportService;

	public CronJob(HkexClient hkexClient, XetraClient xetraClient, NasdaqClient nasdaqClient, YahooClient yahooClient,
			AlphavatageClient alphavatageClient, SymbolImportService symbolImportService) {
		this.hkexClient = hkexClient;
		this.xetraClient = xetraClient;
		this.nasdaqClient = nasdaqClient;
		this.yahooClient = yahooClient;
		this.alphavatageClient = alphavatageClient;
		this.symbolImportService = symbolImportService;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("init called");
	}
	
	@Scheduled(cron = "0 0 1 * * ?")
	@SchedulerLock(name = "CronJob_scheduledImporter", lockAtLeastFor = "PT10M", lockAtMostFor = "PT2H")
	public void scheduledImporter() {
		this.alphavatageClient.getFxTimeseriesDailyHistory(null, true)
				.subscribe(dto -> LOGGER.info("Import of {} currency quotes finished.", dto.getDailyQuotes().size()));
		this.hkexClient.importSymbols().subscribe(dtos -> {
			this.symbolImportService.importHkSymbols(dtos);
			LOGGER.info("Import of {} hk symbols finished.", dtos.size());
		});
		this.xetraClient.importXetraSymbols().subscribe(dtos -> {
			this.symbolImportService.importDeSymbols(dtos);
			LOGGER.info("Import of {} de symbols finished.", dtos.size());
		});
		this.nasdaqClient.importSymbols().subscribe(dtos -> {
			this.symbolImportService.importDeSymbols(dtos);
			LOGGER.info("Import of {} us symbols finished.", dtos.size());
		});
		this.yahooClient.getTimeseriesDailyHistory(null)
				.subscribe(quotes -> LOGGER.info("Import of {} index symbols finished.", quotes.size()));
		this.alphavatageClient.getTimeseriesDailyHistory(null, true).subscribe(
				dto -> LOGGER.info("Import of {} index symbols finished.", dto.getDailyQuotes().entrySet().size()));
	}
}
