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
package ch.xxx.manager.common.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.xxx.manager.common.entity.AppUser;
import ch.xxx.manager.stocks.entity.Symbol;

public interface JpaAppUserRepository extends JpaRepository<AppUser, Long> {
		@Query("select au from AppUser au where au.userName = :username")
		Optional<AppUser> findByUsername(@Param(value = "username") String username);
		@Query("select au from AppUser au where au.uuid = :uuid")
		Optional<AppUser> findByUuid(@Param(value = "uuid") String uuid);
		@Query("select distinct(s) from AppUser au join au.portfolios p join p.portfolioToSymbols pts join pts.symbol s where au.id = :userId")
		Set<Symbol> findAllUserSymbolsByAppUserId(@Param(value="userId") Long id);
}
