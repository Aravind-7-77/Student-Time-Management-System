package tasks;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import DAOLOGIC.TaskDAO;
import data.DataStore;

public class ViewTasksFrame extends JFrame {
    private DefaultTableModel highModel, mediumModel, lowModel;
    private JLabel highCount, mediumCount, lowCount;
    private JButton refreshButton;
    
    // Store tasks with their IDs for each priority level
    private List<Task> highTasks = new ArrayList<>();
    private List<Task> mediumTasks = new ArrayList<>();
    private List<Task> lowTasks = new ArrayList<>();

    public ViewTasksFrame() {
        setTitle("Task Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        highModel = createModel(); 
        mediumModel = createModel(); 
        lowModel = createModel();

        JPanel main = new JPanel(new GridLayout(3, 1, 15, 15));
        main.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        main.setBackground(new Color(240, 248, 255));

        main.add(createSection("HIGH PRIORITY", highModel, highCount = new JLabel("0 tasks"), highTasks));
        main.add(createSection("MEDIUM PRIORITY", mediumModel, mediumCount = new JLabel("0 tasks"), mediumTasks));
        main.add(createSection("LOW PRIORITY", lowModel, lowCount = new JLabel("0 tasks"), lowTasks));

        // Add refresh button in the north
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(new Color(240, 248, 255));
        refreshButton = new JButton("REFRESH");
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.addActionListener(e -> loadTasks());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);
        loadTasks();
        add(main);
        setVisible(true);
    }

    private DefaultTableModel createModel() {
        return new DefaultTableModel(new String[]{
            "TASK NAME", "DUE TIME", "NEXT DUE", "CREATED", "CATEGORY", "REPEATING", "COMPLETE", "DELETE"
        }, 0) {
            public Class<?> getColumnClass(int col) { 
                return String.class; 
            }
            public boolean isCellEditable(int row, int col) { 
                return col == 6 || col == 7; 
            }
        };
    }

