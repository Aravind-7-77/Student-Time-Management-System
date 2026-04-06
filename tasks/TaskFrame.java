package tasks;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import DAOLOGIC.TaskDAO;
import data.DataStore;

public class TaskFrame extends JFrame {

    private JTextField taskField;
    private JRadioButton highRB, mediumRB, lowRB;
    private JRadioButton studyRB, workRB, personalRB;
    private JSpinner dateTimeSpinner;
    private JCheckBox repeatingCheckBox;
    private JComboBox<String> repeatTypeCombo;
    private JComboBox<String> dayCombo;
    private JSpinner dateSpinner;
    private JPanel repeatPanel;
    private JPanel dayPanel;
    private JPanel datePanel;
    private JSpinner.DateEditor fullEditor;
    private JSpinner.DateEditor timeEditor;
    
    // Store the selected date separately for repeating tasks
    private Date selectedStartDate;

    public TaskFrame() {
        setTitle("Add New Task");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(createMainPanel(), BorderLayout.CENTER);
        add(createRightPanel(), BorderLayout.EAST);
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(240, 248, 255));

        JPanel card = new JPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(650, 750));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("+ ADD NEW TASK +");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(25, 25, 112));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // Task Name
        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180)), "Task Name"));
        taskField = new JTextField();
        taskField.setPreferredSize(new Dimension(400, 35));
        taskField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        taskPanel.add(taskField);

        // Priority Panel
        JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        priorityPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 140, 0)), "Priority"));
        
        highRB = createPriorityRadio("High", new Color(220, 20, 60));
        mediumRB = createPriorityRadio("Medium", new Color(255, 165, 0));
        lowRB = createPriorityRadio("Low", new Color(60, 179, 60));
        
        ButtonGroup priorityGroup = new ButtonGroup();
        priorityGroup.add(highRB); priorityGroup.add(mediumRB); priorityGroup.add(lowRB);
        mediumRB.setSelected(true);
        
        priorityPanel.add(highRB); priorityPanel.add(mediumRB); priorityPanel.add(lowRB);

        // Mode Panel
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        modePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(147, 112, 219)), "Category"));
        
        studyRB = new JRadioButton("Study");
        workRB = new JRadioButton("Work");
        personalRB = new JRadioButton("Personal");
        
        Font modeFont = new Font("Segoe UI", Font.PLAIN, 14);
        studyRB.setFont(modeFont); workRB.setFont(modeFont); personalRB.setFont(modeFont);
        
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(studyRB); modeGroup.add(workRB); modeGroup.add(personalRB);
        studyRB.setSelected(true);
        
        modePanel.add(studyRB); modePanel.add(workRB); modePanel.add(personalRB);

        // DateTime Panel
        JPanel dateTimePanel = new JPanel(new BorderLayout());
        dateTimePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 179, 113)), "Date & Time"));
        
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        dateTimeSpinner = new JSpinner(model);
        dateTimeSpinner.setPreferredSize(new Dimension(400, 35));
        
        fullEditor = new JSpinner.DateEditor(dateTimeSpinner, "dd/MM/yyyy HH:mm");
        timeEditor = new JSpinner.DateEditor(dateTimeSpinner, "HH:mm");
        
        dateTimeSpinner.setEditor(fullEditor);
        dateTimePanel.add(dateTimeSpinner);

        // Repeating Panel
        JPanel repeatingMainPanel = new JPanel(new BorderLayout());
        repeatingMainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(255, 105, 180)), "Repeating Options"));
        
        repeatingCheckBox = new JCheckBox("Enable Repeating Task");
        repeatingCheckBox.setFont(new Font("Segoe UI", Font.BOLD, 14));
        repeatingCheckBox.setForeground(new Color(199, 21, 133));
        
        // Repeat Type Panel
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Repeat: "));
        repeatTypeCombo = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"});
        repeatTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        repeatTypeCombo.setPreferredSize(new Dimension(150, 30));
        typePanel.add(repeatTypeCombo);
        
        // Day Selection Panel (for Weekly)
        dayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dayPanel.add(new JLabel("On: "));
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        dayCombo = new JComboBox<>(days);
        dayCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dayCombo.setPreferredSize(new Dimension(120, 30));
        dayPanel.add(dayCombo);
        dayPanel.setVisible(false);
        
        // Date Selection Panel (for Monthly)
        datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("On date: "));
        SpinnerNumberModel dateModel = new SpinnerNumberModel(1, 1, 31, 1);
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setPreferredSize(new Dimension(80, 30));
        datePanel.add(dateSpinner);
        datePanel.add(new JLabel("of month"));
        datePanel.setVisible(false);
        
        // Main repeat panel
        repeatPanel = new JPanel();
        repeatPanel.setLayout(new BoxLayout(repeatPanel, BoxLayout.Y_AXIS));
        repeatPanel.add(typePanel);
        repeatPanel.add(dayPanel);
        repeatPanel.add(datePanel);
        repeatPanel.setVisible(false);
        
        repeatingMainPanel.add(repeatingCheckBox, BorderLayout.NORTH);
        repeatingMainPanel.add(repeatPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        JButton addBtn = createButton("+ ADD TASK", new Color(46, 139, 87));
        JButton viewBtn = createButton("VIEW TASKS", new Color(70, 130, 180));
        buttonPanel.add(addBtn); buttonPanel.add(viewBtn);

        // Add all components
        gbc.gridy = 0; card.add(title, gbc);
        gbc.gridy = 1; card.add(taskPanel, gbc);
        gbc.gridy = 2; card.add(priorityPanel, gbc);
        gbc.gridy = 3; card.add(modePanel, gbc);
        gbc.gridy = 4; card.add(dateTimePanel, gbc);
        gbc.gridy = 5; card.add(repeatingMainPanel, gbc);
        gbc.gridy = 6; card.add(buttonPanel, gbc);

        wrapper.add(card);

        // Actions
        repeatingCheckBox.addActionListener(e -> {
            boolean selected = repeatingCheckBox.isSelected();
            repeatPanel.setVisible(selected);
            
            if (selected) {
                // Store the current date before switching to time editor
                selectedStartDate = (Date) dateTimeSpinner.getValue();
                dateTimeSpinner.setEditor(timeEditor);
                // Set the spinner to show only the time part of the stored date
                dateTimeSpinner.setValue(selectedStartDate);
            } else {
                dateTimeSpinner.setEditor(fullEditor);
                dateTimeSpinner.setValue(new Date());
            }
            updateRepeatPanels();
        });
        
        repeatTypeCombo.addActionListener(e -> updateRepeatPanels());
        
        addBtn.addActionListener(e -> addTask());
        viewBtn.addActionListener(e -> {
            dispose();
            new ViewTasksFrame();
        });

        return wrapper;
    }

    private void updateRepeatPanels() {
        String type = (String) repeatTypeCombo.getSelectedItem();
        dayPanel.setVisible("Weekly".equals(type));
        datePanel.setVisible("Monthly".equals(type));
    }

    private JRadioButton createPriorityRadio(String text, Color color) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rb.setForeground(color);
        return rb;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBackground(new Color(245, 250, 255));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel title = new JLabel("GUIDELINES");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(25, 25, 112));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font("Monospaced", Font.PLAIN, 14));
        text.setBackground(new Color(245, 250, 255));
        text.setMargin(new Insets(15, 15, 15, 15));

        text.setText(
            "PRIORITY GUIDE\n" +
            "─────────────────\n" +
            "[HIGH]    : < 24 hours\n" +
            "[MEDIUM]  : 2-15 days\n" +
            "[LOW]     : > 15 days\n\n" +
            
            "REPEATING TASKS\n" +
            "─────────────────\n" +
            "* Tasks repeat until deleted\n" +
            "* Daily: Same time every day\n" +
            "* Weekly: Choose day of week\n" +
            "* Monthly: Choose date of month\n" +
            "* The start date you set will be the first occurrence\n\n" +
            
            "TIME FORMAT\n" +
            "─────────────────\n" +
            "• Normal: Full date + time\n" +
            "• Repeating: Shows only time (date is preserved)\n" +
            "• Set the first occurrence date before enabling repeating"
        );

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(text), BorderLayout.CENTER);
        return panel;
    }

    private void addTask() {
        String name = taskField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter task name!");
            return;
        }
        if (DataStore.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(this, "User not logged in!");
            return;
        }

        String priority = highRB.isSelected() ? "High" : mediumRB.isSelected() ? "Medium" : "Low";
        String mode = studyRB.isSelected() ? "Study" : workRB.isSelected() ? "Work" : "Personal";
        
        Date selectedDate;
        if (repeatingCheckBox.isSelected() && selectedStartDate != null) {
            // For repeating tasks, use the stored start date but update the time from spinner
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(selectedStartDate);
            
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTime((Date) dateTimeSpinner.getValue());
            
            // Combine the date from selectedStartDate and time from spinner
            startCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            startCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            startCal.set(Calendar.SECOND, 0);
            
            selectedDate = startCal.getTime();
        } else {
            selectedDate = (Date) dateTimeSpinner.getValue();
        }
        
        // Store in database format
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String targetDate = dbFormat.format(selectedDate);
        String createdDateTime = dbFormat.format(new Date());

        boolean success;
        
        if (repeatingCheckBox.isSelected()) {
            String repeatType = ((String) repeatTypeCombo.getSelectedItem()).toLowerCase();
            String repeatDay = "weekly".equals(repeatType) ? (String) dayCombo.getSelectedItem() : null;
            int repeatDate = "monthly".equals(repeatType) ? (Integer) dateSpinner.getValue() : 0;
            
            Task task = new Task(name, targetDate, createdDateTime, priority, mode, false);
            task.setRepeating(true);
            task.setRepeatType(repeatType);
            task.setRepeatDay(repeatDay);
            task.setRepeatDate(repeatDate);
            task.setRepeatBaseDate(selectedDate);
            
            success = TaskDAO.addTask(task, DataStore.getCurrentUser().getId());
            
            if (success) {
                String info = "";
                if ("weekly".equals(repeatType)) info = " on " + repeatDay;
                else if ("monthly".equals(repeatType)) info = " on day " + repeatDate;
                
                JOptionPane.showMessageDialog(this, 
                    String.format("✓ Repeating task added!\n\n" +
                                  "Task: %s\n" +
                                  "Repeat: %s%s\n" +
                                  "First: %s\n" +
                                  "Time: %s\n\n" +
                                  "The task will repeat until deleted!", 
                                  name,
                                  repeatType, info,
                                  new SimpleDateFormat("dd/MM/yyyy").format(selectedDate),
                                  new SimpleDateFormat("HH:mm").format(selectedDate)));
            }
        } else {
            Task task = new Task(name, targetDate, createdDateTime, priority, mode, false);
            success = TaskDAO.addTask(task, DataStore.getCurrentUser().getId());
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    String.format("✓ Task Added Successfully!\n\n" +
                                  "Task: %s\n" +
                                  "Date: %s\n" +
                                  "Time: %s",
                                  name,
                                  new SimpleDateFormat("dd/MM/yyyy").format(selectedDate),
                                  new SimpleDateFormat("HH:mm").format(selectedDate)));
            }
        }

        if (success) {
            int choice = JOptionPane.showConfirmDialog(this, 
                "Task added successfully! Do you want to add another task?", 
                "Add Another?", 
                JOptionPane.YES_NO_OPTION);
            
            if (choice == JOptionPane.NO_OPTION) {
                dispose();
                new ViewTasksFrame();
            } else {
                resetForm();
            }
        } else {
            JOptionPane.showMessageDialog(this, "✗ Database Error! Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setFocusPainted(false);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        
        return btn;
    }

    private void resetForm() {
        taskField.setText("");
        mediumRB.setSelected(true);
        studyRB.setSelected(true);
        dateTimeSpinner.setValue(new Date());
        dateTimeSpinner.setEditor(fullEditor);
        repeatingCheckBox.setSelected(false);
        repeatPanel.setVisible(false);
        dayPanel.setVisible(false);
        datePanel.setVisible(false);
        selectedStartDate = null;
        
        repeatTypeCombo.setSelectedIndex(0);
        dayCombo.setSelectedIndex(0);
        dateSpinner.setValue(1);
        
        taskField.requestFocus();
    }

    private boolean isValidMonthlyDate(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return day <= maxDay;
    }
}