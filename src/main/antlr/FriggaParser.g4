parser grammar FriggaParser;

options { tokenVocab=FriggaLexer; }

friggaFile:
    namespace?
    usingList
    body
    EOF;

//Headers
use: USE STRING;
namespace: NAMESPACE STRING;

usingList:
    (use)*;

//Body
body: line+? | NEWLINE*;

line: expression NEWLINE*?;

expression:
           assignment #assignmentExpression
         |  literal #literalExpression
         | left=expression operator=(PLUS | MINUS | TIMES | DIVIDE | POWER) right=expression #binaryOperator
         | left=expression operator=(EQUAL | MORE_THAN | MORE_EQUAL_THAN | LESS_EQUAL_THAN | LESS_THAN ) right=expression #binaryLogicalOperator
         | function #functionExpression
         | lambda #lambdaExpression
         | expression call #callExpression //something()
         | expression referencedCall #referencedCallExpression
         | expression DOT ID #accessExpression //something.property
         | paranthesizedExpression #paranthesisExpression
         | ID #propertyReference
        ;

         paranthesizedExpression: LPAREN expression RPAREN;

         propertyModifier:
              MUTABLE
            | STATEFUL
            | SECRET
            | STATIC;

         assignment: propertyModifier* ID typeSpec? ASSIGN expression;
         block: LCPAREN body RCPAREN;

         call:  LPAREN args RPAREN;

         referencedCall: LSPAREN args RSPAREN;

         args: expression? (COMMA expression)*;
         typeSpec: DOUBLE_COLON type;

         type: functionType | ID | NOTHING | tuple;

         tuple: (LPAREN (tupleParam COMMA tupleParam)+ RPAREN);
         tupleParam : ID typeSpec;

/*
Generics
*/
generic: LESS_THAN typeParam (COMMA typeParam)* MORE_THAN;
typeParam: (ID typeSpec) | ID;
/*
Functions
*/
function: generic? (functionSignature block); //{} OR (a::Int) -> Int {}
functionSignature: functionParams ARROW type; //() -> _ OR (a::Int) -> Int
functionParams: (LPAREN functionParam? (COMMA functionParam)* RPAREN); //(a::Int) OR (a::Int, b::Int, etc)
functionParam: ID typeSpec; //a::Int

lambda: block | (lambdaParams ARROW (expression | block)); //{} OR (a::Int) -> Int OR (a::Int) -> Int {}
/*
(a, b) -> 3
(a::Int, b::Int) -> a + b
() -> {}
*/
lambdaParams: (LPAREN lamdaParam? (COMMA lamdaParam)* RPAREN); //(a::Int) OR (a, b, etc)
lamdaParam: functionParam | ID; //a::Int OR a

/*
Function Types
*/
functionType: (functionParamTypes) ARROW type;
functionParamTypes: (LPAREN type? (COMMA type)* RPAREN);

literal:
      MINUS? INT #intLiteral
    | MINUS? DEC #decLiteral
    | BOOL #boolLiteral
    | STRING #stringLiteral
    | CHAR #charLiteral
    | LPAREN (expression COMMA expression)+ RPAREN #tupleLiteral;

