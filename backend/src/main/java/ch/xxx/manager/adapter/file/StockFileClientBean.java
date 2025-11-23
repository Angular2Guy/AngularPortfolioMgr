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
package ch.xxx.manager.adapter.file;

import ch.xxx.manager.domain.file.FileClient;
import ch.xxx.manager.domain.model.entity.Symbol;
import ch.xxx.manager.domain.model.entity.Symbol.QuoteSource;
import ch.xxx.manager.domain.model.entity.dto.DailyQuoteImportDto;
import ch.xxx.manager.usecase.service.AppInfoService;
import ch.xxx.manager.usecase.service.QuoteImportService;
import ch.xxx.manager.usecase.service.SymbolImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component(value = "Stock")
public class StockFileClientBean implements FileClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(StockFileClientBean.class);
	private volatile boolean importDone = true;
	private final AppInfoService appInfoService;
	private final CsvMapper csvMapper;
	private final QuoteImportService quoteImportService;
	private final SymbolImportService symbolImportService;
	@Value("${ssd.io:false}")
	private boolean ssdIo;
	String financialDataImportPath;

	public StockFileClientBean(AppInfoService appInfoService, QuoteImportService quoteImportService,
			CsvMapper csvMapper, SymbolImportService symbolImportService) {
		this.appInfoService = appInfoService;
		this.csvMapper = csvMapper;
		this.quoteImportService = quoteImportService;
		this.symbolImportService = symbolImportService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doOnStartup() {
		this.financialDataImportPath = this.appInfoService.getFinancialDataImportPath();
	}

	@Override
	public Boolean importZipFile(String filename) {
//		LOGGER.info("Import start.");
		if (!this.importDone) {
			return false;
		}
		var start = new Date();
		this.importDone = false;
		Thread shutDownThread = createShutDownThread();
		LOGGER.info("Delete old Symbols.");
		List<Symbol> symbolsToDelete = this.symbolImportService.findSymbolsByQuoteSource(QuoteSource.DATA);
		this.symbolImportService.deleteSymbolsWithDailyQuotes(symbolsToDelete);
		LOGGER.info("Old Symbols deleted in {}ms", new Date().getTime() - start.getTime());
		Runtime.getRuntime().addShutdownHook(shutDownThread);
		try (ZipFile initialFile = new ZipFile(this.financialDataImportPath + filename)) {
			Enumeration<? extends ZipEntry> entries = initialFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry element = entries.nextElement();
				LocalDateTime startElement = LocalDateTime.now();
				List<DailyQuoteImportDto> dailyQuoteImportDtos = new ArrayList<>();
				if (!element.isDirectory() && element.getSize() > 10) {
					try (InputStream inputStream = initialFile.getInputStream(element)) {
						LOGGER.info("Filename: {}, Filesize: {}", element.getName(), element.getSize());
						String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
						dailyQuoteImportDtos = this.csvMapper.readerFor(DailyQuoteImportDto.class)
								.with(CsvSchema.builder().setUseHeader(true).build())
								.<DailyQuoteImportDto>readValues(text).readAll().stream()
								.filter(myDQ -> myDQ.getClose() != null && Optional.ofNullable(myDQ.getSymbol())
										.filter(mySym -> !mySym.isBlank()).isPresent())
								.toList();
					} catch (Exception e) {
						LOGGER.info("Exception with file: {}", element.getName(), e);
					}
				}
//				if ((this.ssdIo ? dailyQuoteImportDtos.size() >= 25000 : dailyQuoteImportDtos.size() >= 5000)
//						|| !entries.hasMoreElements()) {
				this.quoteImportService.storeDailyQuoteData(dailyQuoteImportDtos);
				LOGGER.info("Persist time: {}, imported Rows: {}",
						ChronoUnit.MILLIS.between(startElement, LocalDateTime.now()), dailyQuoteImportDtos.size());
//				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		Runtime.getRuntime().removeShutdownHook(shutDownThread);
		LOGGER.info("Import Done in {}ms", new Date().getTime() - start.getTime());
		this.importDone = true;
		return true;
	}

	private Thread createShutDownThread() {
		return new Thread(() -> {
			while (!this.importDone) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					LOGGER.warn("ShutdownHook Thread interrupted.", e);
				}
			}
			LOGGER.info("ShutdownHook Thread is Done.");
		});
	}
}
