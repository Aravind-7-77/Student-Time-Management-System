package auth;

import javax.swing.*;
import java.awt.*;
import data.DataStore;
import model.User;
import DAOLOGIC.UserDAO;
import dashboard.Dashboard;

public class LoginFrame extends JFrame {

    public LoginFrame() {

        setTitle("Student Login");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Use BorderLayout so background fills window
        setLayout(new BorderLayout());

        // Background Panel
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new GridBagLayout());

        // Login Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(480, 540));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Welcome Label (Top - Outside Card)
        JLabel welcomeLabel =
                new JLabel("Welcome To Student Time Management System",
                        SwingConstants.CENTER);

        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Student Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.ORANGE);

        // Labels
        JLabel usernameLabel = new JLabel("Email / Username");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Inputs
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        username.setPreferredSize(new Dimension(280, 38));
        password.setPreferredSize(new Dimension(280, 38));
        username.setOpaque(false);
        password.setOpaque(false);
        username.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        password.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        username.setForeground(Color.WHITE); 
        password.setForeground(Color.WHITE);  

        // Buttons
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Create Account");

        styleButton(loginBtn, new Color(0, 123, 255));
        styleButton(registerBtn, new Color(40, 167, 69));

        /* ================== ADD TO CARD ================== */

        gbc.gridy = 0;
        card.add(title, gbc);

        gbc.gridy++;
        card.add(usernameLabel, gbc);

        gbc.gridy++;
        card.add(username, gbc);

        gbc.gridy++;
        card.add(passwordLabel, gbc);

        gbc.gridy++;
        card.add(password, gbc);
        gbc.gridy++;

JLabel forgotLabel = new JLabel("Forgot Password?");
forgotLabel.setForeground(Color.CYAN);
forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

card.add(forgotLabel, gbc);


        gbc.gridy++;
        card.add(loginBtn, gbc);

        gbc.gridy++;
        card.add(registerBtn, gbc);

        /* ================== MAIN PANEL LAYOUT ================== */

        // Welcome (Top - Center)
        GridBagConstraints welcomeGbc = new GridBagConstraints();
        welcomeGbc.gridx = 0;
        welcomeGbc.gridy = 0;
        welcomeGbc.gridwidth = 2;
        welcomeGbc.weightx = 1.0;
        welcomeGbc.insets = new Insets(40, 10, 20, 10);
        welcomeGbc.anchor = GridBagConstraints.CENTER;

        mainPanel.add(welcomeLabel, welcomeGbc);

        // Empty Space (Left Column)
        GridBagConstraints space = new GridBagConstraints();
        space.gridx = 0;
        space.gridy = 1;
        space.weightx = 1.0;
        space.fill = GridBagConstraints.HORIZONTAL;

        mainPanel.add(new JLabel(), space);

        // Card (Right Column)
        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.gridx = 1;
        cardGbc.gridy = 1;
        cardGbc.weightx = 1.0;
        cardGbc.anchor = GridBagConstraints.EAST;
        cardGbc.insets = new Insets(0, 0, 0, 80);

        mainPanel.add(card, cardGbc);

        // Add background panel
        add(mainPanel, BorderLayout.CENTER);

        /* ================== ACTIONS ================== */

        // Login Action
loginBtn.addActionListener(e -> {

    String input = username.getText().trim();
    String pass = new String(password.getPassword());

    if (input.isEmpty() || pass.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Please enter Username/Email and Password",
                "Empty Fields",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    UserDAO dao = new UserDAO();
    User user = dao.loginUser(input, pass);

    if (user != null) {

        // ✅ STORE USER PROPERLY
        DataStore.setCurrentUser(user);

        new Dashboard();
        dispose();

    } else {

        JOptionPane.showMessageDialog(
                this,
                "Invalid Username or Password",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
        );
    }
});

        forgotLabel.addMouseListener(new java.awt.event.MouseAdapter() {

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        new ForgotPasswordFrame();
        dispose();
    }
});


        // Register Action
        registerBtn.addActionListener(e -> {
            new RegisterFrame();
            dispose();
        });

        setVisible(true);
    }

    /* ================== BUTTON STYLE ================== */

    private void styleButton(JButton btn, Color bg) {

        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(280, 42));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    /* ================== BACKGROUND PANEL ================== */

    class BackgroundPanel extends JPanel {

        private Image bg;

        public BackgroundPanel() {

            java.net.URL url =
                    LoginFrame.class.getClassLoader()
                            .getResource("resources/loginbg.png");

            if (url != null) {
                bg = new ImageIcon(url).getImage();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            if (bg != null) {

                g.drawImage(bg, 0, 0,
                        getWidth(), getHeight(), this);

                // Dark overlay
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0,
                        getWidth(), getHeight());
            }
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
