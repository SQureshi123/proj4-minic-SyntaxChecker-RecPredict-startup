import java.util.List;
import java.util.ArrayList;

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
            case FUNC       : return "\"func\"";
            case VAR        : return "\"var\"";
            case BEGIN      : return "\"{\"";
            case END        : return "\"}\"";
            case RETURN     : return "\"return\"";
            case PRINT      : return "\"print\"";
            case IF         : return "\"if\"";
            case THEN       : return  "\"then\"";
            case ELSE       : return "\"else\"";
            case WHILE      : return "\"while\"";
            case LPAREN     : return "\"(\"";
            case RPAREN     : return "\")\"";
            case LBRACKET   : return "\"[\"";
            case RBRACKET   : return "\"]\"";
            case VOID       : return "\"void\"";
            case NUM        : return "\"num\"";
            case BOOL       : return "\"bool\"";
            case NEW        : return "\"new\"";
            case SIZE       : return "\"size\"";
            case ASSIGN     : return "\":=\"";
            case RELOP      : return "RELOP";
            case EXPROP     : return "EXPROP";
            case TERMOP     : return "TERMOP";
            case TYPEOF     : return "\"::\"";
            case SEMI       : return "\";\"";
            case COMMA      : return "\",\"";
            case IDENT      : return "an identifier";
            case DOT        : return "\".\"";
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

        if(!match)                          // if token does not match
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
        throw new Exception("No matching production in program at " + _lexer.lineno + ":" + _lexer.column + ".");
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
        throw new Exception("No matching production in decl_list at " + _lexer.lineno + ":" + _lexer.column + ".");
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
        throw new Exception("No matching production in decl_list_ at " + _lexer.lineno + ":" + _lexer.column + ".");
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
        throw new Exception("No matching production in fun_decl at " + _lexer.lineno + ":" + _lexer.column + ".");
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
        throw new Exception("No matching production in prim_type at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public List<ParseTree.Param> params() throws Exception //MAYBE NOT RIGHT
    {
        //       params -> eps
        switch(_token.type)
        {
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
            case IDENT:
                List<ParseTree.Param> v01 = param_list();
                return v01;

        }
        throw new Exception("No matching production in params at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.LocalDecl local_decl() throws Exception { //MAYBE NEEDS SWITCH
        //    local_decl -> VAR IDENT TYPEOF type_spec SEMI

        switch(_token.type) {
            case VAR:
                String v01 = Match(VAR);
                String v02 = Match(IDENT);
                String v03 = Match(TYPEOF);
                ParseTree.TypeSpec v04 = type_spec();
                String v05 = Match(SEMI);
                return new ParseTree.LocalDecl(v02, v04);
        }
        throw new Exception("No matching production in local_decl at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.LocalDecl> local_decls() throws Exception
    {
        //  local_decls -> local_decls'

        switch(_token.type) {
            case VAR:
            case BEGIN:
            case END:
            case IDENT:
            case IF:
            case PRINT:
            case RETURN:
            case WHILE:
                return local_decls_();
        }
        throw new Exception("No matching production in local_decls at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.LocalDecl> local_decls_() throws Exception
    {
        // local_decls' -> eps
        switch(_token.type)
        {
            case VAR:
                ParseTree.LocalDecl v01 = local_decl();
                List<ParseTree.LocalDecl> v02 = local_decls_();
                v02.add(0, v01);
                return v02;
            case BEGIN:
            case END:
            case RETURN:
            case PRINT:
            case IF:
            case WHILE:
            case IDENT:
            case ENDMARKER:
                return new ArrayList<ParseTree.LocalDecl>();
        }
        throw new Exception("error");
    }
    public List<ParseTree.Stmt> stmt_list() throws Exception
    {
        //    stmt_list -> stmt_list'

        switch(_token.type) {
            case IDENT:
            case PRINT:
            case IF:
            case WHILE:
            case BEGIN:
            case END:
            case RETURN:
                return stmt_list_();
        }
        throw new Exception("No matching production in stmt_list at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public List<ParseTree.Stmt> stmt_list_() throws Exception
    {
        //   stmt_list' -> eps

        switch(_token.type) {
            case BEGIN:
            case RETURN:
            case PRINT:
            case IF:
            case WHILE:
                ParseTree.Stmt v01 = stmt();
                List<ParseTree.Stmt> v02 = stmt_list_();
                v02.add(0, v01);
                return v02;
            case END:
            case ELSE:
                return new ArrayList<ParseTree.Stmt>();
        }
        throw new Exception("No matching production in stmt_list' at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public ParseTree.Term term() throws Exception {
        // term -> factor term_

        switch (_token.type) {
            case LPAREN:
            case NEW:
            case IDENT:
            case BOOL_LIT:
            case NUM_LIT:
                ParseTree.Factor v01 = factor();
                ParseTree.Term_ v02 = term_();
                return new ParseTree.Term(v01, v02);
        }
        throw new Exception("No matching production in term at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public ParseTree.Term_ term_() throws Exception {
        // term' -> TERMOP factor term' | ϵ

        switch(_token.type) {
            case TERMOP:
                String v01 = Match(TERMOP);
                ParseTree.Factor v02 = factor();
                ParseTree.Term_ v03 = term_();
                return new ParseTree.Term_(v01,v02,v03);
            case EXPROP:
            case RELOP:
            case SEMI:
            case COMMA:
            case RPAREN:
            case RBRACKET:
            case END:
                return null;
        }
        throw new Exception("No matching production in term_ at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.Arg> arg_list() throws Exception {

        switch(_token.type) {
            case LPAREN:
            case NUM:
            case IDENT:
            case BOOL_LIT:
            case NUM_LIT:
                ParseTree.Expr v01 = expr();
                List<ParseTree.Arg> v02 = new ArrayList<>();
                v02.add(new ParseTree.Arg(v01));
                v02.addAll(arg_list_());
                return v02;
        }
        throw new Exception("No matching production in arg_list at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.Arg> arg_list_() throws Exception {
        //arg_list' -> COMMA expr arg_list | eps
        switch (_token.type) {
            case COMMA:
                String v01 = Match(COMMA);
                ParseTree.Expr v02 = expr();
                List<ParseTree.Arg> v03 = new ArrayList<>();
                v03.add(new ParseTree.Arg(v02));
                v03.addAll(arg_list_());
                return v03;
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
        }
        throw new Exception("No matching production in arg_list_ at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public List<ParseTree.Arg> args() throws Exception {

        switch(_token.type) {
            case RPAREN:
                return new ArrayList<ParseTree.Arg>();
            case LPAREN:
            case IDENT:
            case BOOL_LIT:
            case NUM_LIT:
            case NEW:
                List<ParseTree.Arg> v01 = arg_list();
                return v01;
        }
        throw new Exception("No matching production in args at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public ParseTree.StmtAssign assign_stmt() throws Exception {
        //  assign_stmt -> IDENT ASSIGN expr SEMI

        switch(_token.type) {
            case IDENT:
                String v01 = Match(IDENT);
                String v02 = Match(ASSIGN);
                ParseTree.Expr v03 = expr();
                String v04 = Match(SEMI);
                return new ParseTree.StmtAssign(v01, v03);
        }
        throw new Exception("No matching production in assign_stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.StmtCompound compound_stmt() throws Exception {

        switch(_token.type) {
            case BEGIN:
                String v01 = Match(BEGIN);
                List<ParseTree.LocalDecl> v02 = local_decls();
                List<ParseTree.Stmt> v03 = stmt_list();
                String v04 = Match(END);
                return new ParseTree.StmtCompound(v02,v03);
        }
        throw new Exception("No matching production in compound_stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.Expr expr() throws Exception {

        switch(_token.type) {
            case LPAREN:
            case NEW:
            case IDENT:
            case BOOL_LIT:
            case NUM_LIT:
                ParseTree.Term v01 = term();
                ParseTree.Expr_ v02 = expr_();
                return new ParseTree.Expr(v01, v02);
        }
        throw new Exception("No matching production in expr at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.Expr_ expr_() throws Exception {
        // expr' -> EXPROP term expr'| RELOP term expr'| ϵ

        switch(_token.type) { //MIGHT NEED CHANGED
            case EXPROP:
                String v01 = Match(EXPROP);
                ParseTree.Term v02 = term();
                ParseTree.Expr_ v03 = expr_();
                return new ParseTree.Expr_(v01,v02,v03);
            case RELOP:
                String v04 = Match(RELOP);
                ParseTree.Term v05 = term();
                ParseTree.Expr_ v06 = expr_();
                return new ParseTree.Expr_(v04,v05,v06);
            case COMMA:
            case SEMI:
            case RBRACKET:
            case RPAREN:
                return null;
        }
        throw new Exception("No matching production in expr_ at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.Factor factor() throws Exception {                 //NEEDS TO INCLUDE LEFT BRACKET
        // factor -> IDENT factor'| LPAREN expr RPAREN| NUM_LIT

        switch(_token.type) {
            case IDENT:
                String v01 = Match(IDENT);
                ParseTree.Factor_ v02 = factor_(); //NOT WORKING
                return new ParseTree.FactorIdentExt(v01, v02);
            case LPAREN:
                String v03 = Match(LPAREN);
                ParseTree.Expr v04 = expr();
                String v05 = Match(RPAREN);
                return new ParseTree.FactorParen(v04);
            case NUM_LIT:
                String v06 = Match(NUM_LIT);
                double numVal = Double.parseDouble(v06);
                return new ParseTree.FactorNumLit(numVal);
            case BOOL_LIT:
                String v07 = Match(BOOL_LIT);
                boolean boolVal = Boolean.parseBoolean(v07);
                return new ParseTree.FactorBoolLit(boolVal);
            case NEW:
                String v08 = Match(NEW);
                ParseTree.PrimType v09 = prim_type();
                String v10 = Match(LBRACKET);
                ParseTree.Expr v11 = expr();
                String v12 = Match(RBRACKET);
                return new ParseTree.FactorNew(v09,v11);
        }
        throw new Exception("No matching production in factor at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.FactorIdent_DotSize factor_() throws Exception { //need fixing, NOTE: should include LPAREN, RPAREN, LBRACKET,
                                                          //RBRACKET, RELOP, EXPROP, TERMOP, SEMI, COMMA, DOT,
        // factor' -> LPAREN args RPAREN| LBRACKET expr RBRACKET
        //               | DOT SIZE| ϵ

        switch(_token.type) {
            case LPAREN:
                String v01 = Match(LPAREN);
                List<ParseTree.Arg> v02 = args();
                String v03 = Match(RPAREN);
                return new ParseTree.FactorIdent_ParenArgs(v02);
            case LBRACKET:
                String v04 = Match(LBRACKET);
                ParseTree.Expr v05 = expr();
                String v06 = Match(RBRACKET);
                return new ParseTree.FactorIdent_BrackExpr(v05);
            case DOT:
                String v07 = Match(DOT);
                String v08 = Match(SIZE);
                return new ParseTree.FactorIdent_DotSize();
            case COMMA:
            case EXPROP:
            case RBRACKET:
            case RELOP:
            case RPAREN:
            case SEMI:
            case TERMOP:
                return new ParseTree.FactorIdent_Eps();
        }
        throw new Exception("No matching production in factor_ at " + _lexer.lineno + ":" + _lexer.column + ".");

    }
    public ParseTree.StmtIf if_stmt() throws Exception {
        // if_stmt -> IF LPAREN expr RPAREN THEN stmt_list ELSE stmt_list END

        switch(_token.type) {
            case IF:
                String v01 = Match(IF);
                String v02 = Match(LPAREN);
                ParseTree.Expr v03 = expr();
                String v04 = Match(RPAREN);
                String v05 = Match(THEN);
                List<ParseTree.Stmt> v06 = stmt_list();
                String v07 = Match(ELSE);
                List<ParseTree.Stmt> v08 = stmt_list();
                String v09 = Match(END);
                return new ParseTree.StmtIf(v03,v06,v08);
        }
        throw new Exception("No matching production in if_stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.Stmt stmt() throws Exception {

        switch(_token.type) {
            case IDENT:
                return assign_stmt();
            case PRINT:
                return print_stmt();
            case RETURN:
                return return_stmt();
            case IF:
                return if_stmt();
            case WHILE:
                return while_stmt();
            case BEGIN:
                return compound_stmt();
        }
        throw new Exception("No matching production in stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.StmtPrint print_stmt() throws Exception {

        switch(_token.type) {
            case PRINT:
                String v01 = Match(PRINT);
                ParseTree.Expr v02 = expr();
                String v03 = Match(SEMI);
                return new ParseTree.StmtPrint(v02);
        }
        throw new Exception("No matching production in print_stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public ParseTree.StmtReturn return_stmt() throws Exception {

        switch(_token.type) {
            case RETURN:
                String v01 = Match(RETURN);
                ParseTree.Expr v02 = expr();
                String v03 = Match(SEMI);
                return new ParseTree.StmtReturn(v02);
        }
        throw new Exception("No matching production in return_stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
    public ParseTree.Param param() throws Exception {

        switch(_token.type) {
            case IDENT:
            case RPAREN: //INCONSISTENT WITH PARSING TABLE
                String v01 = Match(IDENT);
                String v02 = Match(TYPEOF);
                ParseTree.TypeSpec v03 = type_spec();
                return new ParseTree.TypeSpec(v01, v03);
        }
        throw new Exception("No matching production in param at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.Param> param_list() throws Exception {

        switch(_token.type) {
            case IDENT:
            case RPAREN:
                ParseTree.Param v01 = param();
                List<ParseTree.Param> v02 = param_list_();
                v02.add(0, v01);
                return v02;
        }
        throw new Exception("No matching production in param_list at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.Param> param_list_() throws Exception {
        switch (_token.type) {
            case COMMA:
                String v01 = Match(COMMA);
                ParseTree.Param v02 = param();
                List<ParseTree.Param> v03 = param_list_();
                v03.add(0, v02);
                return v03;
            case RPAREN:
                return new ArrayList<ParseTree.Param>();
        }
        throw new Exception("No matching production in param_list_ at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public ParseTree.TypeSpec type_spec() throws Exception {
        // type_spec -> prim_type type_spec'
        switch(_token.type) {
            case NUM:
            case BOOL:
            case VOID:
            case RPAREN:
                ParseTree.PrimType v01 = prim_type();
                ParseTree.TypeSpec_ v02 = type_spec_();
                return new ParseTree.TypeSpec(v01,v02);
        }
        throw new Exception("No matching production in type_spec at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public List<ParseTree.TypeSpec_> type_spec_() throws Exception {
        switch (_token.type) {
            case RPAREN:
            case SEMI:
            case COMMA:
                return new ParseTree.TypeSpec_Value();
            case LBRACKET:
                String v01 = Match(LBRACKET);
                String v02 = Match(RBRACKET);
                return new ParseTree.TypeSpec_Array();
        }
        throw new Exception("No matching production in type_spec_ at " + _lexer.lineno + ":" + _lexer.column + ".");
    }

    public ParseTree.StmtWhile while_stmt() throws Exception {
        switch (_token.type) {
            case WHILE:
                String v01 = Match(WHILE);
                String v02 = Match(LPAREN);
                ParseTree.Expr v03 = expr();
                String v04 = Match(RPAREN);
                String v05 = Match(BEGIN);
                List<ParseTree.Stmt> v06 = stmt_list();
                String v07 = Match(END);
                return new ParseTree.StmtWhile(v03, v06);
        }
        throw new Exception("No matching production in while_stmt at " + _lexer.lineno + ":" + _lexer.column + ".");
    }
}