import java.util.List;
import java.util.LinkedList;

public class PackratParser implements Parser{
    protected Rules rules;
    protected String input;
    protected int pos;
    protected Peg.NonTerminal start;
    protected Memo memo;

    protected boolean debug = false;

    public PackratParser(Rules rules, String input) {
        this.rules = rules;
        this.start = rules.start;
        this.input = input;
        this.pos = 0;
        this.memo = new Memo();
    }

    public PackratParser(Rules rules) {
        this.rules = rules;
        this.start = rules.start;
        this.input = "";
        this.pos = 0;
        this.memo = new Memo();
    }

    public ASTree parse() throws Exception {
        ASTree res = eval(start);
        if (res.equals(ASTree.MISMATCH)) {
            throw new Exception("解析失敗");
        }
        return res;
    }

    public void setInput(String input) {
        this.input = input;
    }

    private int cnt = 0;
    public ASTree eval(Peg e) throws Exception{
        if (debug){
            cnt++;
            if (cnt > 500) throw new Exception("infinite computation");
        }
        
        int p = pos;
        if (e instanceof Peg.Empty) {
            return new ASTree("");
        }

        if (e instanceof Peg.Terminal) {
            if (debug) System.out.println("eval Terminal " + e.toString() + "");

            Peg.Terminal t = (Peg.Terminal)e;
            if (pos < input.length() && input.substring(pos, pos + 1).equals(t.str)) {
                pos += t.str.length();
                return new ASTree("\"" + t.str + "\"");
            }
            else {
                return ASTree.MISMATCH;
            }
        }

        if (e instanceof Peg.NegPrediction) {
            if (debug) System.out.println("eval NegPrediction !" + e.toString());

            if (!eval(((Peg.NegPrediction)e).expr).equals(ASTree.MISMATCH)) {
                pos = p;
                return ASTree.MISMATCH;
            }
            else {
                return new ASTree("");
            }
        }

        if (e instanceof Peg.NonTerminal) {
            if (debug) System.out.println("eval NonTerminal <" + e.toString() + ">");

            ASTree tmp = applyRule(e, pos);
            if (tmp.equals(ASTree.MISMATCH))
                return tmp;
            
            ASTree res = new ASTree(((Peg.NonTerminal)e).name);
            res.addChild(tmp);
            return res;
        }

        if (e instanceof Peg.Choice) {
            if (debug) System.out.println("eval Choice " + e.toString());

            Peg.Choice ch = (Peg.Choice)e;
            ASTree res = null;
            for (Peg expr: ch.exprs) {
                if (!(res = eval(expr)).equals(ASTree.MISMATCH)) {
                    return res;
                }
            }
            return res;
        }

        if (e instanceof Peg.Sequence) {
            if (debug) System.out.println("eval Sequence" + e.toString());

            Peg.Sequence seq = (Peg.Sequence)e;
            ASTree res = null, tmp = null; 
            for (Peg expr: seq.exprs) {
                if (!(tmp = eval(expr)).equals(ASTree.MISMATCH)) {
                    if (res == null) {
                        res = tmp;
                    }
                    else {
                        res.addNext(tmp);
                    }
                    continue;
                }
                else {
                    pos = p;
                    return ASTree.MISMATCH;
                }
            }
            return res;
        }

        throw new Exception(e + " is invalid expression.");
    }

    public ASTree applyRule(Peg e, int p) throws Exception {
        if (debug) System.out.println("apply " + e.toString());
        
        Peg.NonTerminal nt = (Peg.NonTerminal) e;
        if (memo.getAst(nt, p) == null) {
            Peg exp = rules.getRule(nt);
            ASTree ans = eval(exp);
            memo.put(nt, p, ans, pos);
            return ans; 
        }
        else {
            pos = memo.getPosNext(nt, p);
            return memo.getAst(nt, p);
        }
    }

    public static void main(String[] args) {
        List<PackratParser> testcase = new LinkedList<PackratParser>();
        
        testcase.add(
            new PackratParser(
                new Rules().
                    addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term"))).
                    addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Num"))).
                    addRule(Peg.nt("Num"), Peg.t("1"))
                    , "1+1+1+1"
        ));

        testcase.add(
            new PackratParser(
                new Rules().
                    addRule(Peg.nt("S"), Peg.seq(Peg.np(Peg.np(Peg.seq(Peg.nt("A"), Peg.np(Peg.t("b"))))), Peg.t("a"), Rules.repMT0(Peg.t("a")), Peg.nt("B"))).
                    addRule(Peg.nt("A"), Peg.seq(Peg.t("a"), Peg.ch(Peg.nt("A"), Peg.emp()), Peg.t("b"))).
                    addRule(Peg.nt("B"), Peg.seq(Peg.t("b"), Peg.ch(Peg.nt("B"), Peg.emp()), Peg.t("c")))
                    , "aabbcc"
        ));

        try {
            for (PackratParser test: testcase) {
                test.parse().print();
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
