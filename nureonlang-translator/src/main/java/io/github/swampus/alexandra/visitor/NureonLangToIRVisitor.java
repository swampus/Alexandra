package io.github.swampus.alexandra.visitor;

import io.github.swampus.alexandra.ir.model.Instruction;
import io.github.swampus.alexandra.ir.model.OpCode;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangBaseVisitor;
import io.github.swampus.alexandra.nureonlang.antlr4.NureonLangParser;
import io.github.swampus.alexandra.translator.exception.ParseError;
import lombok.Getter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Visitor skeleton: implement logic for each language construct.
// Fill in fields as needed for your IR POJO (Instruction).

public class NureonLangToIRVisitor extends NureonLangBaseVisitor<Instruction> {

    @Getter
    private final List<ParseError> errors = new ArrayList<>();

    // PROGRAM entrypoint: aggregate all instructions (statements + optional fileRef)
    @Override
    public Instruction visitProgram(NureonLangParser.ProgramContext ctx) {
        List<Instruction> instructions = new ArrayList<>();
        ctx.statement().forEach(stmt -> {
            Instruction instr = visit(stmt);
            if (instr != null) {
                instructions.add(instr);
            } else {
                addError("Unknown or invalid statement: " + stmt.getText(), stmt);
            }
        });
        if (ctx.fileRef() != null) {
            Instruction fileInstr = visitFileRef(ctx.fileRef());
            if (fileInstr != null) {
                instructions.add(fileInstr);
            } else {
                addError("Invalid file reference: " + ctx.fileRef().getText(), ctx.fileRef());
            }
        }
        return Instruction.builder()
                .op(OpCode.PROGRAM)
                .body(instructions)
                .build();
    }

    @Override
    public Instruction visitBlock(NureonLangParser.BlockContext ctx) {
        List<Instruction> body = new ArrayList<>();
        for (var stmt : ctx.statement()) {
            Instruction instr = visit(stmt);
            if (instr != null) {
                body.add(instr);
            } else {
                addError("Unknown or invalid statement inside block: " + stmt.getText(), stmt);
            }
        }
        return Instruction.builder()
                .op(OpCode.BLOCK)
                .body(body)
                .build();
    }

    // LAYER declarations (anonymous)
    @Override
    public Instruction visitAnonLayer(NureonLangParser.AnonLayerContext ctx) {
        String type = ctx.ID().getText();
        Map<String, Object> params = parseParams(ctx.param());
        String name = (String) params.get("name");
        Integer size = extractParam(params, "size");

        if (type == null || type.isBlank()) {
            addError("Layer type is missing in anonymous layer", ctx);
        }
        if (size == null) {
            addError("Layer 'size' parameter is missing in anonymous layer", ctx);
        }

        String activation = (String) params.get("activation");
        Object shape = params.get("shape");
        Integer dim = extractParam(params, "dim");
        Integer heads = extractParam(params, "heads");
        Integer depth = extractParam(params, "depth");

        return Instruction.builder()
                .op(OpCode.LAYER)
                .type(type)
                .name(name)
                .size(size)
                .activation(activation)
                .shape(shape)
                .dim(dim)
                .heads(heads)
                .depth(depth)
                .params(params)
                .build();
    }


    @Override
    public Instruction visitNamedLayer(NureonLangParser.NamedLayerContext ctx) {
        String type = ctx.ID(0).getText();
        String name = ctx.ID(1).getText();
        Map<String, Object> params = parseParams(ctx.param());

        Integer size = extractParam(params, "size");
        String activation = (String) params.get("activation");
        Object shape = params.get("shape");
        Integer dim = extractParam(params, "dim");
        Integer heads = extractParam(params, "heads");
        Integer depth = extractParam(params, "depth");

        if (type == null || type.isBlank()) {
            addError("Layer type is missing in named layer", ctx);
        }
        if (name == null || name.isBlank()) {
            addError("Layer name is missing in named layer", ctx);
        }
        if (size == null) {
            addError("Layer 'size' parameter is missing in named layer", ctx);
        }

        return Instruction.builder()
                .op(OpCode.LAYER)
                .type(type)
                .name(name)
                .size(size)
                .activation(activation)
                .shape(shape)
                .dim(dim)
                .heads(heads)
                .depth(depth)
                .params(params)
                .build();
    }

