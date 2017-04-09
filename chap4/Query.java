package chap4;

import java.sql.*;

public class Query {
    private String[] param;
    
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

    public static void main(String[] args) {
        Query q = new Query(args);
        q.doQuery();
    }

    public Query(String[] args) {
        this.param = args;
    }
    
    public void doQuery() {
        for (int i=0; i<param.length; i++) {
            processTerm(param[i]);
        }
    }

    private void processTerm(String term) {
        Connection conn = this.getConnection();
        try {
            Statement stmt = conn.createStatement();
            // part 1: find wordid
            String sqlstr = "select rowid from wordlist where word='"+term+"'";
            ResultSet rs = stmt.executeQuery(sqlstr);
            int wordid = -1;
            if (rs.next()) wordid = rs.getInt(1);
            rs.close();
            if (wordid == -1) return;
            // part 2: find urlid and wordlocation
            sqlstr = "select urlid, location from wordlocation where wordid="+wordid;
            rs = stmt.executeQuery(sqlstr);
            while (rs.next()) {
                int urlid = rs.getInt("urlid");
                int wordlocation = rs.getInt("location");
                System.out.println("("+urlid+", "+wordid+","+wordlocation+")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
