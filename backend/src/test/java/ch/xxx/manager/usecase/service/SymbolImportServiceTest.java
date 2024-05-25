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

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.crypto.tink.DeterministicAead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.TinkJsonProtoKeysetFormat;
import com.google.crypto.tink.daead.DeterministicAeadConfig;

import ch.xxx.manager.domain.model.entity.AppUserRepository;
import ch.xxx.manager.domain.model.entity.DailyQuoteRepository;
import ch.xxx.manager.domain.model.entity.SymbolRepository;

@ExtendWith(MockitoExtension.class)
public class SymbolImportServiceTest {
	private static final String TINK_JSON_KEY = "{\"primaryKeyId\":1312948548,\"key\":[{\"keyData\":{\"typeUrl\":\"type.googleapis.com/google.crypto.tink.AesSivKey\",\"value\":\"EkBLmOTja91pPngXWMaiyvl3R36cYjlUy+0gUuhjC5zDAuuY/QAWpf+u8RAakr9EVQtDdCkqpLrCabqCBUJuYm8Q\",\"keyMaterialType\":\"SYMMETRIC\"},\"status\":\"ENABLED\",\"keyId\":1312948548,\"outputPrefixType\":\"TINK\"}]}";
	private static final String TEST_STRING = "ABCDEFGHIJKLMN";
	private static final UUID TEST_UUID = UUID.randomUUID();
	private String ciphertext;

	@Mock
	private NasdaqClient nasdaqClient;
	@Mock
	private HkexClient hkexClient;
	@Mock
	private SymbolRepository repository;
	@Mock
	private DailyQuoteRepository dailyQuoteRepository;
	@Mock
	private XetraClient xetraClient;
	@Mock
	private QuoteImportService quoteImportService;
	@Mock
	private AppUserRepository appUserRepository;

	@InjectMocks
	private SymbolImportService symbolImportService;

	@BeforeEach
	public void init() throws GeneralSecurityException {
		this.symbolImportService.tinkJsonKey = TINK_JSON_KEY;
		this.symbolImportService.init();

		DeterministicAeadConfig.register();
		KeysetHandle handle = TinkJsonProtoKeysetFormat.parseKeyset(TINK_JSON_KEY, InsecureSecretKeyAccess.get());
		var daead = handle.getPrimitive(DeterministicAead.class);
		this.ciphertext = Base64.getEncoder().encodeToString(daead.encryptDeterministically(TEST_STRING.getBytes(Charset.defaultCharset()),
				TEST_UUID.toString().getBytes(Charset.defaultCharset())));
	}

	@Test
	public void decryptTest() {
		var result = this.symbolImportService.decrypt(this.ciphertext,
				TEST_UUID);		
		Assertions.assertEquals(TEST_STRING, result);
	}
}
