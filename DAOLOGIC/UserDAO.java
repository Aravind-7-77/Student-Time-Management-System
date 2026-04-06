package DAOLOGIC;

import java.sql.*;
import model.User;
import DAOLOGIC.DBConnection;

public class UserDAO {

    // ================= REGISTER =================
    public boolean registerUser(User user) {

        String sql = "INSERT INTO users " +
                "(full_name, email, username, password, security_question, security_answer) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, user.getFullName());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getUsername());
            pst.setString(4, user.getPassword());
            pst.setString(5, user.getSecurityQuestion());
            pst.setString(6, user.getSecurityAnswer());

            int rows = pst.executeUpdate();
            return rows > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            String message = e.getMessage();
            if (message.contains("email")) {
                System.out.println("Duplicate email: " + user.getEmail());
            } else if (message.contains("username")) {
                System.out.println("Duplicate username: " + user.getUsername());
            }
            return false;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                return false;
            }
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= LOGIN =================
    public User loginUser(String input, String password) {

        String sql = "SELECT * FROM users " +
                "WHERE (email=? OR username=?) AND password=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, input.trim());
            pst.setString(2, input.trim());
            pst.setString(3, password);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
public boolean deleteUser(int userId) {
    String query = "DELETE FROM users WHERE id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        pstmt.setInt(1, userId);
        return pstmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
    // ================= GET USER BY ID =================
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= GET USER BY USERNAME =================
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, username.trim());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= RESET PASSWORD =================
    public boolean resetPassword(String username,
                                 String question,
                                 String answer,
                                 String newPassword) {

        String checkSql = "SELECT id FROM users WHERE username=? AND security_question=? AND security_answer=?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement checkStmt = con.prepareStatement(checkSql)) {

            checkStmt.setString(1, username.trim());
            checkStmt.setString(2, question.trim());
            checkStmt.setString(3, answer.trim());

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateSql = "UPDATE users SET password=? WHERE username=?";
                try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newPassword);
                    updateStmt.setString(2, username.trim());
                    
                    int rows = updateStmt.executeUpdate();
                    return rows > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= UPDATE PASSWORD =================
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, newPassword);
            pst.setInt(2, userId);

            int rows = pst.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= CHECK IF USERNAME EXISTS =================
    public boolean isUsernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, username.trim());
            ResultSet rs = pst.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= CHECK IF EMAIL EXISTS =================
    public boolean isEmailExists(String email) {
        String sql = "SELECT id FROM users WHERE email=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, email.trim());
            ResultSet rs = pst.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= UPDATE USER PROFILE =================
    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET full_name=?, email=?, security_question=?, security_answer=? WHERE id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, user.getFullName());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getSecurityQuestion());
            pst.setString(4, user.getSecurityAnswer());
            pst.setInt(5, user.getId());

            int rows = pst.executeUpdate();
            return rows > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Duplicate email: " + user.getEmail());
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // ================= HELPER METHOD: Extract User from ResultSet =================
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("security_question"),
            rs.getString("security_answer"),
            rs.getTimestamp("created_at")  // Add this line to fetch createdAt
        );
    }
}