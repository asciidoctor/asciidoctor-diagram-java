package org.asciidoctor.diagram.structurizr;

import org.asciidoctor.diagram.DiagramGeneratorFunction;
import org.asciidoctor.diagram.LazyDiagramGenerator;

public class StructurizrDiagramGenerator extends LazyDiagramGenerator {
    public StructurizrDiagramGenerator() {
        super("structurizr");
    }

    @Override
    protected DiagramGeneratorFunction createGenerator() throws Exception {
        return new Structurizr();
    }
}
