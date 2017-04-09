import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MovieLens {
	private HashMap<String, HashMap<String, Double>> data;
	public static final int SIM_EUCLEDIAN=0;
	public static final int SIM_PEARSON=1;
	public static final int SIM_COSINE=2;
	
	public static void main(String[] args) throws Exception {
		String fname1="u1.base";
		String fname2="u1.test";
		MovieLens ml = new MovieLens(fname1);
		double rmse = ml.go(fname2, SIM_EUCLEDIAN);
		System.out.println("RMSE = "+rmse);
		//Pearson RMSE = 1.0193507514585867
		//Cosine RMSE = 1.0312167105867571
		//Eucledian RMSE = 1.0290671936728244
	}
	
	public double go(String fname, int simMethod) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fname));
		String line="";
		double sum=0.0;
		int count=0;
		while ((line=br.readLine()) != null) {
			//1	1	5	874965758
			String[] slist = line.split("\t");
			String uid = slist[0];
			String mid = slist[1];
			double ans = Integer.parseInt(slist[2]);
			double guess = getRecommendation(this.data, uid, mid, simMethod);
			sum += (ans-guess)*(ans-guess);
			count++;
			System.out.println("Processing question "+count+", guess="+guess);
		}
		br.close();
		return Math.sqrt(sum/count);
	}
	
	public double getRecommendation(HashMap<String, HashMap<String, Double>> prefs, 
			String uid, String mid, int simMethod) throws Exception {
		double total = 0.0; 
		double simSum = 0.0;
		for (String other: prefs.keySet()) {
			if (other.equals(uid)) continue;
			double sim = similarity(prefs, uid, other, simMethod);
			if (Double.isNaN(sim)) {
				//System.out.println("sim = NaN");
				continue;
			}
			if (sim <= 0) continue;
			Double d = prefs.get(uid).get(mid);
			if (d==null || d==0) {
				if (prefs.get(other) != null) {
					if (prefs.get(other).get(mid) != null) {
						total += prefs.get(other).get(mid)*sim;
						simSum += sim;
					}
				}
			}
		}
		//System.out.println("Total = "+total+", simSum = "+simSum);
		if (simSum == 0) return 3.0;
		return total/simSum;
	}
	
	public MovieLens(String fname) throws Exception {
		this.data = new HashMap<String, HashMap<String, Double>>(); 
		// load data here
		BufferedReader br = new BufferedReader(new FileReader(fname));
		String line="";
		while ((line=br.readLine()) != null) {
			//1	1	5	874965758
			String[] slist = line.split("\t");
			String uid = slist[0];
			String mid = slist[1];
			double r = Integer.parseInt(slist[2]);
			HashMap<String, Double> userdata = this.data.get(uid);
			if (userdata == null) {
				userdata = new HashMap<String, Double>();
				this.data.put(uid, userdata);
			}
			userdata.put(mid, r);
			
		}
		br.close();
	}
	
	public double similarity(HashMap<String, HashMap<String, Double>> prefs, 
			String person, String other, int simMethod) throws Exception {
		HashMap<String, Double> pt1 = prefs.get(person);
		HashMap<String, Double> pt2 = prefs.get(other);
		switch (simMethod) {
			case SIM_EUCLEDIAN: return eucledianSimilarity(pt1, pt2);
			case SIM_COSINE: return cosineSimilarity(pt1, pt2);
			default: return pearsonSimilarity(pt1, pt2);
		}
	}
	
	public double cosineSimilarity(HashMap<String, Double> pt1, HashMap<String, Double> pt2)
			throws Exception {
		Set<String> set1 = pt1.keySet();
		Set<String> set2 = pt2.keySet();
		Set<String> set0 = new HashSet<String>(set1);
		set0.retainAll(set2);
		double[] p1 = new double[set0.size()];
		double[] p2 = new double[set0.size()];
		int idx=0;
		for (String m: set0) {
			p1[idx] = pt1.get(m);
			p2[idx] = pt2.get(m);
			idx++;
		}
		return cosineSimilarity(p1, p2);
	}
	
	public double cosineSimilarity(double[] pt1, double[] pt2) throws Exception {
		if ((pt1==null) || (pt2==null) || (pt1.length!=pt2.length)) {
			throw new Exception("Null point or dimension mismatch.");
		}
		double sum1Sq=0.0;
		double sum2Sq=0.0;
		double pSum=0.0;
		for (int i=0; i<pt1.length; i++) {
			sum1Sq += pt1[i]*pt1[i];
			sum2Sq += pt2[i]*pt2[i];
			pSum += pt1[i]*pt2[i];
		}
		return pSum/(Math.sqrt(sum1Sq)*Math.sqrt(sum2Sq));
	}
	
	public double pearsonSimilarity(HashMap<String, Double> pt1, HashMap<String, Double> pt2)
			throws Exception {
		Set<String> set1 = pt1.keySet();
		Set<String> set2 = pt2.keySet();
		Set<String> set0 = new HashSet<String>(set1);
		set0.retainAll(set2);
		double[] p1 = new double[set0.size()];
		double[] p2 = new double[set0.size()];
		int idx=0;
		for (String m: set0) {
			p1[idx] = pt1.get(m);
			p2[idx] = pt2.get(m);
			idx++;
		}
		return pearsonSimilarity(p1, p2);
	}

	public double pearsonSimilarity(double[] pt1, double[] pt2) throws Exception {
		if ((pt1==null) || (pt2==null) || (pt1.length!=pt2.length)) {
			throw new Exception("Null point or dimension mismatch.");
		}
		double sum1=0.0;
		double sum2=0.0;
		double sum1Sq=0.0;
		double sum2Sq=0.0;
		double pSum=0.0;
		for (int i=0; i<pt1.length; i++) {
			sum1 += pt1[i];
			sum2 += pt2[i];
			sum1Sq += pt1[i]*pt1[i];
			sum2Sq += pt2[i]*pt2[i];
			pSum += pt1[i]*pt2[i];
		}
		double num=pSum-sum1*sum2/pt1.length;
		double den=Math.sqrt((sum1Sq-sum1*sum1/pt1.length)*(sum2Sq-sum2*sum2/pt1.length));
		if (den==0) return 0;
		
		return num/den;
	}
	
	public double eucledianSimilarity(HashMap<String, Double> pt1, HashMap<String, Double> pt2)
			throws Exception {
		Set<String> set1 = pt1.keySet();
		Set<String> set2 = pt2.keySet();
		Set<String> set0 = new HashSet<String>(set1);
		set0.retainAll(set2);
		double[] p1 = new double[set0.size()];
		double[] p2 = new double[set0.size()];
		int idx=0;
		for (String m: set0) {
			p1[idx] = pt1.get(m);
			p2[idx] = pt2.get(m);
			idx++;
		}
		return eucledianSimilarity(p1, p2);
	}
	
	public double eucledianSimilarity(double[] pt1, double[] pt2) throws Exception {
		if ((pt1==null) || (pt2==null) || (pt1.length!=pt2.length)) {
			throw new Exception("Null point or dimension mismatch.");
		}
		double sum=0.0;
		for (int i=0; i<pt1.length; i++)
			sum += (pt1[i]-pt2[i])*(pt1[i]-pt2[i]);
		//double dist = Math.sqrt(sum); // Math.pow(sum, 0.5);
		double dist = sum;
		return 1.0/(dist+1);
	}


}
