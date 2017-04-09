package chap3;

import java.io.*;
import java.util.*;

public class KMeansClustering {
    private HashMap data;
    private int dim;

    public static void main(String[] args) {
        KMeansClustering kmc = new KMeansClustering(args[0]);
        kmc.doClustering(0, 4);
    }

    private double[][] findRanges() {
        double[][] ranges = new double[this.dim][];
        for (int i=0; i<this.dim; i++) {
            double[] range = new double[2];
            for (Iterator it=this.data.keySet().iterator(); it.hasNext(); ) {
                double[] freqs = (double[])this.data.get(it.next());
                double freq = freqs[i];
                if (freq < range[0]) range[0] = freq;
                else if(freq > range[1]) range[1] = freq;
            }
            ranges[i] = range;
        }
        return ranges;
    }

    private double distance(double[] vec0, double[] vec1, int method) {
        switch (method) {
            case 0: return eucledianSimilarity(vec0, vec1);
            case 1: return pearsonSimilarity(vec0, vec1);
            case 2:
            default: return cosineSimilarity(vec0, vec1);
        }
    }

    public void doClustering(int method, int k) {
        // ranges[this.dim][2]
        double[][] ranges = findRanges();
        // clusters[k][this.dim]
        double[][] clusters = new double[k][this.dim];
        // random initial centroids
        for (int i=0; i<k; i++) {
            for (int j=0; j<this.dim; j++) {
                double min = ranges[j][0];
                double max = ranges[j][1];
                clusters[i][j] = min+(max-min)*Math.random();
            }
        }

        ArrayList lastmatches = new ArrayList();
        for (int t=0; t<100; t++) {
            ArrayList bestmatches = new ArrayList();
            for (int i=0; i<k; i++) bestmatches.add(new HashSet());

            for (Iterator it=this.data.keySet().iterator(); it.hasNext(); ) {
                String blog = (String)it.next();
                double[] freqs = (double[])this.data.get(blog);
                int bestmatch = 0;
                double d = Double.MAX_VALUE;
                for (int i=0; i<k; i++) {
                    double dist = distance(clusters[i], freqs, method);
                    if (d > dist) { d = dist; bestmatch = i; }
                }
                HashSet set = (HashSet)bestmatches.get(bestmatch);
                set.add(blog);
            }

            if (bestmatches.equals(lastmatches)) break;
            lastmatches = bestmatches;

            for (int i=0; i<k; i++) {
                HashSet set = (HashSet)bestmatches.get(i);
                int count=0;
                double[] row = new double[this.dim];
                for (Iterator it=set.iterator(); it.hasNext(); ) {
                    String blog = (String)it.next();
                    double[] freqs = (double[])this.data.get(blog);
                    for (int j=0; j<this.dim; j++) {
                        row[j] += freqs[j];
                    }
                    count++;
                }
                for (int j=0; j<row.length; j++) {
                    if (count > 0) {
                        row[j] /= count;
                    }
                }
                clusters[i] = row;
            }
        }
    }

    public KMeansClustering(String s) {
        this.data = loadData(s);
        if (this.data == null) System.exit(0);
    }

    private int countWords(String line) {
        // first word is "blog name", not counted
        StringTokenizer st = new StringTokenizer(line, "\t");
        return st.countTokens()-1;
    }

    private Object[] makeData(String line, int n) {
        // first word is "blog name"
        double[] freqs = new double[n];
        StringTokenizer st = new StringTokenizer(line, "\t");
        String blogname = st.nextToken();
        for (int i=0; i<n; i++) {
            freqs[i] = Double.parseDouble(st.nextToken());
        }
        Object[] result = {blogname, freqs};
        return result;
    }

    public HashMap loadData(String fname) {
        HashMap result = new HashMap();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fname));
            String line="";
            // first line is header line
            int n = countWords(br.readLine());
            this.dim = n;
            int count=0;
            System.out.println("Data file contains "+n+" kinds of wordws.");
            while ((line=br.readLine()) != null) {
                Object[] objs = makeData(line, n);
                result.put(objs[0], objs[1]);
                count++;
            }
            br.close();
            System.out.println("Total "+count+" records.");
            return result;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    private double eucledianSimilarity(double[] vec0, double[] vec1) {
        double sum = 0.0;
        for (int i=0; i<vec0.length; i++) {
            double score = vec0[i];
            double score2 = vec1[i];
            sum += (score-score2)*(score-score2);
        }
        double d = Math.sqrt(sum);
        return 1.0/(d+1);
    }

    private double pearsonSimilarity(double[] vec0, double[] vec1) {
        double sum1=0.0, sum2=0.0;
        double sum1sq=0.0, sum2sq=0.0;
        double psum=0.0;
        int n = vec0.length;
        for (int i=0; i<n; i++) {
            double r1 = vec0[i];
            double r2 = vec1[i];
            sum1 += r1;
            sum2 += r2;
            sum1sq += r1*r1;
            sum2sq += r2*r2;
            psum += r1*r2;
        }
        double num = psum-(sum1*sum2)/n;
        double den=Math.sqrt((sum1sq-sum1*sum1/n)*(sum2sq-sum2*sum2/n));
        if (den == 0.0) return 0.0;
        return num/den;
    }

    private double cosineSimilarity(double[] vec0, double[] vec1) {
        double sum1sq=0.0, sum2sq=0.0;
        double psum=0.0;
        int n = vec0.length;
        for (int i=0; i<n; i++) {
            double r1 = vec0[i];
            double r2 = vec1[i];
            sum1sq += r1*r1;
            sum2sq += r2*r2;
            psum += r1*r2;
        }
        double den=Math.sqrt(sum1sq)*Math.sqrt(sum2sq);
        if (den == 0.0) return 0.0;
        return psum/den;
    }
}
