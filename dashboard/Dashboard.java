package dashboard;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import auth.LoginFrame;
import data.DataStore;
import DAOLOGIC.TaskDAO;
import tasks.Task;
import tasks.TaskFrame;
import tasks.ViewTasksFrame;
import focustimer.FocusTimerFrame;
import reports.ReportFrame;
import settings.SettingsFrame;
import model.User;

public class Dashboard extends JFrame {

    private DefaultListModel<String> alertModel;
    private Timer alertTimer;
    private Timer refreshTimer;
    private JLabel timeLabel;
    private JLabel dateLabel;
    private JButton refreshButton;
    private JPanel statsPanel;
    private JLabel totalLabel, completedLabel, pendingLabel;
    private Map<String, Long> alertTracker;
    private Preferences prefs;

    public Dashboard() {
        setTitle("Student Time Manager - Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        if (DataStore.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(this, "Please login first!");
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        prefs = Preferences.userNodeForPackage(SettingsFrame.class);
        alertTracker = new HashMap<>();
        
        add(createSidebar(), BorderLayout.WEST);
        add(createMainPanel(), BorderLayout.CENTER);

        refreshAlerts();
        updateStats();
        startAutoRefresh();
        startClock();

        setVisible(true);
    }

    private void startAutoRefresh() {
        alertTimer = new Timer(10000, e -> refreshAlerts());
        alertTimer.start();
        
        refreshTimer = new Timer(10000, e -> updateStats());
        refreshTimer.start();
    }

    private void startClock() {
        Timer clockTimer = new Timer(1000, e -> {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");
            timeLabel.setText(timeFormat.format(new Date()));
            dateLabel.setText(dateFormat.format(new Date()));
        });
        clockTimer.start();
    }

    private void updateStats() {
        User u = DataStore.getCurrentUser();
        int total = 0;
        int completed = 0;
        int pending = 0;
        
        if (u != null) {
            List<Task> tasks = TaskDAO.getTasksByUser(u.getId());
            Date now = new Date();
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = dateOnlyFormat.format(now);
            
            for (Task t : tasks) {
                if (!t.isDeleted()) {
                    total++;
                    
                    if (t.isRepeating()) {
                        if (t.isCompleted()) {
                            completed++;
                        } else {
                            try {
                                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date taskDate = dbFormat.parse(t.getTargetDate());
                                String taskDateStr = dateOnlyFormat.format(taskDate);
                                
                                if (taskDateStr.equals(todayStr)) {
                                    pending++;
                                } else if (taskDate.after(now)) {
                                    pending++;
                                } else {
                                    completed++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                pending++;
                            }
                        }
                    } else {
                        if (t.isCompleted()) {
                            completed++;
                        } else {
                            pending++;
                        }
                    }
                }
            }
        }

        if (totalLabel != null) {
            totalLabel.setText(String.valueOf(total));
            completedLabel.setText(String.valueOf(completed));
            pendingLabel.setText(String.valueOf(pending));
        }
    }

    private boolean isTodayOccurrenceCompleted(Task task) {
        if (!task.isRepeating()) return task.isCompleted();
        
        try {
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            Date taskDate = dbFormat.parse(task.getTargetDate());
            
            String todayStr = dateOnlyFormat.format(now);
            String taskDateStr = dateOnlyFormat.format(taskDate);
            
            if (todayStr.equals(taskDateStr)) {
                return task.isCompleted();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return task.isCompleted();
        }
    }

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(260, getHeight()));
        panel.setBackground(new Color(25, 40, 65));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));

