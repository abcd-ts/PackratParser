import java.util.HashMap;
import java.util.Map;

public class Rules {
    Map<Peg.NonTerminal, Peg> rules;
    Peg.NonTerminal start = null;

    public Rules() {
        this.rules = new HashMap<Peg.NonTerminal, Peg>();
        currentRules = this;
    }

    public Rules addStartRule(Peg.NonTerminal nt) {
        this.start = nt;
        return this;
    }
    
    public Rules addRule(Peg.NonTerminal nt, Peg expr) {
        currentRules = this;
        if (start == null && !nt.name.contains("#")) start = nt;  // デフォルトでは，最初に追加規則が開始規則
        rules.put(nt, expr);
        return this;
    }

    public Peg getRule(Peg nt) {
        if (rules.containsKey(nt)) return rules.get(nt);
        else return null;
    }

    public void printRule() {
        printRule("");
    }

    public void printRule(String discription) {
        System.out.println(discription);
        System.out.println(this.start + " <- " + this.getRule(start).toString());
        for (Peg.NonTerminal nt: this.rules.keySet()) {
            if (nt.equals(start)) continue;
            System.out.println(nt.name + " <- " + this.getRule(nt).toString());
        }
    }

    private static Rules currentRules;
    private int cntRepMT0 = 0;
    private int cntRepMT1 = 0;
    
    public static Peg repMT0(Peg expr) {
        Peg.NonTerminal nt = Peg.NonTerminal.getNt("#REP0_No." + (currentRules.cntRepMT0++) + "#");
        currentRules.addRule(nt, Peg.ch(Peg.seq(expr, nt), Peg.emp()));
        return nt;
    }

    public static Peg repMT1(Peg expr) {
        Peg.NonTerminal nt = Peg.NonTerminal.getNt("#REP1_No." + (currentRules.cntRepMT1++) + "#");
        currentRules.addRule(nt, Peg.ch(Peg.seq(expr, nt), expr));
        return nt;
    }

    // Positive Prediction
    private int cntPosPrediction = 0;
    public static Peg pp(Peg expr) {
        Peg.NonTerminal nt = Peg.NonTerminal.getNt("#POSP_No." + (currentRules.cntPosPrediction++) + "#");
        currentRules.addRule(nt, Peg.np(Peg.np(expr)));
        return nt;
    }

    // Any match
    private int cntAny = 0;
    public static Peg any(Peg expr) {
        Peg.NonTerminal nt = Peg.NonTerminal.getNt("#ANY_No." + (currentRules.cntAny++) + "#");
        currentRules.addRule(nt, Peg.ch(expr, Peg.emp()));
        return nt;
      
    }

    public static void main(String[] args) {
        Rules simple = new Rules().
            addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term"))).
            addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Num"))).
            addRule(Peg.nt("Num"), Peg.t("1"));

        Rules rep = new Rules().
            addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term"))).
            addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Num"))).
            addRule(Peg.nt("Num"), Peg.ch(Peg.t("0"), Peg.seq(Peg.t("1"), Rules.repMT0(Peg.t("1")))));

        rep.printRule();
        
        Rules abc = new Rules().
            addRule(Peg.nt("S"), Peg.seq(Peg.np(Peg.np(Peg.seq(Peg.nt("A"), Peg.np(Peg.t("b"))))), Peg.seq(Peg.t("a"), Peg.rep(Peg.t("a"))), Peg.nt("B"))).
            addRule(Peg.nt("A"), Peg.seq(Peg.t("a"), Peg.ch(Peg.nt("A"), Peg.emp()), Peg.t("b"))).
            addRule(Peg.nt("B"), Peg.seq(Peg.t("b"), Peg.ch(Peg.nt("B"), Peg.emp()), Peg.t("c")));

        Rules abc2 = new Rules().
            addRule(Peg.nt("S"), Peg.seq(Peg.np(Peg.np(Peg.seq(Peg.nt("A"), Peg.np(Peg.t("b"))))), Peg.seq(Peg.t("a"), Rules.repMT0(Peg.t("a"))), Peg.nt("B"))).
            addRule(Peg.nt("A"), Peg.seq(Peg.t("a"), Peg.ch(Peg.nt("A"), Peg.emp()), Peg.t("b"))).
            addRule(Peg.nt("B"), Peg.seq(Peg.t("b"), Peg.ch(Peg.nt("B"), Peg.emp()), Peg.t("c")));

        Rules testRule = new Rules()
            .addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term")))
            .addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.nt("Prim"), Peg.t("*"), Peg.nt("Term")), Peg.nt("Prim")))
            .addRule(Peg.nt("Prim"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Func"), Peg.nt("Name"), Peg.nt("Num")))
            .addRule(Peg.nt("Func"), Peg.seq(Peg.nt("Name"), Peg.t("("), Peg.nt("List"), Peg.t(")")))
            .addRule(Peg.nt("Name"), Peg.ch(Peg.seq(Peg.t("a"), Peg.nt("Name")), Peg.t("a")))
            .addRule(Peg.nt("Num"), Peg.ch(Peg.seq(Peg.t("1"), Peg.nt("Num")), Peg.t("1")))
            .addRule(Peg.nt("List"), Peg.ch(Peg.seq(Peg.nt("Expr"), Peg.t(","), Peg.nt("List")), Peg.nt("Expr"), Peg.emp()));
     
        Rules directLR = new Rules()
            .addRule(Peg.nt("lr"), Peg.ch(Peg.seq(Peg.nt("lr"), Peg.t("1")), Peg.t("1")));

        Rules indirectLR = new Rules()
            .addRule(Peg.nt("lr1"), Peg.ch(Peg.seq(Peg.nt("x"), Peg.t("1")), Peg.t("1")))
            .addRule(Peg.nt("x"), Peg.nt("lr1"));

        Rules multiChoiceLR = new Rules()
            .addRule(Peg.nt("S"), Peg.ch(Peg.seq(Peg.nt("A"), Peg.t("b")), Peg.t("b")))
            .addRule(Peg.nt("A"), Peg.ch(Peg.seq(Peg.nt("A"), Peg.t("a")), Peg.seq(Peg.nt("S"), Peg.t("a"))));

        Rules multiLR = new Rules()
            .addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Expr"), Peg.t("+"), Peg.nt("Num")), Peg.nt("Num")))
            .addRule(Peg.nt("Num"), Peg.ch(Peg.seq(Peg.nt("Num"), Peg.nt("DIGIT")), Peg.nt("DIGIT")))
            .addRule(Peg.nt("DIGIT"), Peg.ch(Peg.t("0"), Peg.t("1")));

        directLR.printRule();
        indirectLR.printRule();
        multiChoiceLR.printRule();
        multiLR.printRule();
    }
}
