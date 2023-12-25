package com.structurizr.dsl;

import com.structurizr.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class StructurizrDslParserAccessor {
    public static void parse(StructurizrDslParser parser, String dsl, File baseDir) throws StructurizrDslParserException {
        if (StringUtils.isNullOrEmpty(dsl)) {
            throw new StructurizrDslParserException("A DSL fragment must be specified");
        } else {
            List<String> lines = Arrays.asList(dsl.split("\\r?\\n"));
            parser.parse(lines, new File(baseDir, "stdin"));
        }
    }
}
