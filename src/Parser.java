import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

public class Parser
{
    public static final int ENDMARKER   =  0;
    public static final int LEXERROR    =  1;

    public static final int NUM         = 11;
    public static final int BEGIN       = 12;
    public static final int END         = 13;
    public static final int LPAREN      = 14;
    public static final int RPAREN      = 15;
    public static final int ASSIGN      = 16;
    public static final int EXPROP      = 17;
    public static final int TERMOP      = 18;
    public static final int SEMI        = 19;
    public static final int TYPEOF      = 20;
    public static final int NUM_LIT     = 21;
    public static final int IDENT       = 22;
    public static final int FUNC        = 23;
    public static final int VAR         = 24;
    public static final int RETURN      = 25;
    public static final int PRINT       = 26;
    public static final int IF          = 27;
    public static final int THEN        = 28;
    public static final int ELSE        = 29;
    public static final int WHILE       = 30;
    public static final int LBRACKET    = 31;
    public static final int RBRACKET    = 32;
    public static final int VOID        = 33;
    public static final int BOOL        = 34;
    public static final int NEW         = 35;
    public static final int SIZE        = 36;
    public static final int RELOP       = 37;
    public static final int COMMA       = 38;
    public static final int DOT         = 39;
    public static final int BOOL_LIT    = 40;

    public static String token2string(int t) {
        switch (t) {
            case FUNC       :  return "\"func\"";
            case VAR        :  return "\"var\"";
            case BEGIN      :    return "\"{\"";
            case END        :      return "\"}\"";
            case RETURN     : return "\"return\"";
            case PRINT      : return "\"print\"";
            case IF         : return "\"if\"";
            case THEN       : return  "\"then\"";
            case ELSE       :     return "\"else\"";
            case WHILE      :    return "\"while\"";
            case LPAREN     :   return "\"(\"";
            case RPAREN     :   return "\")\"";
            case LBRACKET   :   return "\"[\"";
            case RBRACKET   :   return "\"]\"";
            case VOID       :  return "\"void\"";
            case NUM        :  return "\"num\"";
            case BOOL       : return "\"bool\"";
            case NEW        : return "\"new\"";
            case SIZE        : return "\"size\"";
            case ASSIGN     :   return "\":=\"";
            case RELOP      : return "RELOP";
            case EXPROP     : return "EXPROP";
            case TERMOP     : return "TERMOP";
            case TYPEOF     : return "\"::\"";
            case SEMI       :     return "\";\"";
            case COMMA      :    return "\",\"";
            case IDENT      :    return "an identifier";
            case DOT        :  return "\".\"";
            case NUM_LIT    : return "an integer";
            case BOOL_LIT   : return "an boolean";
            default         : return "default";
        }
    }
    public class Token
    {
        public int       type;
        public ParserVal attr;
        public Token(int type, ParserVal attr) {
            this.type   = type;
            this.attr   = attr;
        }
    }

    public ParserVal yylval;
    Token _token;
    Lexer _lexer;
    Compiler _compiler;
    public ParseTree.Program _parsetree;
    public String            _errormsg;
    public Parser(java.io.Reader r, Compiler compiler) throws Exception
    {
        _compiler  = compiler;
        _parsetree = null;
        _errormsg  = null;
        _lexer     = new Lexer(r, this);
        _token     = null;                  // _token is initially null
        Advance();                          // make _token to point the first token by calling Advance()
    }

    public void Advance() throws Exception
    {
        int token_type = _lexer.yylex();                                    // get next/first token from lexer
        if(token_type ==  0)      _token = new Token(ENDMARKER , null);     // if  0 => token is endmarker
        else if(token_type == -1) _token = new Token(LEXERROR  , yylval);   // if -1 => there is a lex error
        else                      _token = new Token(token_type, yylval);   // otherwise, set up _token
    }

    public String Match(int token_type) throws Exception
    {
        boolean match = (token_type == _token.type);
        String lexeme = "";
        if(_token.attr != null) lexeme = (String)_token.attr.obj;

        if(match == false)                          // if token does not match
            throw new Exception("token mismatch");  // throw exception (indicating parsing error in this assignment)

        if(_token.type != ENDMARKER)    // if token is not endmarker,
            Advance();                  // make token point next token in input by calling Advance()

        return lexeme;
    }
    public String getPos() {
        int col = _lexer.column - (yylval.obj.toString().length());
        int line = _lexer.lineno;
        return "" + line + ":" + col;
    }

