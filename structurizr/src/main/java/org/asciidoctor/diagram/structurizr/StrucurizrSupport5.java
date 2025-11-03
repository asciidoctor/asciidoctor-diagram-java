package org.asciidoctor.diagram.structurizr;

import com.structurizr.Workspace;
import com.structurizr.dsl.Features;
import com.structurizr.dsl.StructurizrDslParser;
import com.structurizr.http.HttpClient;
import com.structurizr.view.ThemeUtils;

public class StrucurizrSupport5 implements StructurizrSupport {
    static {
        // Access a 5.x only class during class initialisation to fail early
        new Features();
    }

    @Override
    public StructurizrDslParser createParser(boolean secure) {
        StructurizrDslParser parser = new StructurizrDslParser();
        if (secure) {
            Features features = parser.getFeatures();
            features.disable(Features.ENVIRONMENT);
            features.disable(Features.FILE_SYSTEM);
            features.disable(Features.PLUGINS);
            features.disable(Features.SCRIPTS);
            features.disable(Features.COMPONENT_FINDER);
            features.disable(Features.DOCUMENTATION);
            features.disable(Features.DECISIONS);
            features.disable(Features.HTTP);
            features.disable(Features.HTTPS);
        }
        return parser;
    }

    @Override
    public void loadThemes(Workspace workspace, boolean secure) throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.setTimeout(10000);
        if (!secure) {
            httpClient.allow(".*");
        }

        ThemeUtils.loadThemes(workspace, httpClient);
    }
}
