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
package ch.xxx.manager.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;

@Service
public class IdService {
	private Queue<Long> ids = new LinkedList<>();
	@Autowired
	private DatabaseClient database;

	public Long getId() {
		if (ids.isEmpty()) {
			String statement = "SELECT NEXT VALUE FOR mainseq from appuser;";
			List<Long> newIds = database.execute(statement).as(Long.class).fetch().all().buffer().blockLast();
			for(long i = newIds.get(0)+1; i<newIds.get(0) + 100;i++) {
				newIds.add(i);
			}
			this.ids.addAll(newIds);
		}
		return ids.remove();
	}
}
