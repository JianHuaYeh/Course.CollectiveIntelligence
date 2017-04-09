package chap2;

import java.util.*;
import java.io.*;
/**
 *
 * @author AU
 */
public class History {
  private HashMap data;

  public static void main(String[] args) {
      History his = new History();
      his.loadData(args[0]);
  }

  public History() {
      data = new HashMap();
  }

  public void loadData(String fname) {
      try {
          BufferedReader br = new BufferedReader(new FileReader(fname));
          String line="";
          String pname = "";
          HashMap localmap = new HashMap();
          while ((line=br.readLine()) != null) {
              if (line.startsWith("=")) {
                  if (!pname.equals("")) {
                      System.out.println("Put data: user="+pname+", movie count="+localmap.size());
                      data.put(pname, localmap);
                  }
                  pname = line.substring(1).trim();
                  localmap = new HashMap();
              }
              else {
                  // Lady in the Water @ 2.5
                  StringTokenizer st = new StringTokenizer(line, "@");
                  String movie = st.nextToken().trim();
                  Double rate = new Double(st.nextToken().trim());
                  localmap.put(movie, rate);
              }
          }
          if (!pname.equals("")) {
              System.out.println("Put data: user="+pname+", movie count="+localmap.size());
              data.put(pname, localmap);
          }
          br.close();
          System.out.println("Data loading complete. User count="+data.size());
      } catch (Exception e) {
            System.out.println(e);
      }
  }

  public double eucledianSimilarity(double[] v1, double[] v2) {
      if (v1.length != v2.length)
          return -1.0;
      double sum = 0.0;
      for (int i=0; i<v1.length; i++)
          sum += (v1[i]-v2[i])*(v1[i]-v2[i]);
      return 1/(Math.sqrt(sum)+1.0);
  }
}
