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
package ch.xxx.manager.usecase.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import ch.xxx.manager.domain.file.FileClient;
import ch.xxx.manager.domain.model.dto.ImportFinancialDataDto;


@Service
public class FinancialDataImportService {
	private final FileClient fileClient;
	
	public FinancialDataImportService(FileClient fileClient) {
		this.fileClient = fileClient;
	}
	
	@Async
	public void importFinancialData(ImportFinancialDataDto importFinancialDataDto) {
		this.fileClient.importZipFile(importFinancialDataDto.getFilename());
	}
}
