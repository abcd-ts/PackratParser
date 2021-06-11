import java.util.HashMap;

public class Memo {
    HashMap<Peg.NonTerminal, HashMap<Integer, ASTree>> ntTable;
    HashMap<Peg.NonTerminal, HashMap<Integer, Integer>> posTable;

    public Memo() {
        ntTable = new HashMap<Peg.NonTerminal, HashMap<Integer, ASTree>>();
        posTable = new HashMap<Peg.NonTerminal, HashMap<Integer, Integer>>();
    }

    public void put(Peg.NonTerminal nt, int pos, ASTree ast, int pos_next) {
        if (ntTable.containsKey(nt)) {
            if (ntTable.get(nt).containsKey(pos)) {
                ntTable.get(nt).replace(pos, ast);
                posTable.get(nt).replace(pos, pos_next);
            }
            else {
                ntTable.get(nt).put(pos, ast);
                posTable.get(nt).put(pos, pos_next);
            }
        }
        else {
            ntTable.put(nt, new HashMap<Integer, ASTree>());
            ntTable.get(nt).put(pos, ast);
            posTable.put(nt, new HashMap<Integer, Integer>());
            posTable.get(nt).put(pos, pos_next);
        }
    }

    public ASTree getAst(Peg.NonTerminal nt, int pos) {
        if (ntTable.containsKey(nt)) {
            if (ntTable.get(nt).containsKey(pos)) {
                return ntTable.get(nt).get(pos);
            }
        }
        return null;
    }

    public int getPosNext(Peg.NonTerminal nt, int pos) {
        if (posTable.containsKey(nt)) {
            if (posTable.get(nt).containsKey(pos)) {
                return posTable.get(nt).get(pos);
            }
        }
        return -1;
    }
}
