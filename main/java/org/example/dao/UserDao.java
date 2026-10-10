
package org.example.dao;

import org.example.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class UserDao {

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public boolean register(String username, String password, String role) {

        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        String hashedPassword = hashPassword(password);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, role);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println("That username already exists.");
            } else {
                System.out.println("Registration error: "
                        + e.getMessage());
            }

            return false;
        }
    }

    public User login(String username, String password) {

        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    String storedPassword =
                            result.getString("password");

                    if (verifyPassword(password, storedPassword)) {
                        return new User(
                                result.getInt("id"),
                                result.getString("username"),
                                "",
                                result.getString("roles"),
                                result.getString("status")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] hash = deriveHash(
                password.toCharArray(), salt, ITERATIONS);

        return "PBKDF2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    private boolean verifyPassword(
            String password, String storedPassword) {

        try {
            String[] parts = storedPassword.split("\\$");

            if (parts.length != 4 || !parts[0].equals("PBKDF2")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = deriveHash(
                    password.toCharArray(), salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] deriveHash(
            char[] password, byte[] salt, int iterations) {

        PBEKeySpec specification =
                new PBEKeySpec(password, salt, iterations, KEY_LENGTH);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256");

            return factory.generateSecret(specification).getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to hash password.", e);
        } finally {
            specification.clearPassword();
        }
    }
}
