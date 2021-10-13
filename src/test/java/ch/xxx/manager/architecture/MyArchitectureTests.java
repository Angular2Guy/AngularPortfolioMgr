/**
 *    Copyright 2016 Sven Loesekann

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

import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;

import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

//import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@AnalyzeClasses(packages = "ch.xxx.manager", importOptions = { DoNotIncludeTests.class, EclipseAddOn.class })
public class MyArchitectureTests {
	private static final ArchRule NO_CLASSES_SHOULD_USE_FIELD_INJECTION = createNoFieldInjectionRule();

	private JavaClasses importedClasses = new ClassFileImporter().importPackages("ch.xxx.manager");

	@ArchTest
	static final ArchRule clean_architecture_respected = Architectures.onionArchitecture().domainModels("..domain..")
			.applicationServices("..usecase..", "..dev.usecase..", "..prod.usecase..")
			.adapter("rest", "..adapter.controller..", "..dev.adapter.controller..", "..prod.adapter.controller..")
			.adapter("cron", "..adapter.cron..").adapter("repo", "..adapter.repository..")
			.adapter("client", "..adapter.client..").adapter("config", "..adapter.config..").withOptionalLayers(true);

	@ArchTest
	static final ArchRule devDependencies = ArchRuleDefinition.noClasses().that().resideInAPackage("..dev..").should()
			.dependOnClassesThat().resideInAPackage("..prod..");

	@ArchTest
	static final ArchRule prodDependencies = ArchRuleDefinition.noClasses().that().resideInAPackage("..prod..").should()
			.dependOnClassesThat().resideInAPackage("..dev..");
	
	@ArchTest
	static final ArchRule cyclesDomain = SlicesRuleDefinition.slices().matching("..domain.(*)..").should()
			.beFreeOfCycles();

	@ArchTest
	static final ArchRule cyclesUseCases = SlicesRuleDefinition.slices().matching("..usecase.(*)..").should()
			.beFreeOfCycles();

	@ArchTest
	static final ArchRule cyclesAdapter = SlicesRuleDefinition.slices().matching("..adapter.(*)..").should()
			.beFreeOfCycles();

	@ArchTest
	static final ArchRule cyclesDevUseCases = SlicesRuleDefinition.slices().matching("..dev.usecase.(*)..").should()
			.beFreeOfCycles();

	@ArchTest
	static final ArchRule cyclesDevAdapter = SlicesRuleDefinition.slices().matching("..prod.adapter.(*)..").should()
			.beFreeOfCycles();

	@ArchTest
	static final ArchRule cyclesProdUseCases = SlicesRuleDefinition.slices().matching("..dev.usecase.(*)..").should()
			.beFreeOfCycles();

	@ArchTest
	static final ArchRule cyclesProdAdapter = SlicesRuleDefinition.slices().matching("..prod.adapter.(*)..").should()
			.beFreeOfCycles();

	@Test
	public void ruleControllerAnnotations() {
		List.of("..adapter.controller..", "..dev.adapter.controller..", "..prod.adapter.controller..")
				.forEach(myPackage -> {
					ArchRule beAnnotatedWith = ArchRuleDefinition.classes().that()
							.resideInAPackage("..adapter.controller..").should().beAnnotatedWith(RestController.class)
							.orShould().beAnnotatedWith(Configuration.class);
					beAnnotatedWith.check(this.importedClasses);
				});
	}

	@Test
	public void ruleExceptionsType() {
		ArchRule exceptionType = ArchRuleDefinition.classes().that().resideInAPackage("..domain.exceptions..").should()
				.beAssignableTo(RuntimeException.class).orShould().beAssignableTo(DefaultErrorAttributes.class);
		exceptionType.check(this.importedClasses);
	}

	@Test
	public void ruleCronJobMethodsAnnotations() {
		ArchRule exceptionType = ArchRuleDefinition.methods().that().arePublic().and().areDeclaredInClassesThat()
				.resideInAPackage("..adapter.cron..").should().beAnnotatedWith(Scheduled.class).andShould()
				.beAnnotatedWith(SchedulerLock.class).orShould().beAnnotatedWith(PostConstruct.class);
		exceptionType.check(this.importedClasses);
	}

	@Test
	public void ruleGeneralCodingRulesLoggers() {
		JavaClasses classesToCheck = this.importedClasses
				.that(JavaClass.Predicates.resideOutsideOfPackages("..domain.utils.."));
		ArchRuleDefinition.fields().that().haveRawType(Logger.class).should().bePrivate().andShould().beStatic()
				.andShould().beFinal().because("we agreed on this convention").check(classesToCheck);
	}

	@Test
	public void ruleGeneralCodingRules() {
		ArchRule archRule = CompositeArchRule.of(GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS)
				.and(NO_CLASSES_SHOULD_USE_FIELD_INJECTION).because("Good practice");
		JavaClasses classesToCheck = this.importedClasses
				.that(JavaClass.Predicates.resideOutsideOfPackages("..adapter.clients.test.."));
		archRule.check(classesToCheck);
	}

	private static ArchRule createNoFieldInjectionRule() {
		ArchCondition<JavaField> annotatedWithSpringAutowired = beAnnotatedWith(
				"org.springframework.beans.factory.annotation.Autowired");
		ArchCondition<JavaField> annotatedWithGuiceInject = beAnnotatedWith("com.google.inject.Inject");
		ArchCondition<JavaField> annotatedWithJakartaInject = beAnnotatedWith("javax.inject.Inject");
		ArchRule beAnnotatedWithAnInjectionAnnotation = ArchRuleDefinition.noFields()
				.should(annotatedWithSpringAutowired.or(annotatedWithGuiceInject).or(annotatedWithJakartaInject)
						.as("be annotated with an injection annotation"));
		return beAnnotatedWithAnInjectionAnnotation;
	}
}