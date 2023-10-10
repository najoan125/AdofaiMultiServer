package com.hyfata.adofai.server.util.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDB {
    static Connection conn = DataBaseUtil.getConnection();

    public static String getUserNickName(String clientId) {
        String SQL = "select nickname from user where steamid = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(SQL);
            ps.setString(1, clientId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                return rs.getString(1);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