    public int yyparse() throws Exception
    {
        try
        {
            _parsetree = program();
            return 0;
        }
        catch(Exception e)
        {
            _errormsg = e.getMessage();
            return -1;
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //      program -> decl_list
    //    decl_list -> decl_list'
    //   decl_list' -> fun_decl decl_list'  |  eps
    //     fun_decl -> FUNC IDENT TYPEOF prim_type LPAREN params RPAREN BEGIN local_decls stmt_list END
    //    prim_type -> NUM
    //       params -> eps
    //  local_decls -> local_decls'
    // local_decls' -> eps
    //    stmt_list -> stmt_list'
    //   stmt_list' -> eps
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public ParseTree.Program program() throws Exception
    {
        //      program -> decl_list
        switch(_token.type)
        {
            case FUNC:
            case ENDMARKER:
                List<ParseTree.FuncDecl> funcs = decl_list();
                String v1 = Match(ENDMARKER);
                return new ParseTree.Program(funcs);
        }
        throw new Exception("error");
    }
    public List<ParseTree.FuncDecl> decl_list() throws Exception
    {
        //    decl_list -> decl_list'
        switch(_token.type)
        {
            case FUNC:
            case ENDMARKER:
                return decl_list_();
        }
        throw new Exception("error");
    }
    public List<ParseTree.FuncDecl> decl_list_() throws Exception
    {
        //   decl_list' -> fun_decl decl_list'  |  eps
        switch(_token.type)
        {
            case FUNC:
                ParseTree.FuncDecl       v1 = fun_decl  ();
                List<ParseTree.FuncDecl> v2 = decl_list_();
                v2.add(0, v1);
                return v2;
            case ENDMARKER:
                return new ArrayList<ParseTree.FuncDecl>();
        }
        throw new Exception("error");
    }
    public ParseTree.FuncDecl fun_decl() throws Exception
    {
        //     fun_decl -> FUNC IDENT TYPEOF prim_type LPAREN params RPAREN BEGIN local_decls stmt_list END
        switch(_token.type)
        {
            case FUNC:
                String                    v01 = Match(FUNC  );
                String                    v02 = Match(IDENT );
                String                    v03 = Match(TYPEOF);
                ParseTree.PrimType        v04 = prim_type(  );
                String                    v05 = Match(LPAREN);
                List<ParseTree.Param>     v06 = params(     );
                String                    v07 = Match(RPAREN);
                String                    v08 = Match(BEGIN );
                List<ParseTree.LocalDecl> v09 = local_decls();
                List<ParseTree.Stmt>      v10 = stmt_list(  );
                String                    v11 = Match(END   );
                return new ParseTree.FuncDecl(v02, v04, v06, v09, v10);
        }
        throw new Exception("error");
    }
    public ParseTree.PrimType prim_type() throws Exception
    {
        //    prim_type -> NUM | VOID | BOOL
        switch(_token.type)
        {
            case NUM:
            {
                String v1 = Match(NUM);
                return new ParseTree.PrimTypeNum();
            }
            case BOOL:
            {
                String v1 = Match(BOOL);
                return new ParseTree.PrimTypeNum();
            }
            case VOID:
            {
                String v1 = Match(VOID);
                return new ParseTree.PrimTypeNum();
            }
        }
        throw new Exception("error");
    }
    public List<ParseTree.Param> params() throws Exception
    {
        //       params -> eps
        switch(_token.type)
        {
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("error");
    }
    public  ParseTree.LocalDecl local_decl() throws Exception {
        //    local_decl -> VAR IDENT TYPEOF type_spec SEMI
        Match(VAR);
        Match(IDENT);
        Match(TYPEOF);
        ParseTree.TypeSpec typeSpec = type_spec();
        String id = _token.attr.obj.toString();
        Match(SEMI);
        return new ParseTree.LocalDecl(id, typeSpec);
    }

    public List<ParseTree.LocalDecl> local_decls() throws Exception
    {
        //  local_decls -> local_decls'
        List<ParseTree.LocalDecl> localDecls = new LinkedList<>();
        while (true) {
            if (_token.type == VAR)
                localDecls.add(local_decl());
            else if (_token.type == BEGIN || _token.type == END || _token.type == IDENT ||
                    _token.type == IF || _token.type == PRINT || _token.type == RETURN || _token.type == WHILE)
                return localDecls;
            else {
                String s = String.format("Incorrect declaration of a local variable at %s.", getPos());
                throw new Exception(s);
            }
        }
    }

    public List<ParseTree.LocalDecl> local_decls_() throws Exception
    {
        // local_decls' -> eps
        switch(_token.type)
        {
            case END:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("error");
    }
    public List<ParseTree.Stmt> stmt_list() throws Exception
    {
        //    stmt_list -> stmt_list'
        LinkedList <ParseTree.Stmt> stmts = new LinkedList<>();
        while (true) {
            if (_token.type == IDENT) stmts.add(assign_stmt());
            else if (_token.type == PRINT) stmts.add(print_stmt());
            else if (_token.type == RETURN) stmts.add(return_stmt());
            else if (_token.type == IF) stmts.add(if_stmt());
            else if (_token.type == WHILE) stmts.add(while_stmt());
            else if (_token.type == BEGIN) stmts.add(compound_stmt());
            else if (_token.type == END) return stmts;
            else {
                String s = String.format("Incorrect statement at %s.", getPos());
                throw new Exception(s);
            }
        }
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception
    {
        //   stmt_list' -> eps
        switch(_token.type)
        {
            case END:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("error");
    }

    public List<ParseTree.Arg> arg_list_() throws Exception {
        //arg_list' -> COMMA expr arg_list | eps
        switch (_token.type) {
            case END:
                return arg_list_();
        }
        String s = String.format("Incorrect expression at %s.", getPos());
        throw new Exception(s);


    }
    public List<ParseTree.Arg> args() throws Exception {
        //arg_list' -> arg_list | eps
        switch (_token.type) {
            case END:
                return args();
        }
        String s = String.format("Incorrect expression at %s.", getPos());
        throw new Exception(s);


    }

    public ParseTree.StmtAssign assign_stmt() throws Exception {
        //  assign_stmt -> IDENT ASSIGN expr SEMI
        switch (_token.type) {
            case END:
                // return new ParseTree.StmtAssign();
        }
        String s = String.format("Incorrect expression at %s.", getPos());
        throw new Exception(s);


    }
    public ParseTree.StmtCompound compound_stmt() throws Exception {
        Match(BEGIN);
        List<ParseTree.LocalDecl> local_decls = local_decls();
        List<ParseTree.Stmt> stmts = stmt_list();
        Match(END);
        return new ParseTree.StmtCompound(local_decls, stmts);
    }
    public ParseTree.Expr expr() throws Exception {
        ParseTree.Term term = term();
        ParseTree.Expr_ expr_ = expr_();
        return new ParseTree.Expr(term, expr_);

    }
    public ParseTree.Expr_ expr_() throws Exception {
        // expr' -> EXPROP term expr'| RELOP term expr'| ϵ
        if (_token.type == EXPROP || _token.type == RELOP) {
            String op = _token.attr.obj.toString();
            Advance();
            ParseTree.Term term = term();
            ParseTree.Expr_ expr_ = expr_();
            return new ParseTree.Expr_(op, term, expr_);
        } else if (_token.type == COMMA || _token.type ==SEMI || _token.type == RBRACKET || _token.type == RPAREN || _token.type == SEMI) {
            return null;
        } else {
            String s = String.format("Incorrect expression at %s.", getPos());
            throw new Exception(s);
        }

    }
    public ParseTree.Factor factor() throws Exception {
        // factor -> IDENT factor'| LPAREN expr RPAREN| NUM_LIT
        //    | BOOL_LIT| NEW prim_type LBRACKET expr RBRACKET
        if (_token.type == LPAREN) {
            Match(LPAREN);
            ParseTree.Expr expr = expr();
            ParseTree.FactorParen paren =  new ParseTree.FactorParen(expr);
            Match(RPAREN);
            return paren;
        } else if (_token.type == IDENT) {
            String v = _token.attr.obj.toString();
            Match(IDENT);
            return new ParseTree.FactorIdentExt(v);    //FIX IT!
        } else if (_token.type == NUM_LIT) {
            String v = _token.attr.obj.toString();
            Match(NUM_LIT);
            return new ParseTree.FactorNumLit(Integer.parseInt(v));
        } else if (_token.type == BOOL_LIT) {
            String v = _token.attr.obj.toString();
            Match(BOOL_LIT);
            return new ParseTree.FactorBoolLit(Boolean.parseBoolean(v));
        }
        else if (_token.type == NEW) {
            Advance();
            ParseTree.PrimType type = prim_type();
            Match(LBRACKET);
            ParseTree.Expr expr = expr();
            Match(RBRACKET);
            return new ParseTree.FactorNew(type, expr);
        }
        else if (_token.type == LBRACKET) {
            Match(LBRACKET);
            ParseTree.Expr expr = expr();
            ParseTree.FactorParen paren =  new ParseTree.FactorParen(expr);
            Match(RBRACKET);
        }
        String s = String.format("Incorrect expression at %s.", getPos());
        throw new Exception(s);

    }
    public ParseTree.Factor  factor_() throws Exception {
        // factor' -> LPAREN args RPAREN| LBRACKET expr RBRACKET
        //               | DOT SIZE| ϵ
        switch (_token.type) {
            case END:
                //  return args();
        }
        String s = String.format("Incorrect expression at %s.", getPos());
        throw new Exception(s);


    }
    public ParseTree.StmtIf if_stmt() throws Exception {
        // if_stmt -> IF LPAREN expr RPAREN THEN stmt_list ELSE stmt_list END
        Match(IF);
        Match(LPAREN);
        ParseTree.Expr expr = expr();
        Match(RPAREN);
        ParseTree.Stmt stmt1 = stmt();
        Match(ELSE);
        ParseTree.Stmt stmt2 = stmt();
        return new ParseTree.StmtIf(expr, stmt1, stmt2);

    }
    public ParseTree.Stmt stmt() throws Exception {
        if (_token.type == IDENT)
            return assign_stmt();
        else if (_token.type == PRINT)
            return print_stmt();
        else if (_token.type == RETURN)
            return return_stmt();
        else if (_token.type == IF)
            return if_stmt();
        else if (_token.type == WHILE)
            return while_stmt();
        else if (_token.type == BEGIN)
            return compound_stmt();
        else
            throw new Exception("error");
    }
    public ParseTree.StmtPrint print_stmt() throws Exception {
        Match(PRINT);
        ParseTree.Expr expr = expr();
        Match(SEMI);
        return new ParseTree.StmtPrint(expr);
    }
    public ParseTree.StmtReturn return_stmt() throws Exception {
        Match(RETURN);
        ParseTree.Expr expr = expr();
        Match(SEMI);
        return new ParseTree.StmtReturn(expr);
    }
    public ParseTree.Param param() throws Exception {

        if (_token.type == IDENT) {
            String v = _token.attr.obj.toString();
            Match(IDENT);
            return new ParseTree.FactorIdentExt(v);
        }
    }

    public List<ParseTree.TypeSpec_> type_spec_() throws Exception {
        switch (_token.type) {
            case RPAREN:
                return new ArrayList<ParseTree.TypeSpec_>();
            case LBRACKET:
                Match(LBRACKET);
                Match(RBRACKET);
                return new ArrayList<ParseTree.TypeSpec_>();
        }
        throw new Exception("error");
    }

    public ParseTree.StmtWhile while_stmt() throws Exception {
        switch (_token.type) {
            case WHILE:
                Match(WHILE);
                Match(LPAREN);
                ParseTree.Expr condition = expr();
                Match(RPAREN);
                Match(BEGIN);
                List<ParseTree.Stmt> stmt_list = stmt_list();
                Match(END);
                return new ParseTree.StmtWhile(conditon, stmt_list);
        }
        throw new Exception("error");
    }


}
