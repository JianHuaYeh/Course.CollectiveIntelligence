package chap7;

import java.io.*;
import java.util.*;

public class CART {
    private ArrayList dataset;
    
    public static void main(String[] args) {
        CART cart = new CART(args[0]);
        cart.go();
    }

    private void loadFile(String str) {
        this.dataset = new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new FileReader(str));
            String line="";
            while ((line=br.readLine()) != null) {
                String[] data = new String[5];
                StringTokenizer st = new StringTokenizer(line, "\t");
                data[0] = st.nextToken().trim();
                data[1] = st.nextToken().trim();
                data[2] = st.nextToken().trim();
                data[3] = st.nextToken().trim();
                data[4] = st.nextToken().trim();
                this.dataset.add(data);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashSet[] divideSet(int column, double value) {
        HashSet[] result = {null, null};
        HashSet set1 = new HashSet();
        HashSet set2 = new HashSet();
        for (Iterator it=this.dataset.iterator(); it.hasNext(); ) {
            String[] data = (String[])it.next();
            double val = Double.parseDouble(data[column]);
            if (val >= value) set1.add(data);
            else set2.add(data);
        }
        result[0] = set1;
        result[1] = set2;
        return result;
    }

    public HashSet[] divideSet(int column, String value) {
        HashSet[] result = {null, null};
        HashSet set1 = new HashSet();
        HashSet set2 = new HashSet();
        for (Iterator it=this.dataset.iterator(); it.hasNext(); ) {
            String[] data = (String[])it.next();
            String val = data[column];
            if (val.equals(value)) set1.add(data);
            else set2.add(data);
        }
        result[0] = set1;
        result[1] = set2;
        return result;
    }

    public HashMap uniqueCounts(HashSet set) {
        HashMap result = new HashMap();
        for (Iterator it=set.iterator(); it.hasNext(); ) {
            String[] data = (String[])it.next();
            String str = data[data.length-1];
            Object obj = result.get(str);
            if (obj != null) {
                int freq = ((Integer)obj).intValue()+1;
                result.put(str, new Integer(freq));
            }
            else {
                result.put(str, new Integer(1));
            }
        }
        return result;
    }
    
    public double GiniImpurity(ArrayList al) {
        HashSet set = new HashSet(al);
        return this.GiniImpurity(set);
    }

    public double GiniImpurity(HashSet set) {
        double total = set.size();
        HashMap counts = uniqueCounts(set);
        double imp = 0.0;
        Set keys = counts.keySet();
        ArrayList keya = new ArrayList(keys);
        for (int i=0; i<keya.size(); i++) {
            String key1 = (String)keya.get(i);
            double p1 = ((Integer)counts.get(key1)).intValue()/total;
            for (int j=0; j<keya.size(); j++) {
                if (i==j) continue;
                String key2 = (String)keya.get(j);
                double p2 = ((Integer)counts.get(key2)).intValue()/total;
                imp += p1*p2;
            }
        }
        return imp;
    }

    public double entropy(ArrayList al) {
        HashSet set = new HashSet(al);
        return this.entropy(set);
    }

    public double entropy(HashSet set) {
        double total = set.size();
        HashMap counts = uniqueCounts(set);
        double ent = 0.0;
        for (Iterator it=counts.values().iterator(); it.hasNext(); ) {
            int val = ((Integer)it.next()).intValue();
            double p = val/total;
            ent = ent-p*Math.log(p)/Math.log(2);
        }
        return ent;
    }

    private double scoref(HashSet data, int split) {
        switch (split) {
            case 0: // Gini impurity
                return GiniImpurity(data);
            case 1:
            default:
                return entropy(data);
        }
    }

    private double scoref(ArrayList data, int split) {
        switch (split) {
            case 0: // Gini impurity
                return GiniImpurity(data);
            case 1:
            default:
                return entropy(data);
        }
    }

    public DecisionNode buildTree(ArrayList data, int split) {
        if (data.size() == 0) return new DecisionNode();
        double current_score = scoref(data, split);

        double best_gain = 0.0;
        //Object[] best_criteria = {null, null};
        int best_col = -1;
        String best_value = "";
        HashSet[] best_set = {null, null};

        int column_count = ((String[])data.get(0)).length-1;
        for (int col=0; col<column_count; col++) {
            HashMap column_values = new HashMap();
            for (int row=0; row<data.size(); row++) {
                column_values.put(((String[])data.get(row))[col],
                        new Integer(1));
            }
            for (Iterator it=column_values.keySet().iterator(); it.hasNext(); ) {
                String key = (String)it.next();
                HashSet[] sets = divideSet(col, key);
                // calculate information gain
                double p = ((double)sets[0].size())/data.size();
                double gain = current_score-p*scoref(sets[0], split)-
                        (1-p)*scoref(sets[1], split);
                if ((gain > best_gain) && (sets[0].size() > 0) &&
                        (sets[1].size() > 0)) {
                    best_gain = gain;
                    best_col = new Integer(col);
                    best_value = key;
                    best_set = sets;
                }
            }
        }

        if (best_gain > 0) {
            DecisionNode tb = buildTree(new ArrayList(best_set[0]),
                    split);
            DecisionNode fb = buildTree(new ArrayList(best_set[1]),
                    split);
            return new DecisionNode(best_col, best_value, "N/A",
                    tb, fb);
        }

        return new DecisionNode();
    }

    public void printTree(DecisionNode root) {
        printTree(root, "");
    }

    public void printTree(DecisionNode node, String indent) {
        // leaf node ?
        if (!node.getResults().equals("N/A"))
            System.out.println(node.getResults());
        else {
            System.out.println(node.getCol()+":"+node.getValue()+"?");
            System.out.println(indent+"T->");
            printTree(node.getTB(), indent+" ");
            System.out.println(indent+"F->");
            printTree(node.getFB(), indent+" ");
        }
    }

    public String classify(String[] observation, DecisionNode tree) {
        if (!tree.getResults().equals("N/A"))
            return tree.getResults();

        int col = tree.getCol();
        String val = observation[col];
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        return null;
    }

    public void prune(DecisionNode tree, double mingain) {
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public void mdClassify(String[] observation, DecisionNode tree) {
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public CART(String fname) {
        this.loadFile(fname);
    }

    private void printData(String[] data) {
        for (int i=0; i<data.length; i++) {
            System.out.print(data[i]+" ");
        }
        System.out.println();
    }

    public void go() {
        HashSet[] result = this.divideSet(2, "yes");
        for (Iterator it=result[0].iterator(); it.hasNext(); ) {
            printData((String[])it.next());
        }
        System.out.println("=================================");
        for (Iterator it=result[1].iterator(); it.hasNext(); ) {
            printData((String[])it.next());
        }
        System.out.println("=================================");
        //System.out.println("Gini impurity = "+this.GiniImpurity(this.dataset));
        //System.out.println("Entropy = "+this.entropy(this.dataset));
        System.out.println("Entropy for set1 = "+this.entropy(result[0]));
        System.out.println("Gini impurity for set1 = "+this.GiniImpurity(result[0]));
    }

}
