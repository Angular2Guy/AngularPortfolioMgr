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
package ch.xxx.manager.common.entity;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AppUserRepositoryTest {

	@Autowired
	private AppUserRepository appUserRepository;
	
	@Test
	public void findByUsernameFound() {
		String userName = "sven";
		Optional<AppUser> findByUsernameOpt = this.appUserRepository.findByUsername(userName);
		Assertions.assertTrue(findByUsernameOpt.isPresent());
		Assertions.assertEquals(userName, findByUsernameOpt.get().getUserName());
	}
	
	@Test
	public void findByUsernameNotFound() {
		String userName = "XXX";
		Optional<AppUser> findByUsernameOpt = this.appUserRepository.findByUsername(userName);
		Assertions.assertTrue(findByUsernameOpt.isEmpty());
	}
}
