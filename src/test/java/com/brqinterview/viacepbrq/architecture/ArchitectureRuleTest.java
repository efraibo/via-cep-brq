package com.brqinterview.viacepbrq.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "com.brqinterview.viacepbrq",
        importOptions = {ImportOption.DoNotIncludeJars.class, ImportOption.DoNotIncludeTests.class})
public class ArchitectureRuleTest {

    @ArchTest
    static final ArchRule dependencias_camadas_sao_respeitadas = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controllers").definedBy("..controllers..")
            .layer("Services").definedBy("..services..")
            .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
            .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers")
            .because("Controllers não pode ser acessado por outra camada, " +
                    "Services podem ser acessadas apenas pelo controller");

    @ArchTest
    public static final ArchRule controllers_deve_contem_annotation_restcontroller = ArchRuleDefinition//
            .classes().that().resideInAPackage("..controllers..")//
            .should().beAnnotatedWith(RestController.class)//
            .andShould().beAnnotatedWith(RequestMapping.class)//
            .because("controllers deve conter annotation RestController");//

    @ArchTest
    public static final ArchRule services_deve_contem_annotation_especificos = ArchRuleDefinition//
            .classes().that().resideInAPackage("..services..")//
            .should().beAnnotatedWith(Service.class)//
            .because("services deve conter annotation Services");//

    @ArchTest
    public static final ArchRule services_deve_contem_annotation_service = ArchRuleDefinition//
            .classes().that().resideInAPackage("com.brqinterview.viacepbrq.services")//
            .should().beAnnotatedWith(Service.class)//
            .because("services deve conter annotation Service");//

    @ArchTest
    public static final ArchRule classes_de_services_devem_respeitar_camadas = ArchRuleDefinition.classes()
            .that().resideInAPackage("..services..")
            .should().onlyBeAccessed().byAnyPackage("..controllers..", "..services..");

    @ArchTest
    static final ArchRule pacotes_services_devem_ser_respeitados = ArchRuleDefinition
            .classes().that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..services..")
            .because("Classes com terminologia Service devem está dentro do pacote com.brqinterview.viacepbrq.services");

    @ArchTest
    static final ArchRule pacotes_controllers_devem_ser_respeitados = ArchRuleDefinition
            .classes().that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..controllers..")
            .because("Classes com terminologia Controller devem está dentro do pacote com.brqinterview.viacepbrq.controllers");
}
