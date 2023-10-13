package com.hyfata.adofai.server.util.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDB {
    static Connection conn = DataBaseUtil.getConnection();

    public static String getUserNickName(String clientId) throws SQLException {
        String SQL = "select nickname from user where steamid = ?";
        PreparedStatement ps = conn.prepareStatement(SQL);
        ps.setString(1, clientId);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    public static int insertNickName(String clientId, String nickName) {
        String SQL = "INSERT INTO user VALUES (?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(SQL);
            ps.setString(1, clientId);
            ps.setString(2, nickName);
            return ps.executeUpdate();
        } catch (Exception e) {
            return -1;
        }
    }

    public static int updateNickName(String clientId, String nickName) {
        String SQL = "update user set nickname = ? where steamid = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(SQL);
            ps.setString(1, nickName);
            ps.setString(2, clientId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return -1;
        }
    }

    public static boolean isNickNameExist(String nickName) throws SQLException {
        String SQL = "SELECT nickname FROM user WHERE nickname = ?";
        PreparedStatement ps = conn.prepareStatement(SQL);
        ps.setString(1,nickName);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public static int deleteUser(String clientId) {
        String SQL = "delete from user where steamid = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(SQL);
            ps.setString(1,clientId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return -1;
        }
    }
}
