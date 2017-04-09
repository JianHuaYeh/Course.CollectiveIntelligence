package chap7;

public class DecisionNode {
    private int col;
    private String value;
    private String results;
    private DecisionNode tb;
    private DecisionNode fb;

    public DecisionNode() { this(-1);  }
    public DecisionNode(int col) { this(col, null); }
    public DecisionNode(int col, String value) { this(col, value, "N/A"); }
    public DecisionNode(int col, String value, String results) {
        this(col, value, results, null, null);
    }
    public DecisionNode(int col, String value, String results,
            DecisionNode tb, DecisionNode fb) {
        this.col = col;
        this.value = value;
        this.results = results;
        this.tb = tb;
        this.fb = fb;
    }

    public String getResults() { return this.results; }
    public int getCol() { return this.col; }
    public String getValue() { return this.value; }
    public DecisionNode getTB() { return this.tb; }
    public DecisionNode getFB() { return this.fb; }

}
