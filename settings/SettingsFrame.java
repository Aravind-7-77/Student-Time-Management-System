package settings;

import model.User;
import DAOLOGIC.UserDAO;
import data.DataStore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class SettingsFrame extends JFrame {

    private User currentUser;
    private Preferences prefs;
    
    // Profile Components
    private JTextField fullNameField, emailField, securityAnswerField;
    private JComboBox<String> securityQuestionCombo;
    
    // Alert Components
    private JSpinner highHSpinner, highMSpinner, mediumDSpinner, mediumHSpinner, lowDSpinner, lowHSpinner;
    private JCheckBox enableCustomAlerts;
    private JLabel[] previewLabels = new JLabel[3];
    
    // Timer Components
    private JSpinner shortFocusSpinner, shortBreakSpinner, longFocusSpinner, longBreakSpinner;
    
    public SettingsFrame() {
        currentUser = DataStore.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "No user logged in!");
            dispose();
            return;
        }
        
        prefs = Preferences.userNodeForPackage(SettingsFrame.class);
        
        setTitle("⚙️ Settings - " + currentUser.getUsername());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Main container with sky blue background
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(135, 206, 250)); // Sky Blue
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        JLabel header = new JLabel(" SETTINGS");
        header.setFont(new Font("Segoe UI", Font.BOLD, 40));
        header.setForeground(new Color(20, 60, 90));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subHeader = new JLabel("Customize your application preferences");
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeader.setForeground(new Color(50, 80, 110));
        subHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(header);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subHeader);
        headerPanel.add(Box.createVerticalStrut(20));
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed Pane with sky blue theme
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabs.setBackground(new Color(135, 206, 250));
        tabs.setForeground(new Color(20, 60, 90));
        
        tabs.addTab(" PROFILE", createProfilePanel());
        tabs.addTab(" ALERTS", createAlertsPanel());
        tabs.addTab("  FOCUS TIMER", createTimerPanel());
        
        mainContainer.add(tabs, BorderLayout.CENTER);
        
        // Bottom Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btnPanel.setOpaque(false);
        
        JButton saveBtn = createStyledButton(" SAVE SETTINGS", new Color(46, 204, 113));
        JButton cancelBtn = createStyledButton("✖ CLOSE", new Color(231, 76, 60));
        
        saveBtn.addActionListener(e -> saveAll());
        cancelBtn.addActionListener(e -> dispose());
        
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        mainContainer.add(btnPanel, BorderLayout.SOUTH);
        
        add(mainContainer);
        loadData();
        setVisible(true);
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 15, 12, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Avatar
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel avatar = new JLabel("", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.PLAIN, 60));
        avatar.setForeground(new Color(70, 130, 200));
        panel.add(avatar, gbc);
        
        gbc.gridwidth = 1;
        
        // Username (read-only)
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(createLabel("Username:"), gbc);
        gbc.gridx = 1;
        JLabel usernameLabel = new JLabel(currentUser.getUsername());
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        usernameLabel.setForeground(new Color(70, 130, 200));
        panel.add(usernameLabel, gbc);
        
        // Full Name
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(createLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        fullNameField = createTextField();
        panel.add(fullNameField, gbc);
        
        // Email
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = createTextField();
        panel.add(emailField, gbc);
        
        // Security Question
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(createLabel("Security Question:"), gbc);
        gbc.gridx = 1;
        securityQuestionCombo = new JComboBox<>(new String[]{
            " What is your pet name?", 
            " What is your favorite food?",
            " What is your birth place?", 
            " What is your school name?"
        });
        styleCombo(securityQuestionCombo);
        panel.add(securityQuestionCombo, gbc);
        
        // Security Answer
        gbc.gridy = 5;
        gbc.gridx = 0;
        panel.add(createLabel("Security Answer:"), gbc);
        gbc.gridx = 1;
        securityAnswerField = createTextField();
        panel.add(securityAnswerField, gbc);
        
        // Buttons Panel (Update + Change Password)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        
        JButton updateBtn = createSmallButton(" UPDATE PROFILE", new Color(52, 152, 219));
        JButton pwdBtn = createSmallButton(" CHANGE PASSWORD", new Color(155, 89, 182));
        
        updateBtn.addActionListener(e -> updateProfile());
        pwdBtn.addActionListener(e -> changePassword());
        
        buttonPanel.add(updateBtn);
        buttonPanel.add(pwdBtn);
        
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        // Separator
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(200, 200, 200));
        panel.add(separator, gbc);
        
        // Delete Account Section
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel deletePanel = new JPanel();
        deletePanel.setLayout(new BoxLayout(deletePanel, BoxLayout.Y_AXIS));
        deletePanel.setOpaque(false);
        
        JLabel deleteWarning = new JLabel("⚠️ DANGER ZONE ⚠️");
        deleteWarning.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteWarning.setForeground(new Color(231, 76, 60));
        deleteWarning.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel deleteDesc = new JLabel("Once you delete your account, all your data will be permanently lost.");
        deleteDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deleteDesc.setForeground(new Color(150, 150, 150));
        deleteDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton deleteAccountBtn = createSmallButton("🗑 DELETE ACCOUNT", new Color(231, 76, 60));
        deleteAccountBtn.setBackground(new Color(231, 76, 60));
        deleteAccountBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteAccountBtn.addActionListener(e -> confirmDeleteAccount());
        
        deletePanel.add(deleteWarning);
        deletePanel.add(Box.createVerticalStrut(5));
        deletePanel.add(deleteDesc);
        deletePanel.add(Box.createVerticalStrut(10));
        deletePanel.add(deleteAccountBtn);
        
        panel.add(deletePanel, gbc);
        
        return panel;
    }
    
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        
        // Enable Custom Alerts
        enableCustomAlerts = new JCheckBox(" Enable Custom Alert Timings");
        enableCustomAlerts.setFont(new Font("Segoe UI", Font.BOLD, 16));
        enableCustomAlerts.setForeground(new Color(20, 60, 90));
        enableCustomAlerts.setBackground(Color.WHITE);
        enableCustomAlerts.addActionListener(e -> toggleAlerts());
        panel.add(enableCustomAlerts, gbc);
        
        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(235, 248, 255));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 200), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        JLabel info1 = new JLabel(" Set custom alert times for different priority levels");
        JLabel info2 = new JLabel("   • High Priority: Alert before due date/time");
        JLabel info3 = new JLabel("   • Medium Priority: Alert days before due date");
        JLabel info4 = new JLabel("   • Low Priority: Alert days before due date");
        
        info1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info1.setForeground(new Color(70, 130, 200));
        info2.setForeground(new Color(80, 80, 100));
        info3.setForeground(new Color(80, 80, 100));
        info4.setForeground(new Color(80, 80, 100));
        
        infoPanel.add(info1);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(info2);
        infoPanel.add(info3);
        infoPanel.add(info4);
        
        gbc.gridy = 1;
        panel.add(infoPanel, gbc);
        
        // Priority Cards
        gbc.gridwidth = 1;
        
        // High Priority Card
        gbc.gridy = 2;
        addPriorityCard(panel, gbc, " HIGH PRIORITY", new Color(231, 76, 60), 
            highHSpinner = createSpinner(10, 1, 72), "hours",
            highMSpinner = createSpinner(0, 0, 59), "minutes",
            previewLabels[0] = new JLabel());
        
        // Medium Priority Card
        gbc.gridy = 3;
        addPriorityCard(panel, gbc, " MEDIUM PRIORITY", new Color(241, 196, 15),
            mediumDSpinner = createSpinner(2, 1, 30), "days",
            mediumHSpinner = createSpinner(0, 0, 23), "hours",
            previewLabels[1] = new JLabel());
        
        // Low Priority Card
        gbc.gridy = 4;
        addPriorityCard(panel, gbc, " LOW PRIORITY", new Color(46, 204, 113),
            lowDSpinner = createSpinner(7, 1, 60), "days",
            lowHSpinner = createSpinner(0, 0, 23), "hours",
            previewLabels[2] = new JLabel());
        
        // Reset Button
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton resetBtn = createSmallButton("↺ RESET TO DEFAULTS", new Color(155, 89, 182));
        resetBtn.addActionListener(e -> resetAlerts());
        panel.add(resetBtn, gbc);
        
        // Add listeners for preview updates
        highHSpinner.addChangeListener(e -> updatePreview(0, highHSpinner, highMSpinner, "hour", "hours"));
        highMSpinner.addChangeListener(e -> updatePreview(0, highHSpinner, highMSpinner, "hour", "hours"));
        mediumDSpinner.addChangeListener(e -> updatePreview(1, mediumDSpinner, mediumHSpinner, "day", "days"));
        mediumHSpinner.addChangeListener(e -> updatePreview(1, mediumDSpinner, mediumHSpinner, "day", "days"));
        lowDSpinner.addChangeListener(e -> updatePreview(2, lowDSpinner, lowHSpinner, "day", "days"));
        lowHSpinner.addChangeListener(e -> updatePreview(2, lowDSpinner, lowHSpinner, "day", "days"));
        
        return panel;
    }
    
    private JPanel createTimerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        
        // Short Session Card
        JPanel shortCard = new JPanel();
        shortCard.setBackground(new Color(235, 248, 255));
        shortCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        shortCard.setLayout(new GridBagLayout());
        
        GridBagConstraints cardGbc = new GridBagConstraints();
        cardGbc.insets = new Insets(10, 10, 10, 10);
        cardGbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Short Session Header
        cardGbc.gridx = 0;
        cardGbc.gridy = 0;
        cardGbc.gridwidth = 3;
        JLabel shortHeader = new JLabel(" SHORT SESSION (Pomodoro)");
        shortHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        shortHeader.setForeground(new Color(52, 152, 219));
        shortCard.add(shortHeader, cardGbc);
        
        cardGbc.gridwidth = 1;
        cardGbc.gridy = 1;
        cardGbc.gridx = 0;
        shortCard.add(createLabel("Focus Duration:"), cardGbc);
        cardGbc.gridx = 1;
        shortFocusSpinner = createSpinner(25, 1, 60);
        shortCard.add(shortFocusSpinner, cardGbc);
        cardGbc.gridx = 2;
        shortCard.add(createLabel("minutes"), cardGbc);
        
        cardGbc.gridy = 2;
        cardGbc.gridx = 0;
        shortCard.add(createLabel("Break Duration:"), cardGbc);
        cardGbc.gridx = 1;
        shortBreakSpinner = createSpinner(5, 1, 15);
        shortCard.add(shortBreakSpinner, cardGbc);
        cardGbc.gridx = 2;
        shortCard.add(createLabel("minutes"), cardGbc);
        
        gbc.gridy = 0;
        panel.add(shortCard, gbc);
        
        // Long Session Card
        JPanel longCard = new JPanel();
        longCard.setBackground(new Color(235, 248, 255));
        longCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        longCard.setLayout(new GridBagLayout());
        
        cardGbc.gridy = 0;
        cardGbc.gridx = 0;
        cardGbc.gridwidth = 3;
        JLabel longHeader = new JLabel("📚 LONG SESSION (Deep Work)");
        longHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        longHeader.setForeground(new Color(46, 204, 113));
        longCard.add(longHeader, cardGbc);
        
        cardGbc.gridwidth = 1;
        cardGbc.gridy = 1;
        cardGbc.gridx = 0;
        longCard.add(createLabel("Focus Duration:"), cardGbc);
        cardGbc.gridx = 1;
        longFocusSpinner = createSpinner(50, 25, 120);
        longCard.add(longFocusSpinner, cardGbc);
        cardGbc.gridx = 2;
        longCard.add(createLabel("minutes"), cardGbc);
        
        cardGbc.gridy = 2;
        cardGbc.gridx = 0;
        longCard.add(createLabel("Break Duration:"), cardGbc);
        cardGbc.gridx = 1;
        longBreakSpinner = createSpinner(10, 5, 30);
        longCard.add(longBreakSpinner, cardGbc);
        cardGbc.gridx = 2;
        longCard.add(createLabel("minutes"), cardGbc);
        
        gbc.gridy = 1;
        panel.add(longCard, gbc);
        
        // Info
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel info = new JLabel("💡 Short session = Pomodoro technique | Long session = Deep work mode");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        info.setForeground(new Color(100, 100, 120));
        info.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(info, gbc);
        
        return panel;
    }
    
    private void addPriorityCard(JPanel panel, GridBagConstraints gbc, String title, Color color, 
                                  JSpinner spinner1, String unit1, JSpinner spinner2, String unit2, JLabel preview) {
        JPanel card = new JPanel();
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 8, 5, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        card.add(titleLabel, c);
        
        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        card.add(createLabel("Alert:"), c);
        
        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        spinnerPanel.setOpaque(false);
        spinnerPanel.add(spinner1);
        spinnerPanel.add(createLabel(unit1));
        spinnerPanel.add(spinner2);
        spinnerPanel.add(createLabel(unit2));
        
        c.gridx = 1;
        card.add(spinnerPanel, c);
        
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 2;
        preview.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        preview.setForeground(new Color(100, 100, 120));
        card.add(preview, c);
        
        panel.add(card, gbc);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(50, 50, 70));
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(300, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(300, 38));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220), 1));
    }
    
    private JSpinner createSpinner(int val, int min, int max) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(val, min, max, 1));
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 14));
        spinner.setPreferredSize(new Dimension(70, 35));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        return spinner;
    }
    
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
    
    private JButton createSmallButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }
    
    private void updatePreview(int index, JSpinner s1, JSpinner s2, String unitSingular, String unitPlural) {
        int v1 = (Integer) s1.getValue();
        int v2 = (Integer) s2.getValue();
        String text = "🔔 Preview: Alert ";
        if (v1 > 0) text += v1 + " " + (v1 == 1 ? unitSingular : unitPlural);
        if (v2 > 0) text += (v1 > 0 ? " and " : "") + v2 + " hour" + (v2 > 1 ? "s" : "");
        text += (unitSingular.equals("hour") ? " before due time" : " before due date");
        previewLabels[index].setText(text);
    }
    
    private void toggleAlerts() {
        boolean enabled = enableCustomAlerts.isSelected();
        highHSpinner.setEnabled(enabled);
        highMSpinner.setEnabled(enabled);
        mediumDSpinner.setEnabled(enabled);
        mediumHSpinner.setEnabled(enabled);
        lowDSpinner.setEnabled(enabled);
        lowHSpinner.setEnabled(enabled);
    }
    
    private void resetAlerts() {
        highHSpinner.setValue(10);
        highMSpinner.setValue(0);
        mediumDSpinner.setValue(2);
        mediumHSpinner.setValue(0);
        lowDSpinner.setValue(7);
        lowHSpinner.setValue(0);
        updatePreview(0, highHSpinner, highMSpinner, "hour", "hours");
        updatePreview(1, mediumDSpinner, mediumHSpinner, "day", "days");
        updatePreview(2, lowDSpinner, lowHSpinner, "day", "days");
        JOptionPane.showMessageDialog(this, "✅ Alert settings reset to defaults!");
    }
    
    private void loadData() {
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        securityQuestionCombo.setSelectedItem(currentUser.getSecurityQuestion());
        securityAnswerField.setText(currentUser.getSecurityAnswer());
        
        boolean custom = prefs.getBoolean("alerts.custom.enabled", true);
        enableCustomAlerts.setSelected(custom);
        highHSpinner.setValue(prefs.getInt("alerts.high.hours", 10));
        highMSpinner.setValue(prefs.getInt("alerts.high.minutes", 0));
        mediumDSpinner.setValue(prefs.getInt("alerts.medium.days", 2));
        mediumHSpinner.setValue(prefs.getInt("alerts.medium.hours", 0));
        lowDSpinner.setValue(prefs.getInt("alerts.low.days", 7));
        lowHSpinner.setValue(prefs.getInt("alerts.low.hours", 0));
        
        shortFocusSpinner.setValue(prefs.getInt("timer.short.focus", 25));
        shortBreakSpinner.setValue(prefs.getInt("timer.short.break", 5));
        longFocusSpinner.setValue(prefs.getInt("timer.long.focus", 50));
        longBreakSpinner.setValue(prefs.getInt("timer.long.break", 10));
        
        toggleAlerts();
        updatePreview(0, highHSpinner, highMSpinner, "hour", "hours");
        updatePreview(1, mediumDSpinner, mediumHSpinner, "day", "days");
        updatePreview(2, lowDSpinner, lowHSpinner, "day", "days");
    }
    
    private void saveAll() {
        prefs.putBoolean("alerts.custom.enabled", enableCustomAlerts.isSelected());
        prefs.putInt("alerts.high.hours", (Integer) highHSpinner.getValue());
        prefs.putInt("alerts.high.minutes", (Integer) highMSpinner.getValue());
        prefs.putInt("alerts.medium.days", (Integer) mediumDSpinner.getValue());
        prefs.putInt("alerts.medium.hours", (Integer) mediumHSpinner.getValue());
        prefs.putInt("alerts.low.days", (Integer) lowDSpinner.getValue());
        prefs.putInt("alerts.low.hours", (Integer) lowHSpinner.getValue());
        
        prefs.putInt("timer.short.focus", (Integer) shortFocusSpinner.getValue());
        prefs.putInt("timer.short.break", (Integer) shortBreakSpinner.getValue());
        prefs.putInt("timer.long.focus", (Integer) longFocusSpinner.getValue());
        prefs.putInt("timer.long.break", (Integer) longBreakSpinner.getValue());
        
        JOptionPane.showMessageDialog(this, "✅ All settings saved successfully!");
        dispose();
    }
    
    private void updateProfile() {
        String name = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String answer = securityAnswerField.getText().trim();
        
        if (name.isEmpty() || email.isEmpty() || answer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }
        
        currentUser = new User(currentUser.getId(), name, email, 
            currentUser.getUsername(), currentUser.getPassword(),
            (String) securityQuestionCombo.getSelectedItem(), answer);
        
        if (new UserDAO().updateUserProfile(currentUser)) {
            DataStore.setCurrentUser(currentUser);
            JOptionPane.showMessageDialog(this, "✅ Profile updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to update profile!");
        }
    }
    
    private void changePassword() {
        JDialog dialog = new JDialog(this, "Change Password", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPasswordField oldPwd = new JPasswordField(15);
        JPasswordField newPwd = new JPasswordField(15);
        JPasswordField confirmPwd = new JPasswordField(15);
        
        stylePasswordField(oldPwd);
        stylePasswordField(newPwd);
        stylePasswordField(confirmPwd);
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        dialog.add(createLabel("Old Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(oldPwd, gbc);
        
        gbc.gridy = 1;
        gbc.gridx = 0;
        dialog.add(createLabel("New Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(newPwd, gbc);
        
        gbc.gridy = 2;
        gbc.gridx = 0;
        dialog.add(createLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(confirmPwd, gbc);
        
        JButton update = createSmallButton("UPDATE PASSWORD", new Color(46, 204, 113));
        JButton cancel = createSmallButton("CANCEL", new Color(231, 76, 60));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);
        btnPanel.add(update);
        btnPanel.add(cancel);
        
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);
        
        update.addActionListener(e -> {
            String old = new String(oldPwd.getPassword());
            String newP = new String(newPwd.getPassword());
            String confirm = new String(confirmPwd.getPassword());
            
            if (!newP.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match!");
            } else if (newP.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters!");
            } else if (!old.equals(currentUser.getPassword())) {
                JOptionPane.showMessageDialog(dialog, "Wrong old password!");
            } else if (new UserDAO().updatePassword(currentUser.getId(), newP)) {
                currentUser.setPassword(newP);
                JOptionPane.showMessageDialog(dialog, "✅ Password changed successfully!");
                dialog.dispose();
            }
        });
        
        cancel.addActionListener(e -> dialog.dispose());
        
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void confirmDeleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "⚠️ WARNING: This action is IRREVERSIBLE!\n\n" +
            "Deleting your account will permanently remove:\n" +
            "• Your profile information\n" +
            "• All your tasks and assignments\n" +
            "• All your study sessions and timer history\n" +
            "• All your preferences and settings\n\n" +
            "Are you absolutely sure you want to delete your account?",
            "Delete Account - Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Second confirmation with password
            JPasswordField passwordField = new JPasswordField(15);
            stylePasswordField(passwordField);
            
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(createLabel("Enter your password to confirm:"), gbc);
            gbc.gridx = 1;
            panel.add(passwordField, gbc);
            
            int result = JOptionPane.showConfirmDialog(this, panel, 
                "Confirm Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String password = new String(passwordField.getPassword());
                
                if (password.equals(currentUser.getPassword())) {
                    // Delete account
                    UserDAO userDAO = new UserDAO();
                    if (userDAO.deleteUser(currentUser.getId())) {
                        JOptionPane.showMessageDialog(this, 
                            "🗑 Your account has been permanently deleted.\n\n" +
                            "Thank you for using our application. Goodbye!",
                            "Account Deleted",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Clear current user and close all windows
                        DataStore.setCurrentUser(null);
                        dispose();
                        
                        // Close the main application window if needed
                        Window[] windows = Window.getWindows();
                        for (Window window : windows) {
                            if (window instanceof JFrame && window != this) {
                                window.dispose();
                            }
                        }
                        
                        // Optionally, you can redirect to login screen here
                        // new LoginFrame().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "❌ Failed to delete account. Please try again later.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Incorrect password. Account deletion cancelled.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }
    
    // Public getters for other classes
    public static int getHighAlertHours(Preferences p) { return p.getInt("alerts.high.hours", 10); }
    public static int getHighAlertMinutes(Preferences p) { return p.getInt("alerts.high.minutes", 0); }
    public static int getMediumAlertDays(Preferences p) { return p.getInt("alerts.medium.days", 2); }
    public static int getMediumAlertHours(Preferences p) { return p.getInt("alerts.medium.hours", 0); }
    public static int getLowAlertDays(Preferences p) { return p.getInt("alerts.low.days", 7); }
    public static int getLowAlertHours(Preferences p) { return p.getInt("alerts.low.hours", 0); }
    public static boolean isCustomAlerts(Preferences p) { return p.getBoolean("alerts.custom.enabled", true); }
    public static int getShortFocus(Preferences p) { return p.getInt("timer.short.focus", 25); }
    public static int getShortBreak(Preferences p) { return p.getInt("timer.short.break", 5); }
    public static int getLongFocus(Preferences p) { return p.getInt("timer.long.focus", 50); }
    public static int getLongBreak(Preferences p) { return p.getInt("timer.long.break", 10); }
}