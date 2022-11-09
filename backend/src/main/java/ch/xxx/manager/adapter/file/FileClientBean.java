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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.xxx.manager.domain.file.FileClient;
import ch.xxx.manager.domain.model.entity.dto.SymbolFinancialsDto;
import ch.xxx.manager.usecase.service.AppInfoService;
import ch.xxx.manager.usecase.service.FinancialDataImportService;

@Component
public class FileClientBean implements FileClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileClientBean.class);
	private AppInfoService appInfoService;
	private ObjectMapper objectMapper;
	private FinancialDataImportService financialDataImportService;
	String financialDataImportPath;

	public FileClientBean(AppInfoService appInfoService, ObjectMapper objectMapper, FinancialDataImportService financialDataImportService) {
		this.appInfoService = appInfoService;
		this.objectMapper = objectMapper;
		this.financialDataImportService = financialDataImportService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doOnStartup() {
		this.financialDataImportPath = this.appInfoService.getFinancialDataImportPath();
	}
	
	public Boolean importZipFile(String filename) {
		ZipFile initialFile = null;
		try {
			initialFile = new ZipFile(this.financialDataImportPath + filename);
			Enumeration<? extends ZipEntry> entries = initialFile.entries();
			List<SymbolFinancialsDto> symbolFinancialsDtos = new ArrayList<>(); 
			while (entries.hasMoreElements()) {
				ZipEntry element = entries.nextElement();
				if (!element.isDirectory()) {
					InputStream inputStream = null;
					try {
						LOGGER.info("Filename: {}, Filesize: {}", element.getName(), element.getSize());
						inputStream = initialFile.getInputStream(element);
						String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
						SymbolFinancialsDto symbolFinancialsDto = this.objectMapper.readValue(text, SymbolFinancialsDto.class);
						symbolFinancialsDtos.add(symbolFinancialsDto);
//						LOGGER.info(symbolFinancialsDto.toString());
//						LOGGER.info(text != null ? text.substring(0, 100) : "");
					} finally {
						inputStream.close();
					}
				}
				if(symbolFinancialsDtos.size() > 1000 || !entries.hasMoreElements()) {
					this.financialDataImportService.storeFinancialsData(symbolFinancialsDtos);
					symbolFinancialsDtos.clear();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.closeFile(initialFile);
		}
		return true;
	}

	private void closeFile(ZipFile zipFile) {
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
