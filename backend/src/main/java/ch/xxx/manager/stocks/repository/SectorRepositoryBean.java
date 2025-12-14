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
package ch.xxx.manager.stocks.repository;

import org.springframework.stereotype.Repository;

import ch.xxx.manager.stocks.entity.Sector;
import ch.xxx.manager.stocks.entity.SectorRepository;

@Repository
public class SectorRepositoryBean implements SectorRepository {
	private JpaSectorRepository jpaSectorRepository;
	
	public SectorRepositoryBean(JpaSectorRepository jpaSectorRepository) {
		this.jpaSectorRepository = jpaSectorRepository;
	}
	
	@Override
	public Sector save(Sector sector) {
		return this.jpaSectorRepository.save(sector);
	}

}
