/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) 2000 Gerwin Klein <lsf@jflex.de>                          *
 * All rights reserved.                                                    *
 *                                                                         *
 * Thanks to Larry Bell and Bob Jamison for suggestions and comments.      *
 *                                                                         *
 * License: BSD                                                            *
 *                                                                         *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

%%

%class Lexer
%byaccj

%{

  public Parser   parser;
  public int      lineno;
  public int      column;

  public Lexer(java.io.Reader r, Parser parser) {
    this(r);
    this.parser = parser;
    this.lineno = 1;
    this.column = 1;
  }
   public void update_col() {
      String s = yytext();
      column += s.length();
    }

    public void update_line_and_col() {
      String s = yytext();
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == '\n') {
          column = 1;
          lineno += 1;
        }
        column++;
      }
    }
%}

num          = [0-9]+("."[0-9]+)?
identifier   = [a-zA-Z][a-zA-Z0-9_]*
newline      = \n
whitespace   = [ \t\r]+
linecomment  = "//".*
blockcomment = "/*"[^]*"*/"

%%

"func"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.FUNC   ; }
"var"                               { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.VAR    ; }
"begin"                             { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BEGIN  ; }
"end"                               { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.END    ; }
"return"                            { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RETURN ; }
"print"                             { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.PRINT  ; }
"if"                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.IF     ; }
"then"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.THEN   ; }
"else"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.ELSE   ; }
"while"                             { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.WHILE  ; }
"("                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.LPAREN ; }
")"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RPAREN ; }
"["                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.LBRACKET; }
"]"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RBRACKET; }
"void"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.VOID   ; }
"num"                               { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.NUM    ; }
"bool"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BOOL   ; }
"new"                               { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.NEW    ; }
"size"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.SIZE   ; }
":="                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.ASSIGN ; }
"<"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
">"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
"<="                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
">="                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
"="                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
"<>"                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.RELOP  ; }
"+"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.EXPROP ; }
"-"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.EXPROP ; }
"or"                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.EXPROP ; }
"*"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.TERMOP ; }
"/"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.TERMOP ; }
"and"                               { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.TERMOP ; }
"::"                                { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.TYPEOF ; }
";"                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.SEMI   ; }
","                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.COMMA  ; }
"."                                 { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.DOT    ; }
"true"                              { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BOOL_LIT;}
"false"                             { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.BOOL_LIT;}
{identifier}                        { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.IDENT  ; }
{num}                               { parser.yylval = new ParserVal((Object)yytext()); column += yytext().length(); return Parser.NUM_LIT; }
{newline}                           { lineno++; column = 1;}
{whitespace}                        { column += yytext().length(); }
{linecomment}                       {  /* skip */  }
{blockcomment}                      { /* skip */}


\b     { System.err.println("Sorry, backspace doesn't work"); }

/* error fallback */
[^]    { System.err.println("Error: unexpected character '"+yytext()+"'"); return -1; }
