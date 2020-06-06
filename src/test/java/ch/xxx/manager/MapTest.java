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
		testMap.put(localDate1, "test");
		Assertions.assertEquals("test", testMap.get(localDate2));
	}
}
