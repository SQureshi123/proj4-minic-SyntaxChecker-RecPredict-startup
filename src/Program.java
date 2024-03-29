public class Program
{
    public static void main(String[] args) throws Exception
    {
        //java.io.Reader r = new java.io.StringReader
        //("func main::num()\n"
        //+"begin\n"
        //+"end\n"
        //);
        //
          args = new String[] { "proj4-minic-SyntaxChecker-testcases\\fail_06.minc" };

        if(args.length <= 0)
            return;
        java.io.Reader r = new java.io.FileReader(args[0]);

        Compiler compiler = new Compiler(r);
        compiler.Compile();
    }
}
