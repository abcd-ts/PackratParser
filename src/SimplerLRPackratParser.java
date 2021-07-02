import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

// 梅田さんの卒論に基づいた左再帰を解析できるPackrat Parser
public class SimplerLRPackratParser extends PackratParser {
    protected LRMemo memo;

    public SimplerLRPackratParser(Rules rules, String input) {
        super(rules, input);
        this.memo = new LRMemo();
        debug = false;
    }

    public SimplerLRPackratParser(Rules rules) {
        super(rules);
    }

    @Override
    public ASTree applyRule(Peg e, int p) throws Exception {
        if (debug) System.out.println("apply " + e);

        Peg.NonTerminal nt = (Peg.NonTerminal) e;
        if (memo.getAst(nt, p) == null) {
            memo.put(nt, p, ASTree.FAIL, p, false); // メモ表にFAILを記録
            Peg exp = rules.getRule(nt);
            ASTree ans = eval(exp);
            memo.put(nt, p, ans, pos); // growは触らない
            if (memo.getGrow(nt, p)) { // 左再帰を検出
                growLR(nt, p); // 規則を繰り返し適用
                memo.putGrow(nt, p, false);
                ans = memo.getAst(nt, p);
                pos = memo.getPosNext(nt, p);
            }
            return ans;
        }
        else if (memo.getAst(nt, p).equals(ASTree.FAIL)) {
            memo.put(nt, p, ASTree.MISMATCH, memo.getPosNext(nt, p), true); // FAILを消し，growをtrueに
            return ASTree.MISMATCH;
        }
        else {
            pos = memo.getPosNext(nt, p);
            return memo.getAst(nt, p);
        }
    }

    public void growLR(Peg.NonTerminal nt, int p) throws Exception {
        int oldPos;
        isGrow = true;
        while (true) {
            oldPos = pos;
            pos = p;
            Peg exp = rules.getRule(nt);
            Set<Peg.NonTerminal> limits = new HashSet<Peg.NonTerminal>();
            limits.add(nt);
            ASTree ans = eval(exp, p, limits);
            if ((!ASTree.isMATCH(ans)) || pos <= oldPos) break;

            memo.put(nt, p, ans, pos);
        }
        isGrow = false;
    }

    boolean isGrow = false;

    @Override
    public ASTree eval(Peg e) throws Exception {
        return eval(e, pos, new HashSet<Peg.NonTerminal>());
    }

