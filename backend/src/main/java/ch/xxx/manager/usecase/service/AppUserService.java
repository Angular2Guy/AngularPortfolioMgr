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

import java.util.List;
import java.util.Map;

import ch.xxx.manager.domain.model.dto.AppUserDto;
import ch.xxx.manager.domain.model.dto.KafkaEventDto;
import ch.xxx.manager.domain.model.dto.RefreshTokenDto;
import ch.xxx.manager.domain.utils.TokenSubjectRole;

public interface AppUserService {
	void updateLoggedOutUsers();
	RefreshTokenDto refreshToken(String bearerToken);
	AppUserDto save(AppUserDto appUser);
	Boolean signin(AppUserDto appUserDto);
	Boolean confirmUuid(String uuid);
	AppUserDto login(AppUserDto appUserDto);
	Boolean logout(String bearerStr);
	TokenSubjectRole getTokenRoles(Map<String, String> headers);
	AppUserDto loadById(Long id);
	List<AppUserDto> loadAll();
	AppUserDto loadByName(String name, boolean showApiKeys);
	void sendKafkaEvent(KafkaEventDto kafkaEventDto);
	void cleanup();
	void eventRetry();
}
