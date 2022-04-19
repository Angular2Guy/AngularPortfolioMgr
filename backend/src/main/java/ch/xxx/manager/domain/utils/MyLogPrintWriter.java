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
package ch.xxx.manager.domain.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public class MyLogPrintWriter extends PrintWriter {
	public MyLogPrintWriter(final Logger LOGGER, final Level level) {
		super(new MyLogWriter(LOGGER, level));
	}

	private static class MyLogWriter extends Writer {
		private final Logger LOGGER;
		private final Level level;

		private MyLogWriter(final Logger LOGGER, final Level level) {
			this.LOGGER = LOGGER;
			this.level = level;
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			String line = String.copyValueOf(cbuf, off, len);
			switch (level) {
			case DEBUG -> LOGGER.debug(line);
			case ERROR -> LOGGER.error(line);
			case INFO -> LOGGER.info(line);
			case TRACE -> LOGGER.trace(line);
			case WARN -> LOGGER.warn(line);
			default -> LOGGER.info(line);
			}
		}

		@Override
		public void flush() throws IOException {
			// nothing

		}

		@Override
		public void close() throws IOException {
			// nothing

		}
	}
}
