package org.asciidoctor.diagram.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;

public interface StructurizrSupport {
    StructurizrDslParser createParser(boolean secure);

    void loadThemes(Workspace workspace, boolean secure) throws Exception;
}
