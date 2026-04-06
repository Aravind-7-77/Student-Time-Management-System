package auth;

import javax.swing.*;
import java.awt.*;
import DAOLOGIC.UserDAO;

public class ForgotPasswordFrame extends JFrame {

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
    public ForgotPasswordFrame() {

        setTitle("Reset Password");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 🔹 Background Image Panel
        JPanel main = new BackgroundPanel("/resources/download_1.jpg");

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 255, 255, 230)); // Transparent white
        card.setPreferredSize(new Dimension(700, 600));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Reset Your Password", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JTextField usernameField = new JTextField();
        JComboBox<String> questionBox = new JComboBox<>(new String[]{
                "What is your pet name?",
                "What is your favorite food?",
                "What is your birth place?",
                "What is your school name?"
        });
        JTextField answerField = new JTextField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        styleField(usernameField);
        styleField(answerField);
        styleField(newPassField);
        styleField(confirmPassField);

        JButton resetBtn = new JButton("Reset Password");
        styleButton(resetBtn);

        // ===== ADD COMPONENTS =====

        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        card.add(createLabel("Username"), gbc);
        gbc.gridx = 1;
        card.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Security Question"), gbc);
        gbc.gridx = 1;
        card.add(questionBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Answer"), gbc);
        gbc.gridx = 1;
        card.add(answerField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("New Password"), gbc);
        gbc.gridx = 1;
        card.add(newPassField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        card.add(createLabel("Confirm Password"), gbc);
        gbc.gridx = 1;
        card.add(confirmPassField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 12, 12, 12);
        card.add(resetBtn, gbc);

        main.add(card);
        setContentPane(main);

        // ===== RESET LOGIC =====

        resetBtn.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String question = (String) questionBox.getSelectedItem();
            String answer = answerField.getText().trim();
            String newPassword = new String(newPassField.getPassword());
            String confirmPassword = new String(confirmPassField.getPassword());

            if (username.isEmpty() || answer.isEmpty()
                    || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showMsg("All fields are required!");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showMsg("Passwords do not match!");
                return;
            }

            UserDAO dao = new UserDAO();

            boolean success = dao.resetPassword(
                    username,
                    question,
                    answer,
                    newPassword
            );

            if (success) {
                showMsg("Password reset successful ✅");
                new LoginFrame();
                dispose();
            } else {
                showMsg("Invalid username or security answer ❌");
            }
        });

        setVisible(true);
    }

    // ================= HELPER METHODS =================

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

    private void styleButton(JButton btn) {
        btn.setBackground(new Color(0, 123, 255));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(320, 45));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void showMsg(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }
}