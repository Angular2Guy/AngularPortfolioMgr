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

import java.util.Map;


public class SymbolToCikWrapperDto {
    private Map<String, CompanySymbolDto> companies;
    
    public static class CompanySymbolDto {
        private Long cik_str;
        private String ticker;
        private String title;
        
        // Getters and Setters
        public Long getCik_str() {
            return cik_str;
        }
        
        public void setCik_str(Long cik_str) {
            this.cik_str = cik_str;
        }
        
        public String getTicker() {
            return ticker;
        }
        
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
        
    public Map<String, CompanySymbolDto> getCompanies() {
        return companies;
    }
    
    public void setCompanies(Map<String, CompanySymbolDto> companies) {
        this.companies = companies;
    }
}
