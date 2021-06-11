import java.util.HashMap;

// 左再帰を解析できるPackrat Parser用のメモ化表
public class LRMemo extends Memo {
    HashMap<Peg.NonTerminal, HashMap<Integer, Boolean>> growTable;
    public LRMemo() {
        super();
        this.growTable = new HashMap<Peg.NonTerminal, HashMap<Integer, Boolean>>();
    }

    public void put(Peg.NonTerminal nt, int pos, ASTree ast, int pos_next) {
        if (ntTable.containsKey(nt)) {
            if (ntTable.get(nt).containsKey(pos)) {
                ntTable.get(nt).replace(pos, ast);
                posTable.get(nt).replace(pos, pos_next);
                // メモ化表にntとposの結果があるので，growには触らない
            }
            else {
                ntTable.get(nt).put(pos, ast);
                posTable.get(nt).put(pos, pos_next);
                growTable.get(nt).put(pos, false);
            }
        }
        else {
            ntTable.put(nt, new HashMap<Integer, ASTree>());
            ntTable.get(nt).put(pos, ast);
            posTable.put(nt, new HashMap<Integer, Integer>());
            posTable.get(nt).put(pos, pos_next);
            growTable.put(nt, new HashMap<Integer, Boolean>());
            growTable.get(nt).put(pos, false);
        }
    }

    public void put(Peg.NonTerminal nt, int pos, ASTree ast, int pos_next, boolean grow) {
        if (ntTable.containsKey(nt)) {
            if (ntTable.get(nt).containsKey(pos)) {
                ntTable.get(nt).replace(pos, ast);
                posTable.get(nt).replace(pos, pos_next);
                growTable.get(nt).replace(pos, grow);
            }
            else {
                ntTable.get(nt).put(pos, ast);
                posTable.get(nt).put(pos, pos_next);
                growTable.get(nt).put(pos, grow);
            }
        }
        else {
            ntTable.put(nt, new HashMap<Integer, ASTree>());
            ntTable.get(nt).put(pos, ast);
            posTable.put(nt, new HashMap<Integer, Integer>());
            posTable.get(nt).put(pos, pos_next);
            growTable.put(nt, new HashMap<Integer, Boolean>());
            growTable.get(nt).put(pos, grow);
        }
    }

    public void putGrow(Peg.NonTerminal nt, int pos, boolean grow) {
        if (growTable.containsKey(nt)) {
            if (growTable.get(nt).containsKey(pos)) {
                growTable.get(nt).put(pos, grow);
            }
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

    public boolean getGrow(Peg.NonTerminal nt, int pos) {
        if (growTable.containsKey(nt)) {
            if (growTable.get(nt).containsKey(pos)) {
                return growTable.get(nt).get(pos);
            }
        }
        return false;    
    }
}
