package chap4;

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class GetHTML {
    private String urlstr;
    private Connection _conn = null;
    private ArrayList urllist;

    private Connection getConnection() {
        if (_conn != null) return _conn;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            _conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/");
            return _conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int saveURL(String url) {
        Connection conn = this.getConnection();
        int rowid = -1;
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "select * from urllist where url='"+url+"'";
            ResultSet rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                return rs.getInt("rowid");
            }
            sqlstr = "select count(rowid) from urllist";
            rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                rowid = rs.getInt(1);
            }
            sqlstr = "insert into urllist (rowid, url) values ("+rowid+", '"+url+"')";
            stmt.execute(sqlstr);
            return rowid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int saveWord(String word) {
        Connection conn = this.getConnection();
        int rowid = -1;
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "select * from wordlist where word='"+word+"'";
            ResultSet rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                return rs.getInt("rowid");
            }
            sqlstr = "select count(rowid) from wordlist";
            rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                rowid = rs.getInt(1);
            }
            sqlstr = "insert into wordlist (rowid, word) values ("+rowid+", '"+word+"')";
            stmt.execute(sqlstr);
            return rowid;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void saveWordLocation(int urlid, int wordid, int wordlocation) {
        Connection conn = this.getConnection();
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "insert into wordlocation (urlid, wordid, location) values ("+
                    urlid+", "+wordid+", "+wordlocation+")";
            stmt.execute(sqlstr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String url = "http://jhyeh.csie.au.edu.tw/";
        GetHTML gh = new GetHTML(url);
        gh.go();
    }

    public void go() {
        //GetHTML gh = new GetHTML(args[0]);
        String fname = doFetch();
        // save url to table
        int urlid = saveURL(urlstr);
        try {
            FileReader in = new FileReader(fname);
            Html2Text parser = new Html2Text();
            parser.parse(in);
            in.close();
            urllist = parser.getURLs();
            for (Iterator it=urllist.iterator(); it.hasNext(); ) {
                System.out.println((String)it.next());
            }
            //System.out.println(parser.getText());
            StringTokenizer st = new StringTokenizer(parser.getText(), " ,.");
            int wordlocation = 0;
            while (st.hasMoreTokens()) {
                String word = st.nextToken();
                // save word to table
                int wordid = saveWord(word);
                // save wordlocation to table
                saveWordLocation(urlid, wordid, wordlocation);
                wordlocation++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GetHTML(String s) {
        this.urlstr = s;
        this.urllist = new ArrayList();
    }

    public String doFetch() {
        try {
            URL url = new URL(this.urlstr);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String fname = System.currentTimeMillis()+".txt";
            PrintWriter pw = new PrintWriter(new FileOutputStream(fname));
            String line = "";
            while ((line=br.readLine()) != null) {
                pw.println(line);
            }
            br.close();
            pw.close();
            return fname;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
