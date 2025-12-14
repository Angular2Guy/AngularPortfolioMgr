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
package ch.xxx.manager.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("kedatest")
@RestController
@RequestMapping("rest/kedatest")
public class KedaTestController {
	private static final Logger LOGGER = LoggerFactory.getLogger(KedaTestController.class);
	
	@GetMapping("/time30s")
	public boolean getResultAfter30Sec() {
		try {
			LOGGER.info("Thread sleeping for 30s.");
			Thread.sleep(30000L);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		LOGGER.info("Thread done.");
		return true;
	}
}