    private JPanel createSection(String title, DefaultTableModel model, JLabel countLabel, List<Task> taskList) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(70, 130, 180));
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        countLabel.setForeground(Color.WHITE);
        countLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        countLabel.setOpaque(true);
        countLabel.setBackground(new Color(100, 150, 200));
        countLabel.setHorizontalAlignment(JLabel.CENTER);
        countLabel.setPreferredSize(new Dimension(100, 30));
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setGridColor(new Color(200, 220, 240));
        table.setSelectionBackground(new Color(184, 207, 229));
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Header styling
        JTableHeader headerTable = table.getTableHeader();
        headerTable.setFont(new Font("Segoe UI", Font.BOLD, 14));
        headerTable.setBackground(new Color(100, 149, 237));
        headerTable.setForeground(Color.WHITE);
        headerTable.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180)));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);

        table.getColumnModel().getColumn(5).setCellRenderer(new RepeatingRenderer());

        // Pass taskList to button renderer and editor
        table.getColumnModel().getColumn(6).setCellRenderer(new CompleteButtonRenderer(taskList, table));
        table.getColumnModel().getColumn(6).setCellEditor(new CompleteButtonEditor(table, model, taskList, this));

        table.getColumnModel().getColumn(7).setCellRenderer(new DeleteButtonRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new DeleteButtonEditor(table, model, taskList, this));

        table.setDefaultRenderer(Object.class, new DueDateRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBackground(new Color(200, 220, 240));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }

    class RepeatingRenderer extends JLabel implements TableCellRenderer {
        public RepeatingRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value == null || value.toString().isEmpty() || value.toString().equals("NO")) {
                setText("NO");
                setForeground(new Color(150, 150, 150));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
            } else {
                setText(value.toString());
                setForeground(new Color(0, 100, 200));
                setFont(new Font("Segoe UI", Font.BOLD, 13));
            }
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return this;
        }
    }

    // IMPROVED: Complete Button Renderer with date-based reset logic
    class CompleteButtonRenderer extends JButton implements TableCellRenderer {
        private List<Task> taskList;
        private JTable table;
        
        public CompleteButtonRenderer(List<Task> taskList, JTable table) {
            this.taskList = taskList;
            this.table = table;
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < taskList.size()) {
                Task task = taskList.get(modelRow);
                String repeatType = task.getRepeatType();
                boolean isRepeating = task.isRepeating();
                
                Date dueDate = null;
                try {
                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dueDate = dbFormat.parse(task.getTargetDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Date now = new Date();
                
                if (isRepeating && dueDate != null) {
                    // Check based on recurrence type - comparing DATES only, not times
                    boolean shouldShowComplete = false;
                    
                    if ("daily".equals(repeatType)) {
                        // For daily: compare just the date (day/month/year)
                        shouldShowComplete = isDueDateTodayOrEarlier(dueDate, now);
                        
                        if (shouldShowComplete) {
                            setText("COMPLETE");
                            setBackground(new Color(46, 139, 87));
                            setForeground(Color.WHITE);
                            setEnabled(true);
                        } else {
                            setText("DONE TODAY");
                            setBackground(new Color(100, 100, 100));
                            setForeground(Color.WHITE);
                            setEnabled(false);
                        }
                    } 
                    else if ("weekly".equals(repeatType)) {
                        // For weekly: compare year and week number
                        shouldShowComplete = isDueThisWeekOrEarlier(dueDate, now);
                        
                        if (shouldShowComplete) {
                            setText("COMPLETE");
                            setBackground(new Color(46, 139, 87));
                            setForeground(Color.WHITE);
                            setEnabled(true);
                        } else {
                            setText("DONE THIS WEEK");
                            setBackground(new Color(100, 100, 100));
                            setForeground(Color.WHITE);
                            setEnabled(false);
                        }
                    } 
                    else if ("monthly".equals(repeatType)) {
                        // For monthly: compare year and month
                        shouldShowComplete = isDueThisMonthOrEarlier(dueDate, now);
                        
                        if (shouldShowComplete) {
                            setText("COMPLETE");
                            setBackground(new Color(46, 139, 87));
                            setForeground(Color.WHITE);
                            setEnabled(true);
                        } else {
                            setText("DONE THIS MONTH");
                            setBackground(new Color(100, 100, 100));
                            setForeground(Color.WHITE);
                            setEnabled(false);
                        }
                    }
                    else {
                        // Default for other types
                        setText("COMPLETE");
                        setBackground(new Color(46, 139, 87));
                        setForeground(Color.WHITE);
                        setEnabled(true);
                    }
                } 
                else if (!isRepeating && task.isCompleted()) {
                    setText("COMPLETED");
                    setBackground(new Color(100, 100, 100));
                    setForeground(Color.WHITE);
                    setEnabled(false);
                } 
                else {
                    setText("COMPLETE");
                    setBackground(new Color(46, 139, 87));
                    setForeground(Color.WHITE);
                    setEnabled(true);
                }
            } else {
                setText("COMPLETE");
                setBackground(new Color(46, 139, 87));
                setForeground(Color.WHITE);
                setEnabled(true);
            }
            
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setFocusPainted(false);
            
            return this;
        }
        
        private boolean isDueDateTodayOrEarlier(Date dueDate, Date currentDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(currentDate);
            
            // Compare only YEAR, MONTH, DAY - ignore time
            int dueYear = dueCal.get(Calendar.YEAR);
            int dueDayOfYear = dueCal.get(Calendar.DAY_OF_YEAR);
            
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR);
            
            return (dueYear < todayYear) || (dueYear == todayYear && dueDayOfYear <= todayDayOfYear);
        }
        
        private boolean isDueThisWeekOrEarlier(Date dueDate, Date currentDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(currentDate);
            
            int dueYear = dueCal.get(Calendar.YEAR);
            int dueWeek = dueCal.get(Calendar.WEEK_OF_YEAR);
            
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayWeek = todayCal.get(Calendar.WEEK_OF_YEAR);
            
            return (dueYear < todayYear) || (dueYear == todayYear && dueWeek <= todayWeek);
        }
        
        private boolean isDueThisMonthOrEarlier(Date dueDate, Date currentDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(currentDate);
            
            int dueYear = dueCal.get(Calendar.YEAR);
            int dueMonth = dueCal.get(Calendar.MONTH);
            
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayMonth = todayCal.get(Calendar.MONTH);
            
            return (dueYear < todayYear) || (dueYear == todayYear && dueMonth <= todayMonth);
        }
    }

    // IMPROVED: Complete Button Editor with date-based validation
    class CompleteButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;
        private JTable table;
        private DefaultTableModel model;
        private List<Task> taskList;
        private ViewTasksFrame parentFrame;
        private int currentRow;
        
        public CompleteButtonEditor(JTable table, DefaultTableModel model, List<Task> taskList, ViewTasksFrame parentFrame) {
            this.table = table;
            this.model = model;
            this.taskList = taskList;
            this.parentFrame = parentFrame;
            
            button = new JButton();
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setFocusPainted(false);
            
            button.addActionListener(e -> {
                fireEditingStopped();
                handleComplete();
            });
        }
        
        private void handleComplete() {
            int modelRow = table.convertRowIndexToModel(currentRow);
            if (modelRow >= 0 && modelRow < taskList.size()) {
                Task task = taskList.get(modelRow);
                String taskName = task.getName();
                String repeatType = task.getRepeatType();
                boolean isRepeating = task.isRepeating();
                
                Date dueDate = null;
                try {
                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dueDate = dbFormat.parse(task.getTargetDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Date now = new Date();
                
                // Validate if task can be completed based on recurrence type
                if (isRepeating && dueDate != null) {
                    boolean canComplete = false;
                    String errorMessage = "";
                    
                    if ("daily".equals(repeatType)) {
                        canComplete = isDueDateTodayOrEarlier(dueDate, now);
                        if (!canComplete) {
                            errorMessage = "This task is scheduled for " + formatDate(dueDate) + 
                                         "\nYou can complete it when that day arrives (after 12:00 AM).";
                        }
                    } 
                    else if ("weekly".equals(repeatType)) {
                        canComplete = isDueThisWeekOrEarlier(dueDate, now);
                        if (!canComplete) {
                            errorMessage = "This task is scheduled for week of " + formatDate(dueDate) + 
                                         "\nYou can complete it when that week arrives.";
                        }
                    } 
                    else if ("monthly".equals(repeatType)) {
                        canComplete = isDueThisMonthOrEarlier(dueDate, now);
                        if (!canComplete) {
                            errorMessage = "This task is scheduled for " + formatDate(dueDate) + 
                                         "\nYou can complete it when that month arrives.";
                        }
                    }
                    
                    if (!canComplete) {
                        JOptionPane.showMessageDialog(parentFrame, 
                            errorMessage,
                            "Cannot Complete Yet", 
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }
                
                int confirm = JOptionPane.showConfirmDialog(parentFrame, 
                    "Complete task: " + taskName + "?", 
                    "Complete Task", 
                    JOptionPane.YES_NO_OPTION);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    Date completionDate = new Date();
                    
                    if (isRepeating) {
                        Date nextDueDate = calculateNextDueDate(task, completionDate);
                        
                        if (TaskDAO.completeRepeatingTask(task.getId(), DataStore.getCurrentUser().getId(), nextDueDate)) {
                            String message = "✓ Task completed for this ";
                            if ("daily".equals(repeatType)) {
                                message += "day";
                            } else if ("weekly".equals(repeatType)) {
                                message += "week";
                            } else if ("monthly".equals(repeatType)) {
                                message += "month";
                            }
                            message += "! Next occurrence: " + formatDate(nextDueDate);
                            
                            JOptionPane.showMessageDialog(parentFrame, 
                                message, "Success", JOptionPane.INFORMATION_MESSAGE);
                            parentFrame.loadTasks();
                        }
                    } else {
                        if (TaskDAO.completeTask(task.getId(), DataStore.getCurrentUser().getId())) {
                            JOptionPane.showMessageDialog(parentFrame, 
                                "✓ Task completed permanently!", 
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                            parentFrame.loadTasks();
                        }
                    }
                }
            }
        }
        
        private boolean isDueDateTodayOrEarlier(Date dueDate, Date currentDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(currentDate);
            
            int dueYear = dueCal.get(Calendar.YEAR);
            int dueDayOfYear = dueCal.get(Calendar.DAY_OF_YEAR);
            
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR);
            
            return (dueYear < todayYear) || (dueYear == todayYear && dueDayOfYear <= todayDayOfYear);
        }
        
        private boolean isDueThisWeekOrEarlier(Date dueDate, Date currentDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(currentDate);
            
            int dueYear = dueCal.get(Calendar.YEAR);
            int dueWeek = dueCal.get(Calendar.WEEK_OF_YEAR);
            
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayWeek = todayCal.get(Calendar.WEEK_OF_YEAR);
            
            return (dueYear < todayYear) || (dueYear == todayYear && dueWeek <= todayWeek);
        }
        
        private boolean isDueThisMonthOrEarlier(Date dueDate, Date currentDate) {
            Calendar dueCal = Calendar.getInstance();
            dueCal.setTime(dueDate);
            
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(currentDate);
            
            int dueYear = dueCal.get(Calendar.YEAR);
            int dueMonth = dueCal.get(Calendar.MONTH);
            
            int todayYear = todayCal.get(Calendar.YEAR);
            int todayMonth = todayCal.get(Calendar.MONTH);
            
            return (dueYear < todayYear) || (dueYear == todayYear && dueMonth <= todayMonth);
        }
        
        private String formatDate(Date date) {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy");
            return displayFormat.format(date);
        }
        
        private Date calculateNextDueDate(Task task, Date completionDate) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(completionDate);
            
            String repeatType = task.getRepeatType();
            
            if ("daily".equals(repeatType)) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                // Reset time to original task time
                setOriginalTaskTime(task, cal);
            } 
            else if ("weekly".equals(repeatType)) {
                cal.add(Calendar.DAY_OF_MONTH, 7);
                setOriginalTaskTime(task, cal);
            } 
            else if ("monthly".equals(repeatType)) {
                cal.add(Calendar.MONTH, 1);
                int targetDay = task.getRepeatDate();
                if (targetDay > 0) {
                    int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    cal.set(Calendar.DAY_OF_MONTH, Math.min(targetDay, maxDay));
                }
                setOriginalTaskTime(task, cal);
            }
            
            return cal.getTime();
        }
        
        private void setOriginalTaskTime(Task task, Calendar cal) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date originalDate = sdf.parse(task.getTargetDate());
                Calendar originalCal = Calendar.getInstance();
                originalCal.setTime(originalDate);
                cal.set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            
            int modelRow = table.convertRowIndexToModel(row);
            if (modelRow >= 0 && modelRow < taskList.size()) {
                Task task = taskList.get(modelRow);
                String repeatType = task.getRepeatType();
                boolean isRepeating = task.isRepeating();
                
                Date dueDate = null;
                try {
                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    dueDate = dbFormat.parse(task.getTargetDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                Date now = new Date();
                
                if (isRepeating && dueDate != null) {
                    boolean shouldShowComplete = false;
                    
                    if ("daily".equals(repeatType)) {
                        shouldShowComplete = isDueDateTodayOrEarlier(dueDate, now);
                        if (shouldShowComplete) {
                            button.setText("COMPLETE");
                            button.setBackground(new Color(46, 139, 87));
                            button.setEnabled(true);
                        } else {
                            button.setText("DONE TODAY");
                            button.setBackground(new Color(100, 100, 100));
                            button.setEnabled(false);
                        }
                    } 
                    else if ("weekly".equals(repeatType)) {
                        shouldShowComplete = isDueThisWeekOrEarlier(dueDate, now);
                        if (shouldShowComplete) {
                            button.setText("COMPLETE");
                            button.setBackground(new Color(46, 139, 87));
                            button.setEnabled(true);
                        } else {
                            button.setText("DONE THIS WEEK");
                            button.setBackground(new Color(100, 100, 100));
                            button.setEnabled(false);
                        }
                    } 
                    else if ("monthly".equals(repeatType)) {
                        shouldShowComplete = isDueThisMonthOrEarlier(dueDate, now);
                        if (shouldShowComplete) {
                            button.setText("COMPLETE");
                            button.setBackground(new Color(46, 139, 87));
                            button.setEnabled(true);
                        } else {
                            button.setText("DONE THIS MONTH");
                            button.setBackground(new Color(100, 100, 100));
                            button.setEnabled(false);
                        }
                    }
                    else {
                        button.setText("COMPLETE");
                        button.setBackground(new Color(46, 139, 87));
                        button.setEnabled(true);
                    }
                } 
                else if (!isRepeating && task.isCompleted()) {
                    button.setText("COMPLETED");
                    button.setBackground(new Color(100, 100, 100));
                    button.setEnabled(false);
                } 
                else {
                    button.setText("COMPLETE");
                    button.setBackground(new Color(46, 139, 87));
                    button.setEnabled(true);
                }
            } else {
                button.setText("COMPLETE");
                button.setBackground(new Color(46, 139, 87));
                button.setEnabled(true);
            }
            
            button.setForeground(Color.WHITE);
            return button;
        }
        
        public Object getCellEditorValue() { return null; }
    }

    class DeleteButtonRenderer extends JButton implements TableCellRenderer {
        public DeleteButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("DELETE");
            setBackground(new Color(220, 50, 50));
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            setFocusPainted(false);
            return this;
        }
    }

    class DeleteButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private JButton button;
        private JTable table;
        private DefaultTableModel model;
        private List<Task> taskList;
        private ViewTasksFrame parentFrame;
        private int currentRow;
        
        public DeleteButtonEditor(JTable table, DefaultTableModel model, List<Task> taskList, ViewTasksFrame parentFrame) {
            this.table = table;
            this.model = model;
            this.taskList = taskList;
            this.parentFrame = parentFrame;
            
            button = new JButton("DELETE");
            button.setFont(new Font("Segoe UI", Font.BOLD, 11));
            button.setBackground(new Color(220, 50, 50));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            
            button.addActionListener(e -> {
                fireEditingStopped();
                handleDelete();
            });
        }
        
        private void handleDelete() {
            int modelRow = table.convertRowIndexToModel(currentRow);
            if (modelRow >= 0 && modelRow < taskList.size()) {
                Task task = taskList.get(modelRow);
                String taskName = task.getName();
                
                String message = task.isRepeating() ? 
                    "Delete repeating task \"" + taskName + "\"? This will remove all future occurrences." :
                    "Delete task: " + taskName + "?";
                    
                int confirm = JOptionPane.showConfirmDialog(parentFrame, 
                    message, "Confirm Delete", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    if (TaskDAO.deleteTask(task.getId(), DataStore.getCurrentUser().getId())) {
                        taskList.remove(modelRow);
                        model.removeRow(modelRow);
                        parentFrame.updateCounts();
                        
                        JOptionPane.showMessageDialog(parentFrame, 
                            "Task deleted successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }
        
        public Object getCellEditorValue() { return null; }
    }

    class DueDateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && column == 1) {
                try {
                    String dueTime = (String) value;
                    if (dueTime != null && dueTime.contains("/")) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date due = sdf.parse(dueTime);
                        Date now = new Date();
                        
                        if (due.before(now)) {
                            c.setBackground(new Color(255, 200, 200));
                        } else {
                            c.setBackground(Color.WHITE);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } catch (Exception e) {
                    c.setBackground(Color.WHITE);
                }
            } else if (!isSelected) {
                c.setBackground(Color.WHITE);
            } else {
                c.setBackground(table.getSelectionBackground());
            }
            
            return c;
        }
    }

    private void loadTasks() {
        highModel.setRowCount(0); 
        mediumModel.setRowCount(0); 
        lowModel.setRowCount(0);
        
        highTasks.clear();
        mediumTasks.clear();
        lowTasks.clear();
        
        List<Task> allTasks = TaskDAO.getTasksByUser(DataStore.getCurrentUser().getId());
        Date now = new Date();
        
        for (Task t : allTasks) {
            if (t.isDeleted()) continue;
            
            if (t.isRepeating() && t.isCompleted()) {
                try {
                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date nextDue = dbFormat.parse(t.getTargetDate());
                    
                    if (!nextDue.after(now)) {
                        TaskDAO.activateRecurringTask(t.getId(), DataStore.getCurrentUser().getId());
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            if (t.getPriority().equals("High")) {
                highTasks.add(t);
            } else if (t.getPriority().equals("Medium")) {
                mediumTasks.add(t);
            } else {
                lowTasks.add(t);
            }
            
            DefaultTableModel model = t.getPriority().equals("High") ? highModel :
                                      t.getPriority().equals("Medium") ? mediumModel : lowModel;
            
            String repeatText = t.getRepeatDisplayText();
            String dueTimeText = t.getDueTimeDisplay();
            String createdText = t.getFormattedCreatedDate();
            
            String nextDueText = "-";
            
            try {
                SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date taskDate = dbFormat.parse(t.getTargetDate());
                
                if (t.isRepeating()) {
                    if (t.isCompleted()) {
                        if ("daily".equals(t.getRepeatType())) {
                            nextDueText = "Done Today (Next: " + formatNextDue(taskDate) + ")";
                        } else if ("weekly".equals(t.getRepeatType())) {
                            nextDueText = "Done This Week (Next: " + formatNextDue(taskDate) + ")";
                        } else if ("monthly".equals(t.getRepeatType())) {
                            nextDueText = "Done This Month (Next: " + formatNextDue(taskDate) + ")";
                        }
                    } else {
                        Date nextDate = t.calculateNextOccurrence();
                        if (nextDate != null) {
                            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            nextDueText = displayFormat.format(nextDate);
                        }
                    }
                } else if (t.isCompleted()) {
                    nextDueText = "Completed";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            model.addRow(new Object[]{
                t.getName(),
                dueTimeText,
                nextDueText,
                createdText,
                t.getMode(),
                repeatText,
                "COMPLETE",
                "DELETE"
            });
        }
        updateCounts();
    }
    
    private String formatNextDue(Date date) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return displayFormat.format(date);
    }

    private void updateCounts() {
        int highTotal = highTasks.size();
        int mediumTotal = mediumTasks.size();
        int lowTotal = lowTasks.size();
        
        highCount.setText(highPendingCount() + " pending • " + highTotal + " total");
        mediumCount.setText(mediumPendingCount() + " pending • " + mediumTotal + " total");
        lowCount.setText(lowPendingCount() + " pending • " + lowTotal + " total");
    }
    
    private int highPendingCount() {
        int count = 0;
        Date now = new Date();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Task t : highTasks) {
            try {
                Date dueDate = dbFormat.parse(t.getTargetDate());
                if (t.isRepeating()) {
                    if ("daily".equals(t.getRepeatType())) {
                        if (isDueDateTodayOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    } else if ("weekly".equals(t.getRepeatType())) {
                        if (isDueThisWeekOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    } else if ("monthly".equals(t.getRepeatType())) {
                        if (isDueThisMonthOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    }
                } else {
                    if (!t.isCompleted()) count++;
                }
            } catch (Exception e) {
                if (!t.isCompleted()) count++;
            }
        }
        return count;
    }
    
    private int mediumPendingCount() {
        int count = 0;
        Date now = new Date();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Task t : mediumTasks) {
            try {
                Date dueDate = dbFormat.parse(t.getTargetDate());
                if (t.isRepeating()) {
                    if ("daily".equals(t.getRepeatType())) {
                        if (isDueDateTodayOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    } else if ("weekly".equals(t.getRepeatType())) {
                        if (isDueThisWeekOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    } else if ("monthly".equals(t.getRepeatType())) {
                        if (isDueThisMonthOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    }
                } else {
                    if (!t.isCompleted()) count++;
                }
            } catch (Exception e) {
                if (!t.isCompleted()) count++;
            }
        }
        return count;
    }
    
    private int lowPendingCount() {
        int count = 0;
        Date now = new Date();
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Task t : lowTasks) {
            try {
                Date dueDate = dbFormat.parse(t.getTargetDate());
                if (t.isRepeating()) {
                    if ("daily".equals(t.getRepeatType())) {
                        if (isDueDateTodayOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    } else if ("weekly".equals(t.getRepeatType())) {
                        if (isDueThisWeekOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    } else if ("monthly".equals(t.getRepeatType())) {
                        if (isDueThisMonthOrEarlier(dueDate, now) && !t.isCompleted()) count++;
                    }
                } else {
                    if (!t.isCompleted()) count++;
                }
            } catch (Exception e) {
                if (!t.isCompleted()) count++;
            }
        }
        return count;
    }
    
    private boolean isDueDateTodayOrEarlier(Date dueDate, Date currentDate) {
        Calendar dueCal = Calendar.getInstance();
        dueCal.setTime(dueDate);
        
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(currentDate);
        
        int dueYear = dueCal.get(Calendar.YEAR);
        int dueDayOfYear = dueCal.get(Calendar.DAY_OF_YEAR);
        
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayDayOfYear = todayCal.get(Calendar.DAY_OF_YEAR);
        
        return (dueYear < todayYear) || (dueYear == todayYear && dueDayOfYear <= todayDayOfYear);
    }
    
    private boolean isDueThisWeekOrEarlier(Date dueDate, Date currentDate) {
        Calendar dueCal = Calendar.getInstance();
        dueCal.setTime(dueDate);
        
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(currentDate);
        
        int dueYear = dueCal.get(Calendar.YEAR);
        int dueWeek = dueCal.get(Calendar.WEEK_OF_YEAR);
        
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayWeek = todayCal.get(Calendar.WEEK_OF_YEAR);
        
        return (dueYear < todayYear) || (dueYear == todayYear && dueWeek <= todayWeek);
    }
    
    private boolean isDueThisMonthOrEarlier(Date dueDate, Date currentDate) {
        Calendar dueCal = Calendar.getInstance();
        dueCal.setTime(dueDate);
        
        Calendar todayCal = Calendar.getInstance();
        todayCal.setTime(currentDate);
        
        int dueYear = dueCal.get(Calendar.YEAR);
        int dueMonth = dueCal.get(Calendar.MONTH);
        
        int todayYear = todayCal.get(Calendar.YEAR);
        int todayMonth = todayCal.get(Calendar.MONTH);
        
        return (dueYear < todayYear) || (dueYear == todayYear && dueMonth <= todayMonth);
    }

    @Override
    public void dispose() {
        int total = highTasks.size() + mediumTasks.size() + lowTasks.size();
        int pending = highPendingCount() + mediumPendingCount() + lowPendingCount();
        
        if (pending > 0) {
            JOptionPane.showMessageDialog(this,
                "You have " + pending + " pending task" + (pending != 1 ? "s" : "") + " out of " + total + " total.\n" +
                "High: " + highPendingCount() + " pending | " +
                "Medium: " + mediumPendingCount() + " pending | " +
                "Low: " + lowPendingCount() + " pending",
                "Task Summary",
                JOptionPane.INFORMATION_MESSAGE);
        }
        super.dispose();
    }
}