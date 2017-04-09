package chap3;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;

public class HClustering {
    private HashMap data;

    public static void main(String[] args) {
        HClustering hc = new HClustering(args[0]);
        hc.doClustering(0);
    }

    public HClustering(String s) {
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

    private ArrayList makeInitialClusters() {
        ArrayList result = new ArrayList();
        for (Iterator it=this.data.keySet().iterator(); it.hasNext(); ) {
            String blogname = (String)it.next();
            double[] freqs = (double[])this.data.get(blogname);
            BiCluster cl = new BiCluster(blogname, freqs);
            result.add(cl);
        }
        return result;
    }

    private double distance(BiCluster c0, BiCluster c1, int method) {
        double[] vec0 = c0.getVec();
        double[] vec1 = c1.getVec();
        switch (method) {
            case 0: return 1.0/eucledianSimilarity(vec0, vec1);
            case 1: return 1.0/pearsonSimilarity(vec0, vec1);
            case 2:
            default: return 1.0/cosineSimilarity(vec0, vec1);
        }
    }

    private double[] mergeVec(double[] vec0, double[] vec1) {
        double[] result = new double[vec0.length];
        for (int i=0; i<vec0.length; i++) {
            result[i] = (vec0[i]+vec1[i])/2.0;
        }
        return result;
    }

    public void doClustering(int method) {
        ArrayList clust = makeInitialClusters();

        BiCluster c0 = (BiCluster)clust.get(0);
        BiCluster c1 = (BiCluster)clust.get(1);
        double closest = distance(c0, c1, method);
        while (clust.size() > 1) {
            int[] lowestpair = new int[2];
            lowestpair[0] = 0; lowestpair[1] = 1;
            //System.out.println("Cluster size = "+clust.size());
            for (int i=0; i<clust.size(); i++) {
                for (int j=i+1; j<clust.size(); j++) {
                    c0 = (BiCluster)clust.get(i);
                    c1 = (BiCluster)clust.get(j);
                    double dist = distance(c0, c1, method);
                    if (dist < closest) {
                        closest = dist;
                        lowestpair[0] = i;
                        lowestpair[1] = j;
                    }
                } // for j
            } // for i

            //System.out.println("Lowest pair: "+lowestpair[0]+", "+lowestpair[1]);
            c0 = (BiCluster)clust.get(lowestpair[0]);
            c1 = (BiCluster)clust.get(lowestpair[1]);
            String newid = "{"+c0.getId()+","+c1.getId()+"}";
            double[] mergeVec = mergeVec(c0.getVec(), c1.getVec());
            BiCluster newcluster = new BiCluster(newid, mergeVec, c0, c1);
            clust.remove(c1);
            clust.remove(c0);
            clust.add(newcluster);
        } // while

        // only one cluster left
        BiCluster result = (BiCluster)clust.get(0);
        System.out.println(result.getId());

        // make JTree
        DefaultMutableTreeNode root = makeTree(result);
        JTree tree = new JTree(root);
        JScrollPane pane = new JScrollPane(tree);
        JFrame frame = new JFrame();
        frame.add(pane);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }

    private DefaultMutableTreeNode makeTree(BiCluster bc) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(bc);
        BiCluster left = bc.getLeftNode();
        BiCluster right = bc.getRightNode();
        if (left==null && right==null) return node;
        DefaultMutableTreeNode lnode = makeTree(left);
        DefaultMutableTreeNode rnode = makeTree(right);
        node.add(lnode);
        node.add(rnode);
        return node;
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
