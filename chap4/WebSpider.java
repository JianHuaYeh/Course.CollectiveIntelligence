package chap4;

import java.net.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class WebSpider {
    private String urlstr;
    private int level;
    private int maxlevel;
    private Connection _conn = null;

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
            sqlstr = "insert into urllist (rowid, url, visited) values ("+rowid+", '"+url+"', 0)";
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
            sqlstr = sqlstr.replace("'", "\\'");
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

    private int saveLink(int fromid, int toid) {
        Connection conn = this.getConnection();
        int rowid = -1;
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "select * from link where fromid="+fromid+" and "+
                    "toid="+toid;
            ResultSet rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                return rs.getInt("rowid");
            }
            sqlstr = "select count(rowid) from link";
            rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                rowid = rs.getInt(1);
            }
            sqlstr = "insert into link (rowid, fromid, toid) values ("+
                    rowid+", "+fromid+", "+toid+")";
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

    private void saveLinkWords(int wordid, int linkid) {
        Connection conn = this.getConnection();
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "insert into linkwords (wordid, linkid) values ("+
                    wordid+", "+linkid+")";
            stmt.execute(sqlstr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String url = "http://www.sfw.tw/";
        WebSpider gh = new WebSpider(url, 1);
        gh.go();
    }

    private String transformURL(String urlstr1, String urlstr2) {
        String prefix = urlstr1.substring(0, urlstr1.lastIndexOf("/"))+"/";
        if (urlstr2.startsWith("#")) {
            // case 3
            return urlstr1+urlstr2;
        }
        else {
            if (urlstr2.indexOf("://") > 0) {
                return urlstr2;
            }
            else {
                return urlstr1+urlstr2;
            }
        }
    }

    private boolean hasVisited(String url) {
        Connection conn = this.getConnection();
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "select * from urllist where url='"+url+"' and visited=1";
            ResultSet rs = stmt.executeQuery(sqlstr);
            if (rs.next()) {
                //int i = rs.getInt("visited");
                //if (i == 1) return true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setVisited(int urlid) {
        Connection conn = this.getConnection();
        try {
            Statement stmt = conn.createStatement();
            String sqlstr = "update urllist set visited=1 where rowid="+urlid;
            stmt.execute(sqlstr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void go() {
        if (this.level > this.maxlevel) return;
        if (hasVisited(this.urlstr)) return;
        
        //GetHTML gh = new GetHTML(args[0]);
        String fname = doFetch();
        // save url to table
        int urlid = saveURL(urlstr);
        try {
            FileReader in = new FileReader(fname);
            Html2Text parser = new Html2Text();
            parser.parse(in);
            in.close();

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

            ArrayList urllist = parser.getURLs();
            ArrayList urltextlist = parser.getURLTexts();
            for (int i=0; i<urllist.size(); i++) {
                String urlstr2 = (String)urllist.get(i);
                String urltext = (String)urltextlist.get(i);
                urlstr2 = transformURL(urlstr, urlstr2);
                
                int urlid2 = saveURL(urlstr2);
                int linkid = saveLink(urlid, urlid2);
                StringTokenizer st2 = new StringTokenizer(urltext);
                while (st2.hasMoreTokens()) {
                    String word = st2.nextToken().trim();
                    int wordid = saveWord(word);
                    saveLinkWords(wordid, linkid);
                }
            }
            setVisited(urlid);
            for (Iterator it=urllist.iterator(); it.hasNext(); ) {
                String urlstr2 = (String)it.next();
                WebSpider ws = new WebSpider(urlstr2, this.level+1, this.maxlevel);
                ws.go();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebSpider(String s, int l) {
        this(s, l, 2);
    }

    public WebSpider(String s, int l, int m) {
        this.urlstr = s;
        this.level = l;
        this.maxlevel = m;
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
