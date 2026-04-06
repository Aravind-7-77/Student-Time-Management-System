package auth;

import javax.swing.*;
import java.awt.*;
import model.User;
import DAOLOGIC.UserDAO;

public class RegisterFrame extends JFrame {

    private static final Dimension FIELD_SIZE =
            new Dimension(320, 40);

    // ================= BACKGROUND PANEL =================
    class BackgroundPanel extends JPanel {

        private Image backgroundImage;

        public BackgroundPanel(String path) {

            java.net.URL url = getClass().getResource(path);

            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                System.out.println("Image not found: " + path);
            }

            setLayout(new GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0,
                        getWidth(), getHeight(), this);
            }
        }
    }

    // ================= CONSTRUCTOR =================
    public RegisterFrame() {

        setTitle("Create Account");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 🔹 Load background from images folder
        JPanel main = new BackgroundPanel("/resources/download_1.jpg");

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(700, 650));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(25, 40, 25, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        String[] questions = {
                "What is your pet name?",
                "What is your favorite food?",
                "What is your birth place?",
                "What is your school name?"
        };

        JComboBox<String> securityQuestionBox =
                new JComboBox<>(questions);

        JTextField securityAnswerField = new JTextField();

        styleField(fullNameField);
        styleField(emailField);
        styleField(usernameField);
        styleField(passwordField);
        styleField(confirmPasswordField);
        styleField(securityAnswerField);

        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");

        styleButton(registerBtn, new Color(40, 167, 69));
        styleButton(backBtn, new Color(0, 123, 255));

        // ================= ADD COMPONENTS =================

        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridy++;
        card.add(createLabel("Full Name"), gbc);
        gbc.gridx = 1;
        card.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Email"), gbc);
        gbc.gridx = 1;
        card.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Username"), gbc);
        gbc.gridx = 1;
        card.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Password"), gbc);
        gbc.gridx = 1;
        card.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Confirm Password"), gbc);
        gbc.gridx = 1;
        card.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Security Question"), gbc);
        gbc.gridx = 1;
        card.add(securityQuestionBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Security Answer"), gbc);
        gbc.gridx = 1;
        card.add(securityAnswerField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(registerBtn, gbc);

        gbc.gridy++;
        card.add(backBtn, gbc);

        main.add(card);
        setContentPane(main);

        // ================= REGISTER LOGIC =================

        registerBtn.addActionListener(e -> {

            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword =
                    new String(confirmPasswordField.getPassword());
            String question =
                    securityQuestionBox.getSelectedItem().toString();
            String answer = securityAnswerField.getText().trim();

            if (fullName.isEmpty() || email.isEmpty() || username.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()
                    || answer.isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "All fields are required!");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match!");
                return;
            }

            User user = new User(fullName, email, username,
                    password, question, answer);

            UserDAO dao = new UserDAO();
            boolean success = dao.registerUser(user);

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Registration Successful!");
                new LoginFrame();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Email or Username already exists!");
            }
        });

        backBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        setVisible(true);
    }

    // ================= HELPERS =================

    private void styleField(JTextField field) {
        field.setPreferredSize(FIELD_SIZE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lbl;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(320, 45));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}