    public ASTree eval(Peg e, int p, Set<Peg.NonTerminal> limits) throws Exception {
        if (e instanceof Peg.Empty) {
            return new ASTree("");
        }

        if (e instanceof Peg.Terminal) {
            if (debug) System.out.println("eval g Terminal " + e.toString() + "");

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
            if (debug) System.out.println("eval g NegPrediction !" + e.toString());

            if (ASTree.isMATCH(eval(((Peg.NegPrediction)e).expr, p, limits))) {
                pos = p;
                return ASTree.MISMATCH;
            }
            else {
                return new ASTree("");
            }
        }

        if (e instanceof Peg.NonTerminal) {
            if (debug) System.out.println("eval g NonTerminal " + e.toString());

            ASTree tmp = null;
            if (pos == p && isGrow && !limits.contains((Peg.NonTerminal)e)) {
                tmp = applyRuleGrow(e, pos, limits);
            }
            else {
                tmp = applyRule(e, pos);
            }
            if (tmp.equals(ASTree.MISMATCH))
                return tmp;
            
            //System.out.println(tmp.label);
            ASTree res = new ASTree(((Peg.NonTerminal)e).name);
            res.addChild(tmp);
            return res;
        }

        if (e instanceof Peg.Choice) {
            if (debug) System.out.println("eval g Choice " + e.toString());

            Peg.Choice ch = (Peg.Choice)e;
            ASTree res = null;
            for (Peg expr: ch.exprs) {
                if (ASTree.isMATCH(res = eval(expr, p, limits))) {
                    return res;
                }
            }
            return res;
        }

        if (e instanceof Peg.Sequence) {
            if (debug) System.out.println("eval g Sequence " + e.toString());

            Peg.Sequence seq = (Peg.Sequence)e;
            ASTree res = null, tmp = null; 
            for (Peg expr: seq.exprs) {
                if (!(tmp = eval(expr, p, limits)).equals(ASTree.MISMATCH)) {
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

    public ASTree applyRuleGrow(Peg e, int p, Set<Peg.NonTerminal> limits) throws Exception {
        if (debug) System.out.println("apply g " + e.toString());

        Peg.NonTerminal nt = (Peg.NonTerminal)e;
        limits.add(nt);
        Peg exp = rules.getRule(nt);
        ASTree ans = eval(exp, p, limits);
        if (ans == ASTree.MISMATCH || ans == ASTree.FAIL || ans == null || pos <= memo.getPosNext(nt, p)) {
            ans = memo.getAst(nt, p);
            pos = memo.getPosNext(nt, p);
        }
        else {
            memo.put(nt, p, ans, pos);
        }
        return ans;
    }

    public static void main(String[] args) {
        List<Parser> testcase = new LinkedList<Parser>();

        testcase.add(
            new SimplerLRPackratParser(
                new Rules().
                    addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Term"), Peg.t("+"), Peg.nt("Expr")), Peg.nt("Term"))).
                    addRule(Peg.nt("Term"), Peg.ch(Peg.seq(Peg.t("("), Peg.nt("Expr"), Peg.t(")")), Peg.nt("Num"))).
                    addRule(Peg.nt("Num"), Peg.t("1"))
                    , "1+1+1+1"
        ));

        testcase.add(
            new SimplerLRPackratParser(
                new Rules().
                    addRule(Peg.nt("S"), Peg.seq(Peg.np(Peg.np(Peg.seq(Peg.nt("A"), Peg.np(Peg.t("b"))))), Peg.t("a"), Rules.repMT0(Peg.t("a")), Peg.nt("B"))).
                    addRule(Peg.nt("A"), Peg.seq(Peg.t("a"), Peg.ch(Peg.nt("A"), Peg.emp()), Peg.t("b"))).
                    addRule(Peg.nt("B"), Peg.seq(Peg.t("b"), Peg.ch(Peg.nt("B"), Peg.emp()), Peg.t("c")))
                    , "aabbcc"
        ));
        
        // 左再帰を含む規則
        Rules directLR = new Rules()
            .addRule(Peg.nt("lr"), Peg.ch(Peg.seq(Peg.nt("lr"), Peg.t("1")), Peg.t("1"))
        );

        Rules indirectLR = new Rules()
            .addRule(Peg.nt("lr1"), Peg.ch(Peg.seq(Peg.nt("x"), Peg.t("1")), Peg.t("1")))
            .addRule(Peg.nt("x"), Peg.nt("lr1")
        );

        Rules multiChoiceLR = new Rules()
            .addRule(Peg.nt("S"), Peg.ch(Peg.seq(Peg.nt("A"), Peg.t("b")), Peg.t("b")))
            .addRule(Peg.nt("A"), Peg.ch(Peg.seq(Peg.nt("A"), Peg.t("a")), Peg.seq(Peg.nt("S"), Peg.t("a")))
        );

        Rules multiLR = new Rules()
            .addRule(Peg.nt("Expr"), Peg.ch(Peg.seq(Peg.nt("Expr"), Peg.t("+"), Peg.nt("Num")), Peg.nt("Num")))
            .addRule(Peg.nt("Num"), Peg.ch(Peg.seq(Peg.nt("Num"), Peg.nt("DIGIT")), Peg.nt("DIGIT")))
            .addRule(Peg.nt("DIGIT"), Peg.ch(Peg.t("0"), Peg.t("1"))
        );

        Rules bab_bab = new Rules()
            .addRule(Peg.nt("S"), Peg.seq(Peg.nt("A"), Peg.t("-"), Peg.nt("A")))
            .addRule(Peg.nt("A"), Peg.ch(Peg.seq(Peg.nt("B"), Peg.t("b")), Peg.t("b")))
            .addRule(Peg.nt("B"), Peg.ch(Peg.seq(Peg.nt("B"), Peg.t("a")), Peg.seq(Peg.nt("A"), Peg.t("a")))
        );

        testcase.add( // Warthらの論文の直接左再帰
            new SimplerLRPackratParser(directLR, "11111")
        );

        testcase.add( // Warthらの論文の間接左再帰
            new SimplerLRPackratParser(indirectLR, "11111")
        );
        
        testcase.add( // Warthらの手法で解析できないとされる，同じ解析位置に複数の左再帰が発生する例
            new SimplerLRPackratParser(multiChoiceLR, "baab")
        );
        
        testcase.add( // 複数箇所に複数の左再帰を含む例(後藤らの手法で失敗する例) 
            new SimplerLRPackratParser(multiLR, "10+10")
        );

        testcase.add(
            new SimplerLRPackratParser(bab_bab, "bab-bab")
        );

        testcase.add(   // Medeirosの論文で，Warthの手法では失敗するとされていた例
            new SimplerLRPackratParser(
                new Rules()
                .addRule(Peg.nt("S"), Peg.nt("X"))
                .addRule(Peg.nt("X"), Peg.ch(Peg.seq(Peg.nt("X"), Peg.nt("Y")), Peg.emp()))
                .addRule(Peg.nt("Y"), Peg.t("x"))
            , "xxx")
        );

        try {
            for (Parser test: testcase) {
                test.parse().print();
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
    
}
