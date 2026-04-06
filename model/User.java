package model;

import java.sql.Timestamp;

public class User {

    private int id;
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String securityQuestion;
    private String securityAnswer;
    private Timestamp createdAt;  // Add this field

    // ✅ Constructor for REGISTER (no id yet)
    public User(String fullName, String email, String username,
                String password, String securityQuestion,
                String securityAnswer) {

        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // ✅ Constructor for LOGIN (with id)
    public User(int id, String fullName, String email, String username,
                String password, String securityQuestion,
                String securityAnswer) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }

    // ✅ New constructor with createdAt (for complete user data)
    public User(int id, String fullName, String email, String username,
                String password, String securityQuestion,
                String securityAnswer, Timestamp createdAt) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}