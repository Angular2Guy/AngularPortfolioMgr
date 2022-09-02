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
package ch.xxx.manager.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RapidAssetProfileDto {
	private String sector;
	private String industry;
	private Long fullTimeEmployees;
	private String longBusinessSummary;
	private String city;
	private String phone;
	private String country;
	private String address1;
	private String address2;

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public Long getFullTimeEmployees() {
		return fullTimeEmployees;
	}

	public void setFullTimeEmployees(Long fullTimeEmployees) {
		this.fullTimeEmployees = fullTimeEmployees;
	}

	public String getLongBusinessSummary() {
		return longBusinessSummary;
	}

	public void setLongBusinessSummary(String longBusinessSummary) {
		this.longBusinessSummary = longBusinessSummary;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	@Override
	public String toString() {
		return "RapidAssetProfile [sector=" + sector + ", fullTimeEmployees=" + fullTimeEmployees
				+ ", longBusinessSummary=" + longBusinessSummary + ", city=" + city + ", phone=" + phone + ", country="
				+ country + ", address1=" + address1 + ", industry=" + industry + ", address2=" + address2 + "]";
	}

}
