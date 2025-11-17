grammar NureonLang;

@header {
    package io.github.swampus.alexandra.nureonlang.antlr4;
}

// === PARSER RULES ===

program : statement+ fileRef? EOF ;

block
    : BEGIN statement* END
    ;

statement
    : block
    | layerDecl
    | connectStmt
    | controlStmt
    | moduleDecl
    | moduleCall
    | letStmt
    | macroDecl
    | macroCall
    | expandStmt
    ;

// === LAYERS ===

layerDecl
    : LAYER ID param+            #anonLayer
    | LAYER ID ID param+         #namedLayer
    ;

// === PARAMETERS ===

param
    : ID EQ value
    ;

value
    : expr
    | shape
    ;

shape
    : INT ID INT                         #simpleShape   // shape=256 x 256
    | LPAREN INT (COMMA INT)* RPAREN     #tupleShape    // shape=(3, 224, 224)
    ;

// === EXPRESSIONS ===

expr
    : expr POW expr                      #powExpr
    | expr op=('*'|'/') expr             #mulDivExpr
    | expr op=('+'|'-') expr             #addSubExpr
    | ID LBRACK expr RBRACK              #arrayAccess
    | ID                                 #varRef
    | INT                                #intLiteral
    | FLOAT                              #floatLiteral
    | STRING                             #stringLiteral
    | LPAREN expr RPAREN                 #parenExpr
    ;

// === CONNECT ===

dottedId : ID ('.' ID)* (LBRACK expr RBRACK)* ;

connectStmt : CONNECT dottedId ARROW dottedId ;

// === CONTROL ===

controlStmt
    : forLoop
    | ifStmt
    ;

forLoop
    : FOR ID FROM expr TO expr block
    ;

ifStmt
    : IF condition block (ELSE block)?
    ;

condition
    : expr comparator value
    ;

comparator
    : EQ
    | NEQ
    | EQEQ    // ==
    | LT
    | LTE
    | GT
    | GTE
    ;

// === MODULES ===

moduleDecl
    : MODULE ID block
    ;

moduleCall
    : ID
    ;

// === LET ===

letStmt
    : LET ID (LBRACK expr RBRACK)? EQ expr SEMI
    ;

// === MACROS ===

macroDecl
    : DEFINE ID LPAREN paramList? RPAREN block
    ;

macroCall
    : ID LPAREN argList? RPAREN
    ;

paramList
    : ID (COMMA ID)*
    ;

argList
    : expr (COMMA expr)*
    ;

// === FILE REF ===

fileRef
    : FILE STRING
    ;

// === EXPAND/SYMMETRY BLOCK ===

expandStmt
    : EXPAND expandTarget block
    ;

expandTarget
    : SPACE EQ INT ID         #dimensionalExpand    // SPACE = 3D  (ID для D)
    | GROUP EQ ID             #namedGroupExpand
    | WITH GROUP ID           #withGroupExpand
    | ID                      #customExpand
    ;

// === LEXER RULES ===

BEGIN           : 'BEGIN';
END             : 'END';
LAYER           : 'LAYER';
CONNECT         : 'CONNECT';
MODULE          : 'MODULE';
DEFINE          : 'DEFINE';
LET             : 'LET';
FOR             : 'FOR';
FROM            : 'FROM';
TO              : 'TO';
IF              : 'IF';
ELSE            : 'ELSE';
FILE            : 'FILE';

EXPAND          : 'EXPAND';
SPACE           : 'SPACE';
GROUP           : 'GROUP';
WITH            : 'WITH';

EQEQ            : '==';
EQ              : '=';
NEQ             : '!=';
LT              : '<';
LTE             : '<=';
GT              : '>';
GTE             : '>=';

ARROW           : '->';
COMMA           : ',';
LPAREN          : '(';
RPAREN          : ')';
LBRACK          : '[';
RBRACK          : ']';
SEMI            : ';';

POW             : '^';

ID              : [a-zA-Z_][a-zA-Z_0-9]*;
INT             : [0-9]+;
FLOAT           : [0-9]+ '.' [0-9]*;
STRING          : '"' (~["\\] | '\\' .)* '"';

LINE_COMMENT    : '//' ~[\r\n]* -> skip;
BLOCK_COMMENT   : '/*' .*? '*/' -> skip;
WS              : [ \t\r\n]+ -> skip;
