package org.asciidoctor.diagram;

import java.io.File;

final class Which {
    private Which() {
    }

    public static File which(String tool) {
        String pathext = System.getenv("PATHEXT");
        String[] exts;
        if (pathext == null) {
            exts = new String[] {""};
        } else {
            exts = pathext.split(";");
        }

        String path = System.getenv("PATH");
        String[] paths = path.split(File.pathSeparator);
        for (int i = 0; i < paths.length; i++) {
            File p = new File(paths[i]);
            for (int j = 0; j < exts.length; j++) {
                File f = new File(p, tool + exts[j]);
                if (f.canExecute()) {
                    return f;
                }
            }
        }

        return null;
    }
}
