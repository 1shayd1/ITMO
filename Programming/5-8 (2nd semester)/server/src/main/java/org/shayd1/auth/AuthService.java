package org.shayd1.auth;

import org.shayd1.db.UserRepository;

import java.sql.SQLException;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String register(String username, String password) {
        try {
            boolean success = userRepository.register(username, password);
            return success ? "User '" + username + "' is registered!" : "Username's already exists!";
        } catch (SQLException e) {
            return "Registration error: " + e.getMessage();
        }
    }

    public Long login(String username, String password) throws SQLException {
            return userRepository.authenticate(username, password);
    }
}
