package org.asciidoctor.diagram.plantuml;

import org.asciidoctor.diagram.DiagramGeneratorFunction;
import org.asciidoctor.diagram.LazyDiagramGenerator;

public class PlantUMLDiagramGenerator extends LazyDiagramGenerator {
    public PlantUMLDiagramGenerator() {
        super("plantuml");
    }

    @Override
    protected DiagramGeneratorFunction createGenerator() throws Exception {
        return new PlantUML();
    }
}
