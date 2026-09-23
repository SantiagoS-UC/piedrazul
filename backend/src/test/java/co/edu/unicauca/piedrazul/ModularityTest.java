package co.edu.unicauca.piedrazul;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(PiedrazulApplication.class);

    // Falla si un módulo usa clases internas de otro o si aparecen dependencias cíclicas.
    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    // Genera en target/spring-modulith-docs los diagramas C4 de componentes para el documento.
    @Test
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
