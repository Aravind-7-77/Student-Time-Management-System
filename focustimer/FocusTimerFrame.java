package focustimer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Toolkit;
import java.util.prefs.Preferences;
import settings.SettingsFrame;

public class FocusTimerFrame extends JFrame {

    private JLabel timerLabel, subtitleLabel;
    private JButton shortBtn, longBtn;
    private JButton startBtn, stopBtn, resetBtn;

    private Timer timer;
    private int timeRemaining;
    private boolean isRunning = false;
    private boolean isBreak = false;
    private int focusMinutes = 25; // default
    private int breakMinutes = 5;   // default
    
    private Preferences prefs;

    public FocusTimerFrame() {
        // Load preferences from SettingsFrame
        prefs = Preferences.userNodeForPackage(SettingsFrame.class);
        
        // Get values from settings
        int shortFocus = SettingsFrame.getShortFocus(prefs);
        int shortBreak = SettingsFrame.getShortBreak(prefs);
        int longFocus = SettingsFrame.getLongFocus(prefs);
        int longBreak = SettingsFrame.getLongBreak(prefs);

        setTitle("Focus Timer");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Make it full screen

        // ===== Background Panel with Gradient =====
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Beautiful gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(25, 40, 65),
                    0, getHeight(), new Color(45, 70, 100)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // ===== Title Section =====
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(60, 0, 30, 0));

        JLabel titleLabel = new JLabel(" Focus Timer", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);

        subtitleLabel = new JLabel(
                "Start your work – One focused session can impact a lot",
                SwingConstants.CENTER
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(255, 255, 255, 220));

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        backgroundPanel.add(titlePanel, BorderLayout.NORTH);

        // ===== Center Panel =====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // ===== Timer Card =====
        JPanel timerCard = new JPanel();
        timerCard.setOpaque(false);
        timerCard.setLayout(new BorderLayout());
        timerCard.setMaximumSize(new Dimension(500, 200));
        timerCard.setAlignmentX(Component.CENTER_ALIGNMENT);

        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 120));
        timerLabel.setForeground(Color.WHITE);
        timerCard.add(timerLabel, BorderLayout.CENTER);

        // ===== Session Selection Buttons =====
        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        sessionPanel.setOpaque(false);
        sessionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Use values from settings for button text
        shortBtn = new JButton("Short Session (" + shortFocus + " min)");
        longBtn = new JButton("Long Session (" + longFocus + " min)");

        styleButton(shortBtn, new Color(52, 152, 219));
        styleButton(longBtn, new Color(155, 89, 182));

        sessionPanel.add(shortBtn);
        sessionPanel.add(longBtn);

        // ===== Control Buttons =====
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        controlPanel.setOpaque(false);
        controlPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        resetBtn = new JButton("Reset");

        styleButton(startBtn, new Color(46, 204, 113));
        styleButton(stopBtn, new Color(231, 76, 60));
        styleButton(resetBtn, new Color(241, 196, 15));

        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(resetBtn);

        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(timerCard);
        centerPanel.add(Box.createVerticalStrut(60));
        centerPanel.add(sessionPanel);
        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(controlPanel);

        backgroundPanel.add(centerPanel, BorderLayout.CENTER);

        // ===== Footer with Quote =====
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 50, 0));

        JLabel quoteLabel = new JLabel(" The secret of getting ahead is getting started ", SwingConstants.CENTER);
        quoteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        quoteLabel.setForeground(new Color(255, 255, 255, 180));

        footerPanel.add(quoteLabel);
        backgroundPanel.add(footerPanel, BorderLayout.SOUTH);

        // ===== TIMER LOGIC =====
        timer = new Timer(1000, e -> {
            if (timeRemaining > 0) {
                timeRemaining--;
                updateTimerLabel();
            } else {
                timer.stop();
                Toolkit.getDefaultToolkit().beep();
                handleSessionEnd();
            }
        });

        // ===== Session Selection - Using values from settings =====
        shortBtn.addActionListener(e -> {
            if (!isRunning) {
                focusMinutes = shortFocus;
                breakMinutes = shortBreak;
                timeRemaining = focusMinutes * 60;
                isBreak = false;
                subtitleLabel.setText("Short Session Selected - " + focusMinutes + " minutes of focus");
                updateTimerLabel();
            }
        });

        longBtn.addActionListener(e -> {
            if (!isRunning) {
                focusMinutes = longFocus;
                breakMinutes = longBreak;
                timeRemaining = focusMinutes * 60;
                isBreak = false;
                subtitleLabel.setText("Long Session Selected - " + focusMinutes + " minutes of deep work");
                updateTimerLabel();
            }
        });

        // ===== Start =====
        startBtn.addActionListener(e -> {
            if (!isRunning) {
                isRunning = true;
                disableSessionButtons();
                timer.start();
                subtitleLabel.setText(" Stay Focused! You've got this!");
            }
        });

        // ===== Stop =====
        stopBtn.addActionListener(e -> {
            timer.stop();
            isRunning = false;
            enableSessionButtons();
            subtitleLabel.setText(" Timer Paused - Take a breath");
        });

        // ===== Reset =====
        resetBtn.addActionListener(e -> {
            timer.stop();
            isRunning = false;
            isBreak = false;
            focusMinutes = shortFocus;
            breakMinutes = shortBreak;
            timeRemaining = focusMinutes * 60;
            subtitleLabel.setText("Timer Reset - Ready for a new session");
            enableSessionButtons();
            updateTimerLabel();
        });

        // default to short session values from settings
        focusMinutes = shortFocus;
        breakMinutes = shortBreak;
        timeRemaining = focusMinutes * 60;
        updateTimerLabel();

        setVisible(true);
    }

    // ===== Handle Session End =====
    private void handleSessionEnd() {
        if (!isBreak) {
            // Use break minutes from settings
            isBreak = true;
            timeRemaining = breakMinutes * 60;
            subtitleLabel.setText(" Break Time! Take " + breakMinutes + " minutes to relax");
            timer.start();

        } else {
            isRunning = false;
            subtitleLabel.setText(" Session Completed! Great job! Start another?");
            enableSessionButtons();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void updateTimerLabel() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // ===== Enhanced Button Styling =====
    private void styleButton(JButton button, Color bgColor) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(220, 50));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.darker());
                button.setFont(new Font("Segoe UI", Font.BOLD, 19));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
                button.setFont(new Font("Segoe UI", Font.BOLD, 18));
            }
        });
    }

    private void disableSessionButtons() {
        shortBtn.setEnabled(false);
        longBtn.setEnabled(false);
        shortBtn.setBackground(new Color(100, 100, 100));
        longBtn.setBackground(new Color(100, 100, 100));
    }

    private void enableSessionButtons() {
        shortBtn.setEnabled(true);
        longBtn.setEnabled(true);
        shortBtn.setBackground(new Color(52, 152, 219));
        longBtn.setBackground(new Color(155, 89, 182));
    }
}