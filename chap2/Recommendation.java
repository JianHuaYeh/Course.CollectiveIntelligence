/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import java.io.*;
/**
 *
 * @author Administrator
 */
public class Recommendation {
    private HashMap data;

    public static void main(String[] args) {
        //Recommendation recomm = new Recommendation(args[0]);
        //recomm.getRecommendations(args[1], 2);
    	Recommendation recomm = new Recommendation("critics.txt");
    	recomm.getRecommendations("Toby", 2);
    }

    public Recommendation(String str) {
        this.data = loadData(str);
    }

    private HashMap loadData(String str) {
        HashMap all = new HashMap();
        try {
            FileReader fr = new FileReader(str);
            BufferedReader br = new BufferedReader(fr);
            String line="";
            HashMap local = new HashMap();
            String uid="";
            while ((line=br.readLine()) != null) {
                if (line.startsWith("=")) {
                    // save old
                    if (!uid.equals("")) all.put(uid, local);
                    // start new
                    local = new HashMap();
                    uid = line.substring(1);
                }
                else {
                    // Snakes on a Plane @ 3.5
                    StringTokenizer st = new StringTokenizer(line, "@");
                    String mid = "";
                    double rating = 0.0;
                    mid = st.nextToken();
                    rating = Double.parseDouble(st.nextToken());
                    local.put(mid.trim(), rating);
                }
            } // while
            all.put(uid, local);
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return all;
    }

    private double eucledianSimilarity(String person, String other) {
        HashMap local = (HashMap)this.data.get(person);
        HashMap local2 = (HashMap)this.data.get(other);
        double sum = 0.0;
        for (Iterator it=local.keySet().iterator(); it.hasNext(); ) {
            String mid = (String)it.next();
            Object obj = local2.get(mid);
            if (obj == null) continue;
            double score = (Double)local.get(mid);
            double score2 = (Double)local2.get(mid);
            sum += (score-score2)*(score-score2);
        }
        double d = Math.sqrt(sum);
        return 1.0/(d+1);
    }

    private double pearsonSimilarity(String person, String other) {
        HashMap p1 = (HashMap)this.data.get(person);
        HashMap p2 = (HashMap)this.data.get(other);
        HashSet si = new HashSet();
        for (Iterator it=p1.keySet().iterator(); it.hasNext(); ) {
            String mid = (String)it.next();
            if (p2.get(mid) != null) si.add(mid);
        }
        int n = si.size();
        if (n == 0) return 0.0;
        double sum1=0.0, sum2=0.0;
        double sum1sq=0.0, sum2sq=0.0;
        double psum=0.0;
        for (Iterator it=si.iterator(); it.hasNext(); ) {
            String mid = (String)it.next();
            double r1 = ((Double)p1.get(mid)).doubleValue();
            double r2 = ((Double)p2.get(mid)).doubleValue();
            sum1 += r1;
            sum2 += r2;
            sum1sq += r1*r1;
            sum2sq += r2*r2;
            psum += r1*r2;
        }
        double num = psum-(sum1*sum2)/n;
        double den=Math.sqrt((sum1sq-sum1*sum1/n)*(sum2sq-sum2*sum2/n));
        if (den == 0.0) return 0.0;
        double result = (num/den+1)/2.0;
        return result;
    }

    private double cosineSimilarity(String person, String other) {
        HashMap p1 = (HashMap)this.data.get(person);
        HashMap p2 = (HashMap)this.data.get(other);
        HashSet si = new HashSet();
        for (Iterator it=p1.keySet().iterator(); it.hasNext(); ) {
            String mid = (String)it.next();
            if (p2.get(mid) != null) si.add(mid);
        }
        int n = si.size();
        if (n == 0) return 0.0;
        double sum1sq=0.0, sum2sq=0.0;
        double psum=0.0;
        for (Iterator it=si.iterator(); it.hasNext(); ) {
            String mid = (String)it.next();
            double r1 = ((Double)p1.get(mid)).doubleValue();
            double r2 = ((Double)p2.get(mid)).doubleValue();
            sum1sq += r1*r1;
            sum2sq += r2*r2;
            psum += r1*r2;
        }
        double den=Math.sqrt(sum1sq)*Math.sqrt(sum2sq);
        if (den == 0.0) return 0.0;
        return psum/den;
    }

    private double similarity(String person, String other, int method) {
        // 0: Eucledian, 1: Pearson, 2: cosine
        switch (method) {
            case 0: return eucledianSimilarity(person, other);
            case 1: return pearsonSimilarity(person, other);
            case 2:
            default: return cosineSimilarity(person, other);
        }
    }

    /*public double getRecommendations(String person, String movie, int method) {
        Set names = this.data.keySet();
        HashMap plocal = (HashMap)this.data.get(person);
        for (Iterator it=names.iterator(); it.hasNext(); ) {
            String other = (String)it.next();
            if (other.equals(person)) continue;
            double sim = similarity(person, other, method);
            if (sim <= 0) continue;
            HashMap local = (HashMap)this.data.get(other);
            Object obj = local.get(movie);
            if (obj == null) continue;
            double rating = (Double)obj;
        }

    }*/

    public void getRecommendations(String person, int method) {
        HashMap totals = new HashMap();
        HashMap simSums = new HashMap();
        Set names = this.data.keySet();
        HashMap plocal = (HashMap)this.data.get(person);
        for (Iterator it=names.iterator(); it.hasNext(); ) {
            String other = (String)it.next();
            if (other.equals(person)) continue;
            double sim = similarity(person, other, method);
            //System.err.println(person+" vs "+other+", sim="+sim);

            if (sim <= 0) continue;
            HashMap local = (HashMap)this.data.get(other);
            Set midset = local.keySet();
            for (Iterator it2=midset.iterator(); it2.hasNext(); ) {
                String mid = (String)it2.next();
                double score = ((Double)local.get(mid)).doubleValue();
                if (plocal.get(mid) != null) {
                    Object obj = totals.get(mid);
                    Object obj2 = simSums.get(mid);
                    double total = 0.0;
                    double sim2 = 0.0;
                    double simscore = sim*score;
                    if (obj != null) {
                        total = ((Double)obj).doubleValue();
                        total += simscore;
                        sim2 = ((Double)obj2).doubleValue();
                        sim2 += sim;
                    }
                    else {
                        total = simscore;
                        sim2 = sim;
                    }
                    totals.put(mid, total);
                    simSums.put(mid, sim2);
                }
            } // for
        } // for
        //System.err.println("totals size: "+totals.keySet().size());
        HashMap recomm = new HashMap();
        for (Iterator it=totals.keySet().iterator(); it.hasNext(); ) {
            String mid = (String)it.next();
            double total = (Double)totals.get(mid);
            double simSum = (Double)simSums.get(mid);
            //System.err.println(mid+": "+total+"/"+simSum);
            double score = total/simSum;
            recomm.put(mid, score);
            System.out.println(mid+" recommendation score = "+score);
        }
    }
}



















