package chap6;

import java.util.*;

public class Classifier {
    private HashMap fc;
    private HashMap cc;
    private HashMap thresholds;

    public HashSet getFeatures(String doc) {
        StringTokenizer st = new StringTokenizer(doc, " ,.;'");
        HashSet result = new HashSet();
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if ((tok.length() > 2) && (tok.length() < 20))
                result.add(tok.toLowerCase());
        }
        return result;
    }

    public Classifier() {
        this.fc = new HashMap();
        this.cc = new HashMap();
        this.thresholds = new HashMap();
    }

    public void incf(String feature, String category) {
        HashMap map = (HashMap)this.fc.get(feature);
        if (map == null) {
            map = new HashMap();
            map.put(category, new Integer(1));
            this.fc.put(feature, map);
        }
        else {
            Integer ii = (Integer)map.get(category);
            if (ii == null) ii = new Integer(0);
            int freq = ii.intValue()+1;
            map.put(category, freq);
            this.fc.put(feature, map);
        }
    }

    public void incc(String category) {
        Integer ii = (Integer)this.cc.get(category);
        if (ii == null) {
            this.cc.put(category, new Integer(1));
        }
        else {
            this.cc.put(category, new Integer(ii.intValue()+1));
        }
    }

    public double fcount(String feature, String category) {
        HashMap map = (HashMap)this.fc.get(feature);
        if (map == null) return 0.0;
        else {
            Integer ii = (Integer)map.get(category);
            if (ii == null) return 0.0;
            int freq = ii.intValue();
            return (double)freq;
        }
    }

    public int catcount(String category) {
        Integer ii = (Integer)this.cc.get(category);
        if (ii == null) return 0;
        else return ii.intValue();
    }

    public int totalcount() {
        int sum = 0;
        for (Iterator it=this.cc.values().iterator(); it.hasNext(); ) {
            Integer ii = (Integer)it.next();
            sum += ii.intValue();
        }
        return sum;
    }

    public Set categories() {
        return this.cc.keySet();
    }

    public void train(String item, String category) {
        Set fset = this.getFeatures(item);
        for (Iterator it=fset.iterator(); it.hasNext(); ) {
            String feature = (String)it.next();
            this.incf(feature, category);
        }
        this.incc(category);
    }

    public double fprob(String feature, String category) {
        int catcount = this.catcount(category);
        if (catcount == 0) return 0.0;
        return this.fcount(feature, category)/catcount;
    }

    public double weightedprob(String feature, String category) {
        double weight=1.0, ap=0.5;
        double basicprob = this.fprob(feature, category);
        int totals = 0;
        for (Iterator it=this.categories().iterator(); it.hasNext(); ) {
            String cat = (String)it.next();
            totals += this.fcount(feature, cat);
        }
        return ((weight*ap)+(totals*basicprob))/(weight+totals);
    }

    public double naiveBayesDocProb(String item, String category) {
        HashSet features = this.getFeatures(item);
        double prob = 1.0;
        for (Iterator it=features.iterator(); it.hasNext(); ) {
            String feature = (String)it.next();
            prob *= this.weightedprob(feature, category);
        }
        return prob;
    }

    public double naiveBayesProb(String item, String category) {
        double catprob = this.catcount(category)/(double)this.totalcount();
        double docprob = this.naiveBayesDocProb(item, category);
        return docprob*catprob;
    }

    public void setThreshold(String category, double th) {
        this.thresholds.put(category, new Double(th));
    }

    public double getThreshold(String category) {
        Double dd = (Double)this.thresholds.get(category);
        if (dd == null) return 1.0;
        return dd.doubleValue();
    }

    public String classify(String item) {
        String defaultCategory = "N/A";
        HashMap probs = new HashMap();
        double max = 0.0;
        String bestCategory = "";
        for (Iterator it=this.categories().iterator(); it.hasNext(); ) {
            String category = (String)it.next();
            double prob = this.naiveBayesProb(item, category);
            probs.put(category, new Double(prob));
            if (prob > max) {
                max = prob;
                bestCategory = category;
            }
        }

        for (Iterator it=probs.keySet().iterator(); it.hasNext(); ) {
            String category = (String)it.next();
            if (category.equals(bestCategory)) continue;
            double prob = ((Double)probs.get(category)).doubleValue();
            double threshold = this.getThreshold(bestCategory);
            if (prob*threshold > max) return defaultCategory;
        }

        return bestCategory;
    }

    public void sampleTrain() {
        this.train("Nobody owns the water.", "good");
        this.train("the quick rabbit jumps fences", "good");
        this.train("buy pharmaceuticals now", "bad");
        this.train("make quick money at the online casino", "bad");
        this.train("the quick brown fox jumps", "good");
    }

    public void go() {
        sampleTrain();
        System.out.println("Pr(quick|good) = "+this.fprob("money", "good"));
        System.out.println("Weighted prob = "+weightedprob("money", "good"));

        System.out.println("'quick rabbit' in 'good' for Naive Bayes = "+
                naiveBayesProb("quick rabbit", "good"));
        System.out.println("'quick rabbit' in 'bad' for Naive Bayes = "+
                naiveBayesProb("quick rabbit", "bad"));

        System.out.println("Class for 'quick rabbit' = "+
                this.classify("quick rabbit"));
        System.out.println("Class for 'quick money' = "+
                this.classify("quick money"));

        System.out.println("Threshold for bad changed to 3.0");
        this.setThreshold("bad", 3.0);
        System.out.println("Class for 'quick rabbit' = "+
                this.classify("quick rabbit"));
        System.out.println("Class for 'quick money' = "+
                this.classify("quick money"));

        System.out.println("Training for 10 times.");
        for (int i=0; i<10; i++) sampleTrain();

        System.out.println("Class for 'quick rabbit' = "+
                this.classify("quick rabbit"));
        System.out.println("Class for 'quick money' = "+
                this.classify("quick money"));
    }

    public static void main(String[] args) {
        Classifier classifier = new Classifier();
        classifier.go();
    }


}
