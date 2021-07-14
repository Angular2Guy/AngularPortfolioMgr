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
