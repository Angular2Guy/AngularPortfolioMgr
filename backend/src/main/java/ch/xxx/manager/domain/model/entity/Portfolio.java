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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import ch.xxx.manager.domain.utils.DataHelper;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@DiscriminatorValue("1")
public class Portfolio extends PortfolioBase {
	@ManyToOne
	private AppUser appUser;
	@NotBlank
	@Size(max=255)
	private String name;
	@NotNull
	@Enumerated(EnumType.STRING)	
	private DataHelper.CurrencyKey currencyKey;
	@NotNull
	private LocalDate createdAt;
	@OneToMany(mappedBy = "portfolio")
	private Set<PortfolioToSymbol> portfolioToSymbols = new HashSet<>();
	@OneToMany(mappedBy = "portfolio", orphanRemoval = true)
	private Set<PortfolioElement> portfolioElements = new HashSet<>();

	@PrePersist
	void init() {
		this.createdAt = LocalDate.now();
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

	public Set<PortfolioToSymbol> getPortfolioToSymbols() {
		return portfolioToSymbols;
	}

	public void setPortfolioToSymbols(Set<PortfolioToSymbol> portfolioToSymbols) {
		this.portfolioToSymbols = portfolioToSymbols;
	}

	public DataHelper.CurrencyKey getCurrencyKey() {
		return currencyKey;
	}

	public void setCurrencyKey(DataHelper.CurrencyKey currencyKey) {
		this.currencyKey = currencyKey;
	}

	public Set<PortfolioElement> getPortfolioElements() {
		return portfolioElements;
	}

	public void setPortfolioElements(Set<PortfolioElement> portfolioElements) {
		this.portfolioElements = portfolioElements;
	}
}
