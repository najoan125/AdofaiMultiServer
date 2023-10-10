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

    public static int insertNickName(String clientId, String nickName) {
        String SQL = "INSERT INTO user VALUES (?,?)";
        try {
            // 각각의 데이터를 실제로 넣어준다.
            PreparedStatement pstmt = conn.prepareStatement(SQL);

            // 쿼리문의 ?안에 각각의 데이터를 넣어준다.
            pstmt.setString(1, clientId);
            pstmt.setString(2, nickName);

            // 명령어를 수행한 결과 반환, 반환값: insert가 된 데이터의 개수
            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
