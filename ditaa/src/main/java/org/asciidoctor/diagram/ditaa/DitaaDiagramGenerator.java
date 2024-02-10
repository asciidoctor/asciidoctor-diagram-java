package org.asciidoctor.diagram.ditaa;

import org.asciidoctor.diagram.DiagramGeneratorFunction;
import org.asciidoctor.diagram.LazyDiagramGenerator;

public class DitaaDiagramGenerator extends LazyDiagramGenerator {
    public DitaaDiagramGenerator() {
        super("ditaa");
    }

    @Override
    protected DiagramGeneratorFunction createGenerator() throws Exception {
        return new Ditaa();
    }
}
