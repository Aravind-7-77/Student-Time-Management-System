package reports;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.Timer; // Add this import specifically

import data.DataStore;
import DAOLOGIC.TaskDAO;
import tasks.Task;
import model.User;

public class ReportFrame extends JFrame {
    
    private JPanel dailyPanel, weeklyPanel;
    private JLabel dailyProgressLabel, weeklyProgressLabel;
    private JProgressBar dailyProgressBar, weeklyProgressBar;
    private DefaultListModel<String> dailyPendingModel, dailyCompletedModel;
    private DefaultListModel<String> weeklyPendingModel, weeklyCompletedModel;
    private JLabel dailyDateLabel, weeklyDateRangeLabel;
    private Timer refreshTimer; // For auto-refresh
    private Timer clockTimer; // For clock
    
    public ReportFrame() {
        setTitle("Progress Reports");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 180), 
                                                     0, getHeight(), new Color(100, 180, 255));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Header
        JPanel headerPanel = createHeader();
        
        // Content panel with two sections
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Daily Progress Section
        dailyPanel = createProgressSection(" DAILY PROGRESS", true);
        
        // Weekly Progress Section
        weeklyPanel = createProgressSection(" WEEKLY PROGRESS", false);
        
        contentPanel.add(dailyPanel);
        contentPanel.add(weeklyPanel);
        
        // Refresh button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        JButton refreshBtn = createStyledButton(" REFRESH", new Color(70, 130, 180));
        JButton closeBtn = createStyledButton(" CLOSE", new Color(220, 50, 50));
        
        refreshBtn.addActionListener(e -> refreshData());
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(closeBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Initial data load
        refreshData();
        
        // Auto-refresh every 30 seconds
        startAutoRefresh();
        
        setVisible(true);
    }
    
    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
        
        User user = DataStore.getCurrentUser();
        String userName = (user != null) ? user.getUsername() : "User";
        
        JLabel titleLabel = new JLabel(" PROGRESS REPORTS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("Welcome, " + userName + "!");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        userLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(userLabel);
        
        // Current date/time
        JPanel dateTimePanel = new JPanel(new GridLayout(2, 1));
        dateTimePanel.setOpaque(false);
        dateTimePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        
        JLabel timeLabel = new JLabel(timeFormat.format(new Date()));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JLabel dateLabel = new JLabel(dateFormat.format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(255, 255, 255, 200));
        dateLabel.setHorizontalAlignment(JLabel.CENTER);
        
        dateTimePanel.add(timeLabel);
        dateTimePanel.add(dateLabel);
        
        // Update time every second
        clockTimer = new Timer(1000, e -> {
            timeLabel.setText(timeFormat.format(new Date()));
            dateLabel.setText(dateFormat.format(new Date()));
        });
        clockTimer.start();
        
        panel.add(textPanel, BorderLayout.WEST);
        panel.add(dateTimePanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createProgressSection(String title, boolean isDaily) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 255, 255, 150), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Header with title and date range
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        
        JLabel dateLabel = new JLabel();
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.GRAY);
        
        if (isDaily) {
            dailyDateLabel = dateLabel;
        } else {
            weeklyDateRangeLabel = dateLabel;
        }
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        // Progress section
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel progressLabel = new JLabel("Completion Progress", SwingConstants.CENTER);
        progressLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        progressLabel.setForeground(new Color(70, 130, 180));
        
        JPanel barPanel = new JPanel(new BorderLayout(10, 5));
        barPanel.setOpaque(false);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressBar.setForeground(new Color(46, 139, 87));
        progressBar.setBackground(new Color(220, 220, 220));
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        if (isDaily) {
            dailyProgressBar = progressBar;
            dailyProgressLabel = progressLabel;
        } else {
            weeklyProgressBar = progressBar;
            weeklyProgressLabel = progressLabel;
        }
        
        barPanel.add(progressBar, BorderLayout.CENTER);
        
        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(barPanel, BorderLayout.CENTER);
        
        // Lists panel
        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        listsPanel.setOpaque(false);
        listsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Pending tasks list
        JPanel pendingPanel = createListPanel(" PENDING", new Color(220, 120, 0));
        JList<String> pendingList = (JList<String>) ((JScrollPane) pendingPanel.getComponent(1)).getViewport().getView();
        
        // Completed tasks list
        JPanel completedPanel = createListPanel(" COMPLETED", new Color(46, 139, 87));
        JList<String> completedList = (JList<String>) ((JScrollPane) completedPanel.getComponent(1)).getViewport().getView();
        
        if (isDaily) {
            dailyPendingModel = (DefaultListModel<String>) pendingList.getModel();
            dailyCompletedModel = (DefaultListModel<String>) completedList.getModel();
        } else {
            weeklyPendingModel = (DefaultListModel<String>) pendingList.getModel();
            weeklyCompletedModel = (DefaultListModel<String>) completedList.getModel();
        }
        
        listsPanel.add(pendingPanel);
        listsPanel.add(completedPanel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(progressPanel, BorderLayout.CENTER);
        panel.add(listsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createListPanel(String title, Color color) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        list.setBackground(new Color(250, 250, 250));
        list.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshData() {
        User user = DataStore.getCurrentUser();
        if (user == null) return;
        
        int userId = user.getId();
        Date now = new Date();
        
        // Get daily tasks
        List<Task> dailyTasks = TaskDAO.getTasksByDate(userId, now);
        List<Task> dailyCompleted = new ArrayList<>();
        List<Task> dailyPending = new ArrayList<>();
        
        for (Task task : dailyTasks) {
            if (task.isCompleted()) {
                dailyCompleted.add(task);
            } else {
                dailyPending.add(task);
            }
        }
        
        // Update daily progress
        int dailyPercentage = TaskDAO.getDailyProgressPercentage(userId, now);
        dailyProgressBar.setValue(dailyPercentage);
        dailyProgressLabel.setText(String.format("Completion Progress: %d/%d tasks (%d%%)", 
            dailyCompleted.size(), dailyTasks.size(), dailyPercentage));
        
        // Get weekly tasks
        List<Task> weeklyTasks = TaskDAO.getTasksForCurrentWeek(userId);
        List<Task> weeklyCompleted = new ArrayList<>();
        List<Task> weeklyPending = new ArrayList<>();
        
        for (Task task : weeklyTasks) {
            if (task.isCompleted()) {
                weeklyCompleted.add(task);
            } else {
                weeklyPending.add(task);
            }
        }
        
        // Update weekly progress
        int weeklyPercentage = TaskDAO.getWeeklyProgressPercentage(userId);
        weeklyProgressBar.setValue(weeklyPercentage);
        weeklyProgressLabel.setText(String.format("Completion Progress: %d/%d tasks (%d%%)", 
            weeklyCompleted.size(), weeklyTasks.size(), weeklyPercentage));
        
        // Update date displays
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dailyDateLabel.setText(dateFormat.format(now));
        
        // Update weekly date range
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date weekStart = cal.getTime();
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEnd = cal.getTime();
        SimpleDateFormat weekFormat = new SimpleDateFormat("dd MMM");
        weeklyDateRangeLabel.setText(String.format("%s - %s", 
            weekFormat.format(weekStart), weekFormat.format(weekEnd)));
        
        // Update daily lists
        updateDailyLists(dailyPending, dailyCompleted);
        
        // Update weekly lists
        updateWeeklyLists(weeklyPending, weeklyCompleted);
    }
    
    private void updateDailyLists(List<Task> pending, List<Task> completed) {
        dailyPendingModel.clear();
        dailyCompletedModel.clear();
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        
        for (Task task : pending) {
            try {
                Date taskDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(task.getTargetDate());
                String time = timeFormat.format(taskDate);
                String priority = getPriorityEmoji(task.getPriority());
                dailyPendingModel.addElement(String.format("%s %s (%s) - %s", 
                    priority, task.getName(), task.getMode(), time));
            } catch (Exception e) {
                dailyPendingModel.addElement(task.getName());
            }
        }
        
        for (Task task : completed) {
            try {
                Date taskDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(task.getTargetDate());
                String time = timeFormat.format(taskDate);
                String priority = getPriorityEmoji(task.getPriority());
                dailyCompletedModel.addElement(String.format("%s %s (%s) - %s ", 
                    priority, task.getName(), task.getMode(), time));
            } catch (Exception e) {
                dailyCompletedModel.addElement(task.getName() );
            }
        }
        
        if (pending.isEmpty()) {
            dailyPendingModel.addElement(" No pending tasks for today!");
        }
        
        if (completed.isEmpty()) {
            dailyCompletedModel.addElement("No completed tasks yet today");
        }
    }
    
    private void updateWeeklyLists(List<Task> pending, List<Task> completed) {
        weeklyPendingModel.clear();
        weeklyCompletedModel.clear();
        
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, HH:mm");
        
        for (Task task : pending) {
            try {
                Date taskDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(task.getTargetDate());
                String dateStr = dateTimeFormat.format(taskDate);
                String priority = getPriorityEmoji(task.getPriority());
                weeklyPendingModel.addElement(String.format("%s %s (%s) - %s", 
                    priority, task.getName(), task.getMode(), dateStr));
            } catch (Exception e) {
                weeklyPendingModel.addElement(task.getName());
            }
        }
        
        for (Task task : completed) {
            try {
                Date taskDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(task.getTargetDate());
                String dateStr = dateTimeFormat.format(taskDate);
                String priority = getPriorityEmoji(task.getPriority());
                weeklyCompletedModel.addElement(String.format("%s %s (%s) - %s ", 
                    priority, task.getName(), task.getMode(), dateStr));
            } catch (Exception e) {
                weeklyCompletedModel.addElement(task.getName() );
            }
        }
        
        if (pending.isEmpty()) {
            weeklyPendingModel.addElement(" No pending tasks this week!");
        }
        
        if (completed.isEmpty()) {
            weeklyCompletedModel.addElement("No completed tasks this week");
        }
    }
    
    private String getPriorityEmoji(String priority) {
        switch (priority.toLowerCase()) {
            case "high": return "";
            case "medium": return "";
            case "low": return "";
            default: return "";
        }
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> refreshData());
        refreshTimer.start();
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        if (clockTimer != null) {
            clockTimer.stop();
        }
        super.dispose();
    }
}