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
package ch.xxx.manager.adapter.connector;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class NasdaqConnector {
	private static final Logger LOGGER = LoggerFactory.getLogger(NasdaqConnector.class);
	private static final String HOST = "ftp.nasdaqtrader.com";
	private static final String DIR = "/symboldirectory/";
	private static final List<String> IMPORT_FILES = Arrays.asList("nasdaqlisted.txt", "otherlisted.txt");

	public Flux<String> importSymbols() {
		FTPClient ftp = new FTPClient();
		ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		try {
			ftp.setStrictReplyParsing(false);
			ftp.connect(HOST);			
			ftp.enterLocalPassiveMode();
			ftp.login("anonymous", "sven@gmx.de");
			if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
				ftp.disconnect();
				throw new SocketException(String.format("Failed to connect to %s", HOST));
			}
			FTPFile[] files = ftp.listFiles(DIR);
			if (2 != Arrays.stream(files).map(FTPFile::getName).filter(file -> IMPORT_FILES.contains(file)).count()) {
				throw new FileNotFoundException(
						String.format("Files: %s, %s", IMPORT_FILES.get(0), IMPORT_FILES.get(1)));
			}
		} catch (IOException e) {			
			throw new RuntimeException(e);
		}
		return Flux.concat(this.importSymbols(IMPORT_FILES.get(0), ftp), this.importSymbols(IMPORT_FILES.get(1), ftp))
				.doAfterTerminate(() -> {
					try {
						ftp.logout();
						ftp.disconnect();
					} catch (IOException e) {
						throw new RuntimeException(String.format("Failed to close ftp connection to: %s", HOST));
					}
				});
	}

	private Flux<String> importSymbols(String fileName, FTPClient ftp) {
		String[] symbols = new String[0];
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			if(ftp.retrieveFile(String.format("/%s/%s", DIR, fileName), baos)) {
				LOGGER.info("File imported: {}", fileName);
			} else {
				LOGGER.warn("File import failed: {}", fileName);
			}
			symbols = baos.toString(Charset.defaultCharset()).split("\\r?\\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return Flux.fromArray(symbols);
	}
}
