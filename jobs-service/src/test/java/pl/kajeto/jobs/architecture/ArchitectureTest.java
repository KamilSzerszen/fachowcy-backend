package pl.kajeto.jobs.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "pl.kajeto.jobs", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule controllersDoNotUseRepositoriesDirectly =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("pl.kajeto.jobs.persistence..");

  @ArchTest
  static final ArchRule domainDoesNotDependOnOuterLayers =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "pl.kajeto.jobs.web..", "pl.kajeto.jobs.persistence..", "pl.kajeto.jobs.config..");

  @ArchTest
  static final ArchRule persistenceDoesNotDependOnWeb =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.persistence..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("pl.kajeto.jobs.web..");

  @ArchTest
  static final ArchRule restControllersLiveInWebPackage =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("pl.kajeto.jobs.web..");

  @ArchTest
  static final ArchRule controllersAreNamedController =
      classes()
          .that()
          .resideInAPackage("pl.kajeto.jobs.web..")
          .and()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller");
}
