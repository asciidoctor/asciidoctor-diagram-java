package org.asciidoctor.diagram.syntrax;

import org.asciidoctor.diagram.DiagramGeneratorFunction;
import org.asciidoctor.diagram.LazyDiagramGenerator;

public class SyntraxDiagramGenerator extends LazyDiagramGenerator {
    public SyntraxDiagramGenerator() {
        super("syntrax");
    }

    @Override
    protected DiagramGeneratorFunction createGenerator() throws Exception {
        return new Syntrax();
    }
}