    private Integer extractParam(Map<String, Object> params, String key) {
        Object value = params.get(key);
        if (value == null) {
            return null;
        }
        String str = value.toString();
        if (str.matches("\\d+")) {
            return Integer.valueOf(str);
        }
        return null;
    }

    // CONNECT statements
    @Override
    public Instruction visitConnectStmt(NureonLangParser.ConnectStmtContext ctx) {
        String from = null;
        String to = null;

        if (ctx.dottedId().size() > 0) {
            from = ctx.dottedId(0).getText();
        }

        if (ctx.dottedId().size() > 1) {
            to = ctx.dottedId(1).getText();
        }

        if (from == null || from.isBlank()) {
            addError("Missing 'from' identifier in CONNECT statement.", ctx);
        }

        if (to == null || to.isBlank()) {
            addError("Missing 'to' identifier in CONNECT statement.", ctx);
        }

        return Instruction.builder()
                .op(OpCode.CONNECT)
                .from(from)
                .to(to)
                .build();
    }

    // Control statements: FOR / IF
    @Override
    public Instruction visitForLoop(NureonLangParser.ForLoopContext ctx) {
        String var = ctx.ID() != null ? ctx.ID().getText() : null;
        String fromVal = ctx.expr().size() > 0 ? ctx.expr(0).getText() : null;
        String toVal = ctx.expr().size() > 1 ? ctx.expr(1).getText() : null;
        Instruction body = ctx.block() != null ? visit(ctx.block()) : null;

        if (var == null || var.isBlank()) {
            addError("Missing loop variable in FOR statement.", ctx);
        }
        if (fromVal == null) {
            addError("Missing 'from' value in FOR statement.", ctx);
        }
        if (toVal == null) {
            addError("Missing 'to' value in FOR statement.", ctx);
        }
        if (body == null) {
            addError("Missing block/body in FOR statement.", ctx);
        }

        return Instruction.builder()
                .op(OpCode.FOR)
                .var(var)
                .fromVal(fromVal)
                .toVal(toVal)
                .body(body == null ? List.of() : List.of(body))
                .build();
    }

    @Override
    public Instruction visitIfStmt(NureonLangParser.IfStmtContext ctx) {
        Instruction condInstr = null;
        if (ctx.condition() != null) {
            condInstr = visit(ctx.condition());
        } else {
            condInstr = Instruction.builder()
                    .op(OpCode.CONDITION)
                    .expr("<missing>")
                    .build();
        }
        Instruction thenBlock = visit(ctx.block(0));
        Instruction elseBlock = ctx.ELSE() != null ? visit(ctx.block(1)) : null;

        List<Instruction> body = new ArrayList<>();
        body.add(thenBlock);

        if (elseBlock != null) {
            body.add(elseBlock);
        }

        return Instruction.builder()
                .op(OpCode.IF)
                .cond(condInstr)
                .body(body)
                .build();
    }


    @Override
    public Instruction visitCondition(NureonLangParser.ConditionContext ctx) {
        String condStr = ctx.getText();
        return Instruction.builder()
                .op(OpCode.CONDITION)
                .expr(condStr)
                .build();
    }

    // MODULE declarations and calls
    @Override
    public Instruction visitModuleDecl(NureonLangParser.ModuleDeclContext ctx) {
        String name = ctx.ID().getText();
        Instruction body = visit(ctx.block());
        return Instruction.builder()
                .op(OpCode.MODULE_DEF)
                .name(name)
                .body(List.of(body))
                .build();
    }

    @Override
    public Instruction visitModuleCall(NureonLangParser.ModuleCallContext ctx) {
        String name = ctx.ID().getText();
        return Instruction.builder()
                .op(OpCode.MODULE_CALL)
                .name(name)
                .build();
    }

