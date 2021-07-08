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
package ch.xxx.manager.adapter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ch.xxx.manager.domain.model.entity.Currency;
import ch.xxx.manager.domain.utils.CurrencyKey;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
	@Query("select c from Currency c where c.toCurrKey = :toCurr")
	Optional<Currency> findByToCurr(CurrencyKey toCurr);	
}
