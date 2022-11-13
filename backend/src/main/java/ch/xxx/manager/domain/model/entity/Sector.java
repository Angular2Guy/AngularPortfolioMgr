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
package ch.xxx.manager.domain.model.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;

@Entity
public class Sector extends EntityBase {		
	@Size(max=100)
	private String alphavantageName;
	@Size(max=100)
	private String yahooName;
	@OneToMany(mappedBy = "sector", orphanRemoval = true)
	private Set<Symbol> symbols = new HashSet<>();

	public String getAlphavantageName() {
		return alphavantageName;
	}
	public void setAlphavantageName(String alphavantageName) {
		this.alphavantageName = alphavantageName;
	}
	public String getYahooName() {
		return yahooName;
	}
	public void setYahooName(String yahooName) {
		this.yahooName = yahooName;
	}
	public Set<Symbol> getSymbols() {
		return symbols;
	}
	public void setSymbols(Set<Symbol> symbols) {
		this.symbols = symbols;
	}
}
