package org.asciidoctor.diagram.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.view.ThemeUtils;

public class StrucurizrSupportPre5 implements StructurizrSupport {
    @Override
    public StructurizrDslParser createParser(boolean secure) {
        StructurizrDslParser parser = new StructurizrDslParser();
        parser.setRestricted(secure);
        return parser;
    }

    @Override
    public void loadThemes(Workspace workspace, boolean secure) throws Exception {
        ThemeUtils.loadThemes(workspace);
    }
}
