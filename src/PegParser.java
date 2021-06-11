import java.util.LinkedList;
import java.util.List;

public class PegParser implements Parser{
    private Rules rules;
    private String input;
    private int pos;
    private Peg.NonTerminal start;
    private boolean debug = false;

    public PegParser(Rules rules, String input) {
        this.rules = rules;
        this.start = rules.start;
        this.input = input;
        this.pos = 0;
    }

    public PegParser(Rules rules) {
        this.rules = rules;
        this.start = rules.start;
        this.input = "";
        this.pos = 0;
    }
    
    public ASTree parse() throws Exception{
        pos = 0;
        ASTree res = eval(start);
        if (res == null) throw new Exception("解析失敗");
        return res;
    }

    public void setInput(String input) {
        this.input = input;
    }

    private int depth = 0;
    public ASTree eval(Peg peg) throws Exception {
        if (debug) {
            depth++;
            for (int i = 1; i < depth; i++)
                System.out.print("  |");
        }
        
        ASTree res;
        if (peg instanceof Peg.Terminal) { res = terminal((Peg.Terminal)peg); }
        else if (peg instanceof Peg.NonTerminal) { res = nonTerminal((Peg.NonTerminal)peg); }
        else if (peg instanceof Peg.Sequence) { res = sequence((Peg.Sequence)peg); }
        else if (peg instanceof Peg.Choice) { res = choice((Peg.Choice)peg); }
        else if (peg instanceof Peg.Repeat) { res = repeat((Peg.Repeat)peg); }
        else if (peg instanceof Peg.NegPrediction) { res = negPrediction((Peg.NegPrediction)peg); }
        else if (peg instanceof Peg.Empty) { res = empty((Peg.Empty)peg); }
        else throw new Exception("eval");
        
        if (debug) depth--;
        
        return res;
    }  

    private ASTree terminal(Peg.Terminal t) {
        if (debug) System.out.print("evaluate Terminal " + t.toString() + " ");
        
        if (pos < input.length() && input.substring(pos, pos + 1).equals(t.str)) {
            pos += t.str.length();
            if (debug) System.out.println("MATCH!");
            return new ASTree("\"" + t.str + "\"");
        }
        else {  // 解析失敗
            if (debug) System.out.println("MISMATCH");
            return null;
        }
    }

    private ASTree nonTerminal(Peg.NonTerminal nt) throws Exception{
        if (debug) System.out.println("evaluate NonTerminal " + nt.toString());
        
        Peg rule = rules.getRule(nt);
        ASTree res = eval(rule);
        if (res == null) return null; // 解析失敗
        else {
            ASTree ast = new ASTree(nt.name);
            ast.addChild(res);
            return ast;
        }        
    }

    private ASTree sequence(Peg.Sequence seq) throws Exception {
        if (debug) System.out.println("evaluate Sequence " + seq.toString());
        
        int backtrackPos = pos;
        ASTree res = null, tmp = null;
        for (Peg expr: seq.exprs) {
            if (expr == null) break;
            tmp = eval(expr);
            if (tmp == null) {
                pos = backtrackPos;
                return null; // バックトラック
            }
            if (res == null)
                res = tmp;
            else
                res.addNext(tmp);

        }
        return res;
    }

    private ASTree choice(Peg.Choice ch) throws Exception {
        if (debug) System.out.println("evaluate Choice " + ch.toString());
        
        int backtrackPos = pos;
        ASTree res;
        for (Peg expr: ch.exprs) {
            if (expr == null) break;
            pos = backtrackPos;
            res = eval(expr);
            if (res == null) continue; // バックトラック
            else { return res; }
        }
        return null;
    }
    
    private ASTree repeat(Peg.Repeat rep) throws Exception {
        if (debug) System.out.println("evaluate repeat " + rep.toString());
        
        int backtrackPos = pos;
        ASTree res = null, tmp = null;
        while (true) {
            tmp = eval(rep.expr);
            if (tmp == null) break;
            
            backtrackPos = pos;
            if (res == null)
                res = tmp;
            else
                res.addNext(tmp);
        }
        pos = backtrackPos;
        if (res == null) res = new ASTree("");
        return res;
    }

    private ASTree negPrediction(Peg.NegPrediction np) throws Exception {
        if (debug) System.out.println("evaluate negPrediction " + np.toString());
        
        int backtrackPos = pos;
        ASTree res = eval(np.expr);
        if (res == null) {
            pos = backtrackPos;
            return new ASTree("");
        }
        else {   
            return null;
        }
    }

    private ASTree empty(Peg.Empty emp) throws Exception {
        if (debug) System.out.println("evaluate Empty");
        return new ASTree("");
    }

    public static void main(String[] args) {
        List<PegParser> testcase = new LinkedList<PegParser>();

        testcase.add(
            new PegParser(
                new Rules().
                    addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term"))).
                    addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Num"))).
                    addRule(Peg.nt("Num"), Peg.t("1")).
                    addStartRule(Peg.nt("Expr")), "1+1+1+1"));

        testcase.add(
            new PegParser(
                new Rules().
                    addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term"))).
                    addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Num"))).
                    addRule(Peg.nt("Num"), Peg.ch(Peg.t("0"), Peg.seq(Peg.t("1"), Peg.rep(Peg.t("1"))))).
                    addStartRule(Peg.nt("Expr")), "1+11")
        );

        testcase.add(
            new PegParser(
                new Rules().
                    addRule(Peg.nt("S"), Peg.seq(Peg.np(Peg.np(Peg.seq(Peg.nt("A"), Peg.np(Peg.t("b"))))), Peg.seq(Peg.t("a"), Peg.rep(Peg.t("a"))), Peg.nt("B"))).
                    addRule(Peg.nt("A"), Peg.seq(Peg.t("a"), Peg.ch(Peg.nt("A"), Peg.emp()), Peg.t("b"))).
                    addRule(Peg.nt("B"), Peg.seq(Peg.t("b"), Peg.ch(Peg.nt("B"), Peg.emp()), Peg.t("c"))).
                    addStartRule(Peg.nt("S")), 
                    "aabbcc")
        );

        testcase.add(
            new PegParser(
                new Rules().
                    addRule(Peg.nt("S"), Peg.seq(Peg.np(Peg.np(Peg.seq(Peg.nt("A"), Peg.np(Peg.t("b"))))), Peg.seq(Peg.t("a"), Rules.repMT0(Peg.t("a"))), Peg.nt("B"))).
                    addRule(Peg.nt("A"), Peg.seq(Peg.t("a"), Peg.ch(Peg.nt("A"), Peg.emp()), Peg.t("b"))).
                    addRule(Peg.nt("B"), Peg.seq(Peg.t("b"), Peg.ch(Peg.nt("B"), Peg.emp()), Peg.t("c")))
                    , "aabbcc"
        ));
        

        try {
            for (PegParser test: testcase) {
                test.parse().print();
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }

}