        JLabel title = new JLabel("STUDENT TIMER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        User u = DataStore.getCurrentUser();
        String userName = (u != null) ? u.getUsername() : "User";
        
        JLabel userNameLabel = new JLabel(userName);
        userNameLabel.setForeground(new Color(200, 220, 250));
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(title);
        panel.add(userNameLabel);
        panel.add(Box.createVerticalStrut(40));

        JButton addTask = createButton(" ADD TASK", new Color(70, 130, 180));
        JButton viewTask = createButton(" VIEW TASKS", new Color(100, 149, 237));
        JButton focusTimer = createButton(" FOCUS TIMER", new Color(60, 120, 200));
        
        panel.add(addTask);
        panel.add(Box.createVerticalStrut(10));
        panel.add(viewTask);
        panel.add(Box.createVerticalStrut(10));
        panel.add(focusTimer);
        
        panel.add(Box.createVerticalStrut(20));
        
        JButton reports = createButton(" REPORTS", new Color(150, 100, 200));
        JButton settings = createButton(" SETTINGS", new Color(100, 100, 100));
        settings.setEnabled(true);
        settings.setToolTipText("Application Settings - Customize alert timings");

        panel.add(reports);
        panel.add(Box.createVerticalStrut(10));
        panel.add(settings);

        panel.add(Box.createVerticalGlue());

        JButton logout = createButton(" LOGOUT", new Color(180, 70, 70));
        panel.add(logout);

        addTask.addActionListener(e -> new TaskFrame());
        viewTask.addActionListener(e -> new ViewTasksFrame());
        focusTimer.addActionListener(e -> new FocusTimerFrame());
        reports.addActionListener(e -> new ReportFrame());
        settings.addActionListener(e -> new SettingsFrame());
        
        logout.addActionListener(e -> {
            if (alertTimer != null) alertTimer.stop();
            if (refreshTimer != null) refreshTimer.stop();
            dispose();
            DataStore.setCurrentUser(null);
            new LoginFrame().setVisible(true);
        });

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        panel.add(createTopPanel(), BorderLayout.NORTH);
        panel.add(createAlertPanel(), BorderLayout.CENTER);
        panel.add(createStatsPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        User u = DataStore.getCurrentUser();
        String userName = (u != null) ? u.getUsername() : "User";
        
        JPanel welcomePanel = new JPanel(new GridLayout(2, 1));
        welcomePanel.setOpaque(false);
        
        JLabel hi = new JLabel("Welcome back, " + userName + "!");
        hi.setFont(new Font("Segoe UI", Font.BOLD, 28));
        hi.setForeground(new Color(25, 40, 65));
        
        JLabel msg = new JLabel("Track your tasks and stay productive");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        msg.setForeground(Color.GRAY);
        
        welcomePanel.add(hi);
        welcomePanel.add(msg);

        JPanel timePanel = new JPanel(new GridLayout(2, 1));
        timePanel.setOpaque(false);
        timePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        timeLabel.setForeground(new Color(70, 130, 180));
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.DARK_GRAY);
        dateLabel.setHorizontalAlignment(JLabel.CENTER);
        
        timePanel.add(timeLabel);
        timePanel.add(dateLabel);

        panel.add(welcomePanel, BorderLayout.WEST);
        panel.add(timePanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createAlertPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 50, 50), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JLabel title = new JLabel(" SMART ALERTS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(220, 50, 50));
        
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        refreshPanel.setOpaque(false);
        
        boolean customEnabled = SettingsFrame.isCustomAlerts(prefs);
        String alertMode = customEnabled ? " Custom Alerts ON" : " Default Alerts";
        JLabel alertModeLabel = new JLabel(alertMode);
        alertModeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        alertModeLabel.setForeground(customEnabled ? new Color(46, 204, 113) : Color.GRAY);
        
        JLabel refreshIcon = new JLabel("Auto-refresh every 10s");
        refreshIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshIcon.setForeground(Color.GRAY);
        
        refreshButton = new JButton("REFRESH NOW");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> {
            refreshAlerts();
            updateStats();
        });
        
        refreshPanel.add(alertModeLabel);
        refreshPanel.add(Box.createHorizontalStrut(10));
        refreshPanel.add(refreshIcon);
        refreshPanel.add(refreshButton);
        
        header.add(title, BorderLayout.WEST);
        header.add(refreshPanel, BorderLayout.EAST);

        alertModel = new DefaultListModel<>();
        JList<String> alertList = new JList<>(alertModel);
        alertList.setFont(new Font("Segoe UI", Font.BOLD, 14));
        alertList.setBackground(new Color(255, 250, 250));
        alertList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(alertList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        User u = DataStore.getCurrentUser();
        int total = 0;
        int completed = 0;
        int pending = 0;
        
        if (u != null) {
            List<Task> tasks = TaskDAO.getTasksByUser(u.getId());
            Date now = new Date();
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
            String todayStr = dateOnlyFormat.format(now);
            
            for (Task t : tasks) {
                if (!t.isDeleted()) {
                    total++;
                    
                    if (t.isRepeating()) {
                        if (t.isCompleted()) {
                            completed++;
                        } else {
                            try {
                                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date taskDate = dbFormat.parse(t.getTargetDate());
                                String taskDateStr = dateOnlyFormat.format(taskDate);
                                
                                if (taskDateStr.equals(todayStr)) {
                                    pending++;
                                } else if (taskDate.after(now)) {
                                    pending++;
                                } else {
                                    completed++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                pending++;
                            }
                        }
                    } else {
                        if (t.isCompleted()) {
                            completed++;
                        } else {
                            pending++;
                        }
                    }
                }
            }
        }

        Object[] statCards = createStatCards(total, completed, pending);
        totalLabel = (JLabel) statCards[0];
        completedLabel = (JLabel) statCards[1];
        pendingLabel = (JLabel) statCards[2];

        return statsPanel;
    }

    private Object[] createStatCards(int total, int completed, int pending) {
        JLabel totalValueLabel = new JLabel(String.valueOf(total));
        JLabel completedValueLabel = new JLabel(String.valueOf(completed));
        JLabel pendingValueLabel = new JLabel(String.valueOf(pending));
        
        statsPanel.add(createStatCard(" TOTAL TASKS", totalValueLabel, new Color(70, 130, 180)));
        statsPanel.add(createStatCard(" COMPLETED", completedValueLabel, new Color(60, 160, 60)));
        statsPanel.add(createStatCard(" PENDING", pendingValueLabel, new Color(220, 120, 0)));
        
        return new Object[]{totalValueLabel, completedValueLabel, pendingValueLabel};
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(color.darker());
        valueLabel.setHorizontalAlignment(JLabel.CENTER);

        panel.add(titleLabel);
        panel.add(valueLabel);

        return panel;
    }

    private void refreshAlerts() {
        if (alertModel == null) return;
        alertModel.clear();

        User currentUser = DataStore.getCurrentUser();
        if (currentUser == null) {
            alertModel.addElement("Please login to view alerts");
            return;
        }

        boolean customAlertsEnabled = SettingsFrame.isCustomAlerts(prefs);
        
        List<Task> tasks = TaskDAO.getTasksByUser(currentUser.getId());
        Date now = new Date();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        String today = dateOnlyFormat.format(now);
        Map<String, Boolean> alertedToday = new HashMap<>();
        boolean hasAlerts = false;

        for (Task t : tasks) {
            if (t.isDeleted() || t.isCompleted()) continue;
            
            if (t.getTargetDate() != null && !t.getTargetDate().isEmpty()) {
                try {
                    Date dueDate = dbFormat.parse(t.getTargetDate());
                    long timeUntilDue;
                    Date comparisonDate = dueDate;
                    
                    if (t.isRepeating()) {
                        String taskTime = timeFormat.format(dueDate);
                        String todayStr = dateOnlyFormat.format(now);
                        Date todayWithTaskTime = dbFormat.parse(todayStr + " " + taskTime + ":00");
                        
                        if (isTodayOccurrenceCompleted(t)) continue;
                        comparisonDate = todayWithTaskTime;
                    }
                    
                    timeUntilDue = comparisonDate.getTime() - now.getTime();
                    long minutesUntilDue = TimeUnit.MILLISECONDS.toMinutes(timeUntilDue);
                    long hoursUntilDue = TimeUnit.MILLISECONDS.toHours(timeUntilDue);
                    long daysUntilDue = TimeUnit.MILLISECONDS.toDays(timeUntilDue);
                    boolean isOverdue = timeUntilDue < 0;
                    
                    // Get alert thresholds based on priority
                    long alertThreshold = 0;
                    String priority = t.getPriority();
                    
                    if ("High".equalsIgnoreCase(priority)) {
                        int hours = SettingsFrame.getHighAlertHours(prefs);
                        int minutes = SettingsFrame.getHighAlertMinutes(prefs);
                        alertThreshold = (hours * 60L * 60 * 1000) + (minutes * 60L * 1000);
                    } else if ("Medium".equalsIgnoreCase(priority)) {
                        int days = SettingsFrame.getMediumAlertDays(prefs);
                        int hours = SettingsFrame.getMediumAlertHours(prefs);
                        alertThreshold = (days * 24L * 60 * 60 * 1000) + (hours * 60L * 60 * 1000);
                    } else if ("Low".equalsIgnoreCase(priority)) {
                        int days = SettingsFrame.getLowAlertDays(prefs);
                        int hours = SettingsFrame.getLowAlertHours(prefs);
                        alertThreshold = (days * 24L * 60 * 60 * 1000) + (hours * 60L * 60 * 1000);
                    }
                    
                    String taskKey = t.getName() + "|" + today;
                    if (alertedToday.containsKey(taskKey)) continue;
                    
                    boolean shouldAlert = false;
                    String alertMessage = "";
                    
                    // OVERDUE - always alert
                    if (isOverdue) {
                        if (t.isRepeating()) {
                            alertMessage = " OVERDUE TODAY: " + t.getName();
                        } else {
                            long overdueDays = Math.abs(daysUntilDue);
                            if (overdueDays > 0) {
                                alertMessage = " OVERDUE: " + t.getName() + " - " + overdueDays + " day(s) late";
                            } else {
                                alertMessage = " OVERDUE: " + t.getName();
                            }
                        }
                        shouldAlert = true;
                    }
                    // Due today
                    else if (daysUntilDue == 0) {
                        if (customAlertsEnabled) {
                            // Only alert if within custom threshold
                            if (timeUntilDue <= alertThreshold) {
                                alertMessage = " DUE TODAY: " + t.getName() + " at " + timeFormat.format(comparisonDate);
                                if (minutesUntilDue > 0 && minutesUntilDue <= 60) {
                                    alertMessage += " (in " + minutesUntilDue + " min)";
                                }
                                shouldAlert = true;
                            }
                        } else {
                            alertMessage = " DUE TODAY: " + t.getName() + " at " + timeFormat.format(comparisonDate);
                            if (minutesUntilDue > 0 && minutesUntilDue <= 60) {
                                alertMessage += " (in " + minutesUntilDue + " min)";
                            }
                            shouldAlert = true;
                        }
                    }
                    // Future tasks - check if within alert window
                    else if (timeUntilDue > 0 && timeUntilDue <= alertThreshold) {
                        if ("High".equalsIgnoreCase(priority)) {
                            if (hoursUntilDue > 0) {
                                alertMessage = " HIGH: " + t.getName() + " due in " + hoursUntilDue + " hour(s)";
                            } else if (minutesUntilDue > 0) {
                                alertMessage = " HIGH: " + t.getName() + " due in " + minutesUntilDue + " minutes";
                            } else {
                                alertMessage = " HIGH: " + t.getName() + " is approaching!";
                            }
                        } else if ("Medium".equalsIgnoreCase(priority)) {
                            if (daysUntilDue == 1) {
                                alertMessage = " MEDIUM: " + t.getName() + " due tomorrow";
                            } else if (daysUntilDue > 1) {
                                alertMessage = " MEDIUM: " + t.getName() + " due in " + daysUntilDue + " days";
                            } else if (hoursUntilDue > 0) {
                                alertMessage = " MEDIUM: " + t.getName() + " due in " + hoursUntilDue + " hours";
                            } else {
                                alertMessage = " MEDIUM: " + t.getName() + " is coming up!";
                            }
                        } else if ("Low".equalsIgnoreCase(priority)) {
                            if (daysUntilDue <= 2 && daysUntilDue > 0) {
                                alertMessage = " LOW: " + t.getName() + " due in " + daysUntilDue + " day(s)";
                            } else if (daysUntilDue > 2) {
                                alertMessage = " LOW: " + t.getName() + " due in " + daysUntilDue + " days";
                            } else {
                                alertMessage = " LOW: " + t.getName() + " - don't forget!";
                            }
                        }
                        shouldAlert = true;
                    }
                    
                    if (shouldAlert) {
                        alertModel.addElement(alertMessage);
                        alertedToday.put(taskKey, true);
                        hasAlerts = true;
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        
        if (!hasAlerts) {
            if (customAlertsEnabled) {
                alertModel.addElement("✅ All tasks on track with custom alert settings!");
            } else {
                alertModel.addElement("✅ All tasks on track! Great job!");
            }
        }
    }

    private JButton createButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(220, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
}