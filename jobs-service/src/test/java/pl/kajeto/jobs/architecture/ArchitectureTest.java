package pl.fachowcy.jobs.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "pl.fachowcy.jobs", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule controllersDoNotUseRepositoriesDirectly =
      noClasses()
          .that()
          .resideInAPackage("pl.fachowcy.jobs.web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("pl.fachowcy.jobs.persistence..");

  @ArchTest
  static final ArchRule domainDoesNotDependOnOuterLayers =
      noClasses()
          .that()
          .resideInAPackage("pl.fachowcy.jobs.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "pl.fachowcy.jobs.web..",
              "pl.fachowcy.jobs.persistence..",
              "pl.fachowcy.jobs.config..");

  @ArchTest
  static final ArchRule persistenceDoesNotDependOnWeb =
      noClasses()
          .that()
          .resideInAPackage("pl.fachowcy.jobs.persistence..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("pl.fachowcy.jobs.web..");

  @ArchTest
  static final ArchRule restControllersLiveInWebPackage =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("pl.fachowcy.jobs.web..");

  @ArchTest
  static final ArchRule controllersAreNamedController =
      classes()
          .that()
          .resideInAPackage("pl.fachowcy.jobs.web..")
          .and()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller");
}
