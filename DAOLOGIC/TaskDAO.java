package DAOLOGIC;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import tasks.Task;

public class TaskDAO {

    // ================= ADD TASK =================
    public static boolean addTask(Task t, int userId) {
        String sql = "INSERT INTO tasks " +
                "(name, target_date, created_date, priority, mode, completed, " +
                "is_repeating, repeat_type, repeat_day, repeat_date, repeat_base_date, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getName());
            ps.setString(2, t.getTargetDate());
            ps.setString(3, t.getCreatedDateTime());
            ps.setString(4, t.getPriority());
            ps.setString(5, t.getMode());
            ps.setBoolean(6, t.isCompleted());
            ps.setBoolean(7, t.isRepeating());
            ps.setString(8, t.getRepeatType());
            ps.setString(9, t.getRepeatDay());
            ps.setInt(10, t.getRepeatDate());
            
            // Handle repeat base date
            if (t.getRepeatBaseDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                ps.setString(11, sdf.format(t.getRepeatBaseDate()));
            } else {
                ps.setNull(11, Types.VARCHAR);
            }
            
            ps.setInt(12, userId);

            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET TASKS =================
    public static List<Task> getTasksByUser(int userId) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id=? AND (is_deleted=false OR is_deleted IS NULL) ORDER BY target_date ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Task t = extractTaskFromResultSet(rs);
                if (t != null) {
                    list.add(t);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= GET TASKS BY DATE RANGE =================
    public static List<Task> getTasksByDateRange(int userId, Date startDate, Date endDate) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id=? AND target_date BETWEEN ? AND ? AND is_deleted=false ORDER BY target_date ASC";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ps.setInt(1, userId);
            ps.setString(2, sdf.format(startDate));
            ps.setString(3, sdf.format(endDate));
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Task t = extractTaskFromResultSet(rs);
                if (t != null) {
                    list.add(t);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }

    // ================= GET TASKS BY SPECIFIC DATE =================
    public static List<Task> getTasksByDate(int userId, Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endDate = cal.getTime();
        
        return getTasksByDateRange(userId, startDate, endDate);
    }

    // ================= GET TASKS FOR CURRENT WEEK =================
    public static List<Task> getTasksForCurrentWeek(int userId) {
        Calendar cal = Calendar.getInstance();
        
        // Set to first day of week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date weekStart = cal.getTime();
        
        // Set to last day of week (Sunday)
        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date weekEnd = cal.getTime();
        
        return getTasksByDateRange(userId, weekStart, weekEnd);
    }

    // ================= GET COMPLETED TASKS COUNT FOR DATE =================
    public static int getCompletedTasksCountForDate(int userId, Date date) {
        List<Task> tasks = getTasksByDate(userId, date);
        int count = 0;
        for (Task t : tasks) {
            if (t.isCompleted() && !t.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    // ================= GET PENDING TASKS COUNT FOR DATE =================
    public static int getPendingTasksCountForDate(int userId, Date date) {
        List<Task> tasks = getTasksByDate(userId, date);
        int count = 0;
        for (Task t : tasks) {
            if (!t.isCompleted() && !t.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    // ================= GET COMPLETED TASKS COUNT FOR WEEK =================
    public static int getCompletedTasksCountForWeek(int userId) {
        List<Task> tasks = getTasksForCurrentWeek(userId);
        int count = 0;
        for (Task t : tasks) {
            if (t.isCompleted() && !t.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    // ================= GET PENDING TASKS COUNT FOR WEEK =================
    public static int getPendingTasksCountForWeek(int userId) {
        List<Task> tasks = getTasksForCurrentWeek(userId);
        int count = 0;
        for (Task t : tasks) {
            if (!t.isCompleted() && !t.isDeleted()) {
                count++;
            }
        }
        return count;
    }

    // ================= GET DAILY PROGRESS PERCENTAGE =================
    public static int getDailyProgressPercentage(int userId, Date date) {
        List<Task> tasks = getTasksByDate(userId, date);
        int total = tasks.size();
        if (total == 0) return 0;
        
        int completed = 0;
        for (Task t : tasks) {
            if (t.isCompleted() && !t.isDeleted()) {
                completed++;
            }
        }
        return (completed * 100) / total;
    }

    // ================= GET WEEKLY PROGRESS PERCENTAGE =================
    public static int getWeeklyProgressPercentage(int userId) {
        List<Task> tasks = getTasksForCurrentWeek(userId);
        int total = tasks.size();
        if (total == 0) return 0;
        
        int completed = 0;
        for (Task t : tasks) {
            if (t.isCompleted() && !t.isDeleted()) {
                completed++;
            }
        }
        return (completed * 100) / total;
    }

    // ================= GET RECENTLY COMPLETED TASKS =================
    public static List<Task> getRecentlyCompletedTasks(int userId, int limit) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id=? AND completed=true AND is_deleted=false ORDER BY target_date DESC LIMIT ?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Task t = extractTaskFromResultSet(rs);
                if (t != null) {
                    list.add(t);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }

    // ================= GET TASK STATISTICS FOR DATE RANGE =================
    public static Map<String, Integer> getTaskStatistics(int userId, Date startDate, Date endDate) {
        Map<String, Integer> stats = new HashMap<>();
        List<Task> tasks = getTasksByDateRange(userId, startDate, endDate);
        
        int total = 0;
        int completed = 0;
        int pending = 0;
        int highPriority = 0;
        int mediumPriority = 0;
        int lowPriority = 0;
        
        for (Task t : tasks) {
            if (t.isDeleted()) continue;
            
            total++;
            if (t.isCompleted()) {
                completed++;
            } else {
                pending++;
            }
            
            switch (t.getPriority().toLowerCase()) {
                case "high":
                    highPriority++;
                    break;
                case "medium":
                    mediumPriority++;
                    break;
                case "low":
                    lowPriority++;
                    break;
            }
        }
        
        stats.put("total", total);
        stats.put("completed", completed);
        stats.put("pending", pending);
        stats.put("highPriority", highPriority);
        stats.put("mediumPriority", mediumPriority);
        stats.put("lowPriority", lowPriority);
        
        return stats;
    }

    // ================= COMPLETE REPEATING TASK =================
    public static boolean completeRepeatingTask(int taskId, int userId, Date nextDueDate) {
        String sql = "UPDATE tasks SET completed=?, target_date=? WHERE id=? AND user_id=?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            ps.setBoolean(1, true); // Mark as completed for current cycle
            ps.setString(2, sdf.format(nextDueDate)); // Set next occurrence date
            ps.setInt(3, taskId);
            ps.setInt(4, userId);
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= ACTIVATE RECURRING TASK =================
    public static boolean activateRecurringTask(int taskId, int userId) {
        String sql = "UPDATE tasks SET completed=? WHERE id=? AND user_id=? AND is_repeating=?";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setBoolean(1, false); // Mark as pending
            ps.setInt(2, taskId);
            ps.setInt(3, userId);
            ps.setBoolean(4, true); // Only for repeating tasks
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= COMPLETE ONE-TIME TASK =================
    public static boolean completeOneTimeTask(int taskId, int userId) {
        String sql = "UPDATE tasks SET completed=? WHERE id=? AND user_id=? AND (is_repeating=false OR is_repeating IS NULL)";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setBoolean(1, true); // Permanently mark as completed
            ps.setInt(2, taskId);
            ps.setInt(3, userId);
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= COMPLETE TASK (Backward compatibility) =================
    public static boolean completeTask(int taskId, int userId) {
        Connection con = null;
        PreparedStatement selectPs = null;
        ResultSet rs = null;
        
        try {
            con = DBConnection.getConnection();
            
            // First, get the task details to check if it's repeating
            String selectSql = "SELECT * FROM tasks WHERE id=? AND user_id=?";
            selectPs = con.prepareStatement(selectSql);
            selectPs.setInt(1, taskId);
            selectPs.setInt(2, userId);
            rs = selectPs.executeQuery();
            
            if (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                
                if (task != null && task.isRepeating()) {
                    // For repeating tasks, calculate next occurrence
                    Date nextDate = calculateNextOccurrenceFromCompletion(task, new Date());
                    return completeRepeatingTask(taskId, userId, nextDate);
                } else {
                    // For non-repeating tasks
                    return completeOneTimeTask(taskId, userId);
                }
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (selectPs != null) selectPs.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= CALCULATE NEXT OCCURRENCE FROM COMPLETION =================
    private static Date calculateNextOccurrenceFromCompletion(Task task, Date completionDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(completionDate);
        
        String repeatType = task.getRepeatType();
        
        if ("daily".equals(repeatType)) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else if ("weekly".equals(repeatType)) {
            cal.add(Calendar.DAY_OF_MONTH, 7);
        } else if ("monthly".equals(repeatType)) {
            cal.add(Calendar.MONTH, 1);
            // Handle month-end dates properly
            if (task.getRepeatDate() > 0) {
                int targetDay = task.getRepeatDate();
                int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                cal.set(Calendar.DAY_OF_MONTH, Math.min(targetDay, maxDay));
            }
        }
        
        // Preserve the original time
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
        
        return cal.getTime();
    }

    // ================= DELETE TASK (soft delete) =================
    public static boolean deleteTask(int taskId, int userId) {
        String sql = "UPDATE tasks SET is_deleted=true WHERE id=? AND user_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= PERMANENT DELETE =================
    public static boolean permanentDeleteTask(int taskId, int userId) {
        String sql = "DELETE FROM tasks WHERE id=? AND user_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= CHECK AND ACTIVATE DUE RECURRING TASKS =================
    public static void checkAndActivateDueTasks(int userId) {
        String sql = "SELECT * FROM tasks WHERE user_id=? AND is_repeating=true AND completed=true AND is_deleted=false";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                Task task = extractTaskFromResultSet(rs);
                if (task != null) {
                    Date nextDue = sdf.parse(task.getTargetDate());
                    
                    // If next due date has arrived or passed, activate the task
                    if (!nextDue.after(now)) {
                        activateRecurringTask(task.getId(), userId);
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= GET PENDING TASKS COUNT =================
    public static int getPendingTasksCount(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id=? AND completed=false AND is_deleted=false";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    // ================= GET COMPLETED TASKS COUNT =================
    public static int getCompletedTasksCount(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id=? AND completed=true AND is_deleted=false";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    // ================= GET TOTAL TASKS COUNT =================
    public static int getTotalTasksCount(int userId) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE user_id=? AND is_deleted=false";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    // ================= HELPER METHOD: Extract Task from ResultSet =================
    private static Task extractTaskFromResultSet(ResultSet rs) throws SQLException {
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String targetDate = rs.getString("target_date");
            String createdDate = rs.getString("created_date");
            String priority = rs.getString("priority");
            String mode = rs.getString("mode");
            boolean completed = rs.getBoolean("completed");
            
            boolean isRepeating = false;
            String repeatType = null;
            String repeatDay = null;
            int repeatDate = 0;
            Date repeatBaseDate = null;
            boolean isDeleted = false;
            
            try {
                isRepeating = rs.getBoolean("is_repeating");
                repeatType = rs.getString("repeat_type");
                repeatDay = rs.getString("repeat_day");
                repeatDate = rs.getInt("repeat_date");
                
                String baseDateStr = rs.getString("repeat_base_date");
                if (baseDateStr != null && !baseDateStr.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    repeatBaseDate = sdf.parse(baseDateStr);
                }
                
                try {
                    isDeleted = rs.getBoolean("is_deleted");
                } catch (SQLException e) {
                    // is_deleted column might not exist
                }
                
            } catch (SQLException e) {
                // Columns don't exist yet - use defaults
            }

            Task task;
            if (isRepeating) {
                task = new Task(
                        name,
                        targetDate,
                        createdDate,
                        priority,
                        mode,
                        completed,
                        true,
                        repeatType,
                        repeatDay,
                        repeatDate,
                        repeatBaseDate
                );
            } else {
                task = new Task(
                        name,
                        targetDate,
                        createdDate,
                        priority,
                        mode,
                        completed
                );
            }
            
            task.setId(id);
            task.setDeleted(isDeleted);
            return task;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================= HELPER METHOD: Calculate next occurrence =================
    public static Date calculateNextOccurrence(Task task) {
        if (!task.isRepeating()) return null;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currentDate = sdf.parse(task.getTargetDate());
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            
            switch(task.getRepeatType()) {
                case "daily":
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                    
                case "weekly":
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    if (task.getRepeatDay() != null) {
                        int targetDay = getDayOfWeekIndex(task.getRepeatDay());
                        cal.set(Calendar.DAY_OF_WEEK, targetDay);
                    }
                    break;
                    
                case "monthly":
                    cal.add(Calendar.MONTH, 1);
                    if (task.getRepeatDate() > 0) {
                        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                        cal.set(Calendar.DAY_OF_MONTH, Math.min(task.getRepeatDate(), maxDay));
                    }
                    break;
            }
            
            return cal.getTime();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================= HELPER METHOD: Get day of week index =================
    private static int getDayOfWeekIndex(String day) {
        switch(day) {
            case "Sunday": return Calendar.SUNDAY;
            case "Monday": return Calendar.MONDAY;
            case "Tuesday": return Calendar.TUESDAY;
            case "Wednesday": return Calendar.WEDNESDAY;
            case "Thursday": return Calendar.THURSDAY;
            case "Friday": return Calendar.FRIDAY;
            case "Saturday": return Calendar.SATURDAY;
            default: return Calendar.MONDAY;
        }
    }
}