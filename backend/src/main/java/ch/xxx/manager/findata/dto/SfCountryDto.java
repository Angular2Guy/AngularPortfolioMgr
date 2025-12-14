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
package ch.xxx.manager.findata.dto;

public class SfCountryDto {
	private String country;
	private Long timesFound;
	
	public SfCountryDto() {}
	
	public SfCountryDto(String country, Long timesFound) {
		super();
		this.country = country;
		this.timesFound = timesFound;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public Long getTimesFound() {
		return timesFound;
	}
	public void setTimesFound(Long timesFound) {
		this.timesFound = timesFound;
	}
}
