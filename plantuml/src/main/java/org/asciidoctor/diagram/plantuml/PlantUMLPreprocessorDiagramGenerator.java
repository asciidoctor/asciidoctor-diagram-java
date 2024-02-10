package org.asciidoctor.diagram.plantuml;

import org.asciidoctor.diagram.DiagramGeneratorFunction;
import org.asciidoctor.diagram.LazyDiagramGenerator;

public class PlantUMLPreprocessorDiagramGenerator extends LazyDiagramGenerator {
    public PlantUMLPreprocessorDiagramGenerator() {
        super("plantumlpreprocessor");
    }

    @Override
    protected DiagramGeneratorFunction createGenerator() throws Exception {
        return new PlantUMLPreprocessor();
    }
}
