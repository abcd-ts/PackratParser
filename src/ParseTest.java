public class ParseTest {
    public static void parseTimeTest(String parserName, Parser parser, String input) throws Exception{
        parser.setInput(input);
        long start, end;
        start = System.nanoTime();
        parser.parse();
        end = System.nanoTime();
        System.out.println(parserName + ": " + (end - start) + " ms");
    }
    public static void main(String[] args) {

        /**
         * 子原さんの論文でメモ化によってコストが削減される例としてあげられていた規則，入力例
         * Expr ← Term ‘+’ Expr / Term
         * Term ← Prim ‘*’ Term / Prim
         * Prim ← ‘(’ Expr ‘)’ / Func / Name / Num
         * Func ← Name ‘(’ List ‘)’ 
         * Name ← [a−z] Name / [a−z]
         * Num ← [0−9] Num / [0−9] 
         * List ← Expr ‘,’ List / Expr / ε
        */
        Rules testRule = new Rules()
            // <Expr>が開始規則
            .addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term")))
            .addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.nt("Prim"), Peg.t("*"), Peg.nt("Term")), Peg.nt("Prim")))
            .addRule(Peg.nt("Prim"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Func"), Peg.nt("Name"), Peg.nt("Num")))
            .addRule(Peg.nt("Func"), Peg.seq(Peg.nt("Name"), Peg.t("("), Peg.nt("List"), Peg.t(")")))
            .addRule(Peg.nt("Name"), Peg.ch(Peg.seq(Peg.t("a"), Peg.nt("Name")), Peg.t("a")))
            .addRule(Peg.nt("Num"), Peg.ch(Peg.seq(Peg.t("1"), Peg.nt("Num")), Peg.t("1")))
            .addRule(Peg.nt("List"), Peg.ch(Peg.seq(Peg.nt("Expr"), Peg.t(","), Peg.nt("List")), Peg.nt("Expr"), Peg.emp()));

        String nestedFunc = "a(11*a(1)+1)*(1+a)";
        String largeName = "";
        for (int i = 0; i < 512; i++) largeName += "a";

        Parser peg = new PegParser(testRule);
        Parser packrat = new PackratParser(testRule);

        try {
            // a(11*a(1)+1)*(1+a)
            System.out.println("input: " + nestedFunc);
            parseTimeTest("PegParser", peg, nestedFunc);
            parseTimeTest("PackratParser", packrat, nestedFunc);

            // x^(512)
            System.out.println("input: x^(512)");
            parseTimeTest("PegParser", peg, largeName);
            parseTimeTest("PackratParser", packrat, largeName);

        }
        catch(Exception e) {}
        
            
    }
    
}
