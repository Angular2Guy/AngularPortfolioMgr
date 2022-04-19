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
package ch.xxx.manager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MapTest {
	@Test
	public void hashMapTest() {
		Map<LocalDate, String> testMap = new HashMap<>();
		LocalDate localDate1 = LocalDate.of(2020, 1, 1);
		LocalDate localDate2 = LocalDate.of(2020, 1, 1);
		LocalDate localDate3 = LocalDate.of(2020, 1, 2);
		LocalDate localDate4 = LocalDate.of(2020, 1, 2);
		testMap.put(localDate1, "test");
		testMap.put(localDate3, "test2");
		Assertions.assertEquals("test", testMap.get(localDate2));
		Assertions.assertEquals("test2", testMap.get(localDate4));
	}
}
