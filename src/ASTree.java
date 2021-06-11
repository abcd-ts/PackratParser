import java.util.Arrays;
import java.util.List;

public class ASTree {
    protected String label;
    protected ASTree children; // 最初の子供への参照
    protected ASTree next; // 同じ親を持つ次のノードへの参照
    public final static ASTree MISMATCH = new ASTree("MISMATCH");
    public final static ASTree FAIL = new ASTree("FAIL");

    public final static ASTree MATCH = new ASTree("MATCH");

    protected ASTree(String label, List<ASTree> children) {
        this.label = label;
        this.next = null;
        this.addChildren(children);
    }

    protected ASTree(String label, ASTree...children) {
        this.label = label;
        this.next = null;
        this.addChildren(children);
    }

    protected ASTree(String label) {
        this.label = label;
        this.children = null;
        this.next = null;
    }

    public void addNext(ASTree next) {
        ASTree ast = this;
        while (ast.next != null)
            ast = ast.next;
        
        ast.next = next;
    }

    public void setNext(ASTree next) {
        this.next = next;
    }

    public ASTree getNext() {
        return this.next;
    }

    public String label() {
        return this.label;
    }

    public ASTree children() {
        return children;
    }

    public void addChild(ASTree child) {
        if (children == null) this.children = child;
        else {
            ASTree c = this.children;
            while (c.next != null) {
                c = c.next;
            }
            c.next = child;
        }
    }

    public void addChildren(List<ASTree> children) {
        for (ASTree child: children) {
            addChild(child);
        }
    }

    public void addChildren(ASTree...children) {
        addChildren(Arrays.asList(children));
    }

    public static boolean isMATCH(ASTree ast) {
        if (ast == null || ast.equals(MISMATCH) || ast.equals(FAIL)) return false;
        return true;
    }

    public void printLoop() {
        System.out.print(this.label);
        if (this.children != null) {
            System.out.print("(");
            this.children.printLoop();
            System.out.print(")");
        }
        if (this.next != null) {
            //System.out.print(" ");
            this.next.printLoop();
        }
    }

    public void print() {
        this.printLoop();
        System.out.println();
    }
}