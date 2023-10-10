package com.hyfata.adofai.server.util.mysql;

import com.hyfata.json.JsonReader;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class DataBaseUtil {
    static JSONObject info = null;
    public static Connection getConnection() { //데이터베이스와 연결상태 관리
        try {
            if (info == null) {
                info = JsonReader.readFromInputStream(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResourceAsStream("db.json")));
            }
            String dbURL = "jdbc:mysql://"+info.getString("url");
            String dbID = info.getString("id");
            String dbPassword = info.getString("password");
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(dbURL, dbID, dbPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
