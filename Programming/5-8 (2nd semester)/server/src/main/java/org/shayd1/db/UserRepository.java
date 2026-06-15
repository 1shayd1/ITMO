package org.shayd1.db;


import org.shayd1.auth.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private final DatabaseManager db;
    private final PasswordHasher hasher;

    public UserRepository(DatabaseManager db, PasswordHasher hasher){
        this.db = db;
        this.hasher = hasher;
    }

    public boolean register(String username, String password) throws SQLException {
        String hashedPassword = hasher.hash(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();
            return true;
        } catch (SQLException e){
            if (e.getSQLState().equals("23505")) return false;
            throw e;
        }
    }

    public Long authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                long id = rs.getLong("id");
                String storedHash = rs.getString("password_hash");
                if (hasher.verify(password, storedHash)){
                    return id;
                }
            }
            return null;
        }
    }
}