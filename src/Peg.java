import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Peg{
    String label;
    public Peg(String label) {
        this.label = label;
    }

    public static class Terminal extends Peg {
        String str;
        public Terminal(String str) {
            super("#Terminal#:\"" + str + "\"");
            this.str = str;
        }

        public String toString() { return "\"" + str + "\""; }
    }

    public static class NonTerminal extends Peg {
        String name;
        private static Map<String, NonTerminal> map = new HashMap<String, NonTerminal>();
        
        public NonTerminal(String name) {
            super("#NonTerminal#:<" + name + ">");
            this.name = name;
        }

        public static NonTerminal getNt(String name) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
            else {
                NonTerminal nt = new NonTerminal(name);
                map.put(name, nt);
                return nt;
            }
        }

        public int hashCode() {
            return this.name.hashCode();
        }

        public String toString() { return name; }
    }

    public static class Sequence extends Peg {
        List<Peg> exprs;
        public Sequence(Peg...exprs) {
            super("#Sequence#");
            this.exprs = Arrays.asList(exprs);
        }

        public String toString() {
            String str = "";
            for (Peg e: exprs) str += e + " ";
            return str;
        }
    }

    public static class Choice extends Peg {
        List<Peg> exprs;
        public Choice(Peg...exprs) {
            super("#Choice#");
            this.exprs = Arrays.asList(exprs);
        }

        public String toString() {
            String str = "";
            for (Peg e: exprs) str += e + "/";
            return str;
        }
    }

    public static class Repeat extends Peg {
        Peg expr;
        public Repeat(Peg expr) {
            super("#Repeat#");
            this.expr = expr;
        }

        public String toString() { return expr + "*"; }
    }

    public static class NegPrediction extends Peg{
        Peg expr;
        public NegPrediction(Peg expr) {
            super("#NegPrediction#");
            this.expr = expr;
        }

        public String toString() { return "!(" + expr + ")"; }
    }

    public static class Empty extends Peg{
        public Empty() {
            super("#Empty#");
        }

        public String toString() { return "ε";}
    }

    public static Terminal t(String str) { return new Terminal(str); }
    public static NonTerminal nt(String name) { return NonTerminal.getNt(name); }
    public static Sequence seq(Peg...exprs) { return new Sequence(exprs); }
    public static Choice ch(Peg...exprs) { return new Choice(exprs); }
    public static Repeat rep(Peg expr) { return new Repeat(expr); }
    public static NegPrediction np(Peg expr) { return new NegPrediction(expr); }
    public static Empty emp() { return new Empty(); }

    public static void main(String[] args) {
        Peg rule = new Choice(
            new Sequence(new NonTerminal("Term"), new Terminal("+"), new NonTerminal("Expr")),
            new NonTerminal("Term"));

        System.out.println(rule.toString());
    }
}
