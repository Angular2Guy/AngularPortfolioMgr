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
package ch.xxx.manager.domain.utils;

import java.util.Enumeration;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

public class EnumerationSpliterator<T> extends AbstractSpliterator<T> {

	   private final Enumeration<T> enumeration;

	    public EnumerationSpliterator(long est, int additionalCharacteristics, Enumeration<T> enumeration) {
	        super(est, additionalCharacteristics);
	        this.enumeration = enumeration;
	    }

	    @Override
	    public boolean tryAdvance(Consumer<? super T> action) {
	        if (enumeration.hasMoreElements()) {
	            action.accept(enumeration.nextElement());
	            return true;
	        }
	        return false;
	    }

	    @Override
	    public void forEachRemaining(Consumer<? super T> action) {
	        while (enumeration.hasMoreElements())
	            action.accept(enumeration.nextElement());
	    }

}
