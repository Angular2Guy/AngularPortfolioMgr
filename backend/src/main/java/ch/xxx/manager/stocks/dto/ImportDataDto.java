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
package ch.xxx.manager.stocks.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public class ImportDataDto {
	public enum ImportDataType {
		Sec("Sec"), Stocks("Stocks");

		private final String key;

		private ImportDataType(String key) {
			this.key = key;
		}

		@JsonValue
		public String getKey() {
			return key;
		}
	}

	private String filename;
	private String path;
	private ImportDataType importDataType;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ImportDataType getImportDataType() {
		return importDataType;
	}

	public void setImportDataType(ImportDataType importDataType) {
		this.importDataType = importDataType;
	}
}
