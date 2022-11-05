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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.xxx.manager.domain.file.FileClient;
import ch.xxx.manager.usecase.service.AppInfoService;

@Component
public class FileClientBean implements FileClient {
	private Logger LOGGER = LoggerFactory.getLogger(FileClientBean.class);
	private AppInfoService appInfoService;

	public FileClientBean(AppInfoService appInfoService) {
		this.appInfoService = appInfoService;
	}

	public Boolean importZipFile(String filename) {
		ZipInputStream zipTargetStream = null;
		try {
			File initialFile = new File(this.appInfoService.getFinancialDataImportPath() + filename);
			zipTargetStream = new ZipInputStream(new FileInputStream(initialFile));
			this.handleZipEntry(zipTargetStream.getNextEntry());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			this.closeStream(zipTargetStream);
		}
		return true;
	}

	private void handleZipEntry(ZipEntry zipEntry) {
		if (!zipEntry.isDirectory()) {
			ZipInputStream zipInputStream = null;
			try {
				LOGGER.info("Filename: {}, Filesize: {}", zipEntry.getName(), zipEntry.getSize());
				File inFile = new File(zipEntry.getName());
				zipInputStream = new ZipInputStream(new FileInputStream(inFile));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				this.closeStream(zipInputStream);
			}
		} else {			
			ZipInputStream zipInputStream = null;
			try {
				File inFile = new File(zipEntry.getName());
				zipInputStream = new ZipInputStream(new FileInputStream(inFile));
				ZipEntry nextEntry = zipInputStream.getNextEntry();
				while(nextEntry != null) {
					this.handleZipEntry(nextEntry);
					nextEntry = zipInputStream.getNextEntry();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				this.closeStream(zipInputStream);
			}
		}
	}

	private void closeStream(ZipInputStream inputStream) {
		if (inputStream != null) {
			try {
				inputStream.closeEntry();
				inputStream.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
