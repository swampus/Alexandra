package io.github.swampus.alexandra;


import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.nureonlang.parser.internal.InternalNureonLangService;
import io.github.swampus.alexandra.nureonlang.parser.impl.InternalNureonLangServiceImpl;
import io.github.swampus.alexandra.nureonlang.parser.impl.SyntaxCheckResult;

public class Examples {
    public static void main(String[] args) {
        InternalNureonLangService nureonLangService = new InternalNureonLangServiceImpl();
        SyntaxCheckResult result =
                nureonLangService.checkSyntax("BEGIN LAYER rr INPUT size=4 END");
        if (result.isValid()) {
            System.out.println("OK!");
        } else {
            System.out.println("Error:" + result.getErrors());
        }

        InternalNureonLangService service = new InternalNureonLangServiceImpl();
        String code = "BEGIN LAYER INPUT size=4 END";
        NureonLangParser.ProgramContext ctx = service.parse(code);

        System.out.println("Parsing success " + ctx.toStringTree());
    }
}