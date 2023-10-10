package com.hyfata.adofai.server.util.mysql;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseUtil {
    public static Connection getConnection() { //데이터베이스와 연결상태 관리
        try {
            String dbURL = "jdbc:mysql://localhost:3306/adofai";
            String dbID = "root";
            String dbPassword = "12341234";
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(dbURL, dbID, dbPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
