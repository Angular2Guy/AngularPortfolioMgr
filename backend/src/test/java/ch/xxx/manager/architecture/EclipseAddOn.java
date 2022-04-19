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
package ch.xxx.manager.architecture;

import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

public class EclipseAddOn implements ImportOption {
	
    @Override
    public boolean includes(Location location) {
        return MY_TEST_LOCATION.apply(location);
    }
    
	private static final PatternPredicate ECLIPSE_TEST_PATTERN = new PatternPredicate(".*/bin/test/.*");
	
	private static final Predicate<Location> MY_TEST_LOCATION = Predicates.not(ECLIPSE_TEST_PATTERN);
	
	private static final class PatternPredicate implements Predicate<Location> {
        private final Pattern pattern;

        PatternPredicate(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        @Override
        @SuppressWarnings("ConstantConditions") // ArchUnit never uses null as a valid parameter
        public boolean apply(Location input) {
            return input.matches(pattern);
        }
    }
}