    // MACRO declarations and calls
    @Override
    public Instruction visitMacroDecl(NureonLangParser.MacroDeclContext ctx) {
        String name = ctx.ID().getText();
        List<String> paramList = parseParamList(ctx.paramList());
        Instruction body = visit(ctx.block());

        Map<String, Object> meta = new HashMap<>();
        meta.put("params", paramList);

        return Instruction.builder()
                .op(OpCode.MACRO_DEF)
                .inputs(paramList)
                .name(name)
                .meta(meta)
                .body(List.of(body))
                .build();
    }

    @Override
    public Instruction visitMacroCall(NureonLangParser.MacroCallContext ctx) {
        String name = ctx.ID().getText();
        List<String> args = parseArgList(ctx.argList());

        Map<String, Object> meta = new HashMap<>();
        meta.put("args", args);

        return Instruction.builder()
                .op(OpCode.MACRO_CALL)
                .name(name)
                .inputs(args)
                .meta(meta)
                .build();
    }

    // LET statements
    @Override
    public Instruction visitLetStmt(NureonLangParser.LetStmtContext ctx) {
        String var = ctx.ID().getText();
        int last = ctx.expr().size() - 1;
        String expr = ctx.expr(last).getText();
        return Instruction.builder()
                .op(OpCode.LET)
                .var(var)
                .expr(expr)
                .build();
    }

    @Override
    public Instruction visitExpandStmt(NureonLangParser.ExpandStmtContext ctx) {
        String rawTarget = ctx.expandTarget().getText();
        String target = rawTarget.contains("=")
                ? rawTarget.substring(rawTarget.indexOf("=") + 1).trim()
                : rawTarget.trim();

        Instruction body = visit(ctx.block());

        Map<String, Object> meta = new HashMap<>();
        meta.put("GROUP", target);

        System.out.println("[PARSER] EXPAND: target = '" + target + "', meta = " + meta);

        return Instruction.builder()
                .op(OpCode.EXPAND)
                .target(target)
                .body(List.of(body))
                .meta(meta)
                .build();
    }

    // FILE reference
    @Override
    public Instruction visitFileRef(NureonLangParser.FileRefContext ctx) {
        String quotesFilePath = "^\"|\"$";
        String path = ctx.STRING().getText().replaceAll(quotesFilePath, "");
        return Instruction.builder()
                .op(OpCode.FILE)
                .path(path)
                .build();
    }

    // Helper for param list (for MACRO)
    private List<String> parseParamList(NureonLangParser.ParamListContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        return ctx.ID().stream()
                .map(r -> r.getText().trim())
                .filter(s -> !s.isEmpty())
                .toList();
    }


    // Helper for arg list (for MACRO call)
    private List<String> parseArgList(NureonLangParser.ArgListContext ctx) {
        if (ctx == null) {
            return List.of();
        }
        return ctx.expr().stream()
                .map(RuleContext::getText)
                .toList();
    }


    // Helper for params (for LAYER, etc)
    private Map<String, Object> parseParams(List<NureonLangParser.ParamContext> paramCtxs) {
        if (paramCtxs == null) {
            return Map.of();
        }
        return paramCtxs.stream()
                .collect(Collectors.toMap(
                        p -> p.ID().getText(),
                        p -> parseValue(p.value())
                ));
    }

    // Helper for value (expr/shape)
    private Object parseValue(NureonLangParser.ValueContext ctx) {
        if (ctx == null) {
            return null;
        }
        if (ctx.expr() != null) {
            return ctx.expr().getText();
        }
        if (ctx.shape() != null) {
            return ctx.shape().getText();
        }
        return null;
    }

    private void addError(String message, RuleContext ctx) {
        if (ctx instanceof ParserRuleContext prc && prc.start != null) {
            errors.add(new ParseError(message, prc.start.getLine(), prc.start.getCharPositionInLine()));
        } else {
            errors.add(new ParseError(message, -1, -1));
        }
    }
}
