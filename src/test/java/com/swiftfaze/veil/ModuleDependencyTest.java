package com.swiftfaze.veil;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleDependencyTest {

    private static final String UI_PACKAGE = "com.swiftfaze.veil.ui";
    private static final String WIDGET_PACKAGE = UI_PACKAGE + ".widget";
    private static final String SANDBOX_PACKAGE = "com.swiftfaze.veil.sandbox";

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.swiftfaze.veil");

    @Test
    void engineCodeMustNotDependOnUi() {
        ArchRule rule = noClasses()
                .that(resideOutsideOfPackage(UI_PACKAGE + "..")
                        .and(resideOutsideOfPackage(SANDBOX_PACKAGE + ".."))
                        .and(not(equivalentTo(Main.class))))
                .should().dependOnClassesThat().resideInAPackage(UI_PACKAGE + "..")
                .because("engine code must not depend on the UI layer; Main is the composition root "
                        + "that wires UI together, and sandbox/ is a dev-only UI tool that legitimately "
                        + "reuses ui.widget classes");
        rule.check(classes);
    }

    @Test
    void widgetsMustNotDependOnScreens() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(WIDGET_PACKAGE)
                .should().dependOnClassesThat().resideInAPackage(UI_PACKAGE)
                .because("widget classes must not depend on screen classes that sit directly in "
                        + "com.swiftfaze.veil.ui; screens may depend on widgets, not the reverse");
        rule.check(classes);
    }
}
