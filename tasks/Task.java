package tasks;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Task {

    private int id;
    private String name;
    private String targetDate;        // Stored as yyyy-MM-dd HH:mm:ss in DB
    private String createdDateTime;   // Stored as yyyy-MM-dd HH:mm:ss in DB
    private String priority;
    private String mode;
    private boolean completed;
    
    // Fields for repeating tasks
    private boolean isRepeating;
    private String repeatType; // "daily", "weekly", "monthly"
    private String repeatDay; // for weekly: day of week
    private int repeatDate; // for monthly: day of month
    private Date repeatBaseDate; // base date for calculations
    private boolean isDeleted;

    // Original constructor (non-repeating tasks)
    public Task(String name, String targetDate,
                String createdDateTime,
                String priority, String mode,
                boolean completed) {

        this.name = name;
        this.targetDate = targetDate;
        this.createdDateTime = createdDateTime;
        this.priority = priority;
        this.mode = mode;
        this.completed = completed;
        this.isRepeating = false;
        this.repeatType = null;
        this.repeatDay = null;
        this.repeatDate = 0;
        this.repeatBaseDate = null;
        this.isDeleted = false;
    }

    // Constructor for repeating tasks (infinite repeat)
    public Task(String name, String targetDate,
                String createdDateTime,
                String priority, String mode,
                boolean completed,
                boolean isRepeating, String repeatType,
                String repeatDay, int repeatDate,
                Date repeatBaseDate) {

        this.name = name;
        this.targetDate = targetDate;
        this.createdDateTime = createdDateTime;
        this.priority = priority;
        this.mode = mode;
        this.completed = completed;
        this.isRepeating = isRepeating;
        this.repeatType = repeatType;
        this.repeatDay = repeatDay;
        this.repeatDate = repeatDate;
        this.repeatBaseDate = repeatBaseDate;
        this.isDeleted = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public String getRepeatDay() {
        return repeatDay;
    }

    public void setRepeatDay(String repeatDay) {
        this.repeatDay = repeatDay;
    }

    public int getRepeatDate() {
        return repeatDate;
    }

    public void setRepeatDate(int repeatDate) {
        this.repeatDate = repeatDate;
    }

    public Date getRepeatBaseDate() {
        return repeatBaseDate;
    }

    public void setRepeatBaseDate(Date repeatBaseDate) {
        this.repeatBaseDate = repeatBaseDate;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    // Get display text for repeat column
    public String getRepeatDisplayText() {
        if (!isRepeating) return "NO";
        
        switch(repeatType) {
            case "daily":
                return "Daily";
            case "weekly":
                return "Weekly (" + repeatDay + ")";
            case "monthly":
                return "Monthly (day " + repeatDate + ")";
            default:
                return "YES";
        }
    }

    // Get due time display text
    public String getDueTimeDisplay() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(targetDate);
            
            if (isRepeating) {
                // For repeating tasks, show only the time part
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                return timeFormat.format(date);
            } else {
                // For non-repeating tasks, show full date and time
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                return displayFormat.format(date);
            }
        } catch (Exception e) {
            return targetDate;
        }
    }

    // Get formatted created date
    public String getFormattedCreatedDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sdf.parse(createdDateTime);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return displayFormat.format(date);
        } catch (Exception e) {
            return createdDateTime;
        }
    }

    // Calculate next occurrence date
    public Date calculateNextOccurrence() {
        if (!isRepeating) return null;
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currentDate = sdf.parse(targetDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            
            switch(repeatType) {
                case "daily":
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                    
                case "weekly":
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    if (repeatDay != null) {
                        int targetDay = getDayOfWeekIndex(repeatDay);
                        cal.set(Calendar.DAY_OF_WEEK, targetDay);
                    }
                    break;
                    
                case "monthly":
                    cal.add(Calendar.MONTH, 1);
                    if (repeatDate > 0) {
                        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                        cal.set(Calendar.DAY_OF_MONTH, Math.min(repeatDate, maxDay));
                    }
                    break;
            }
            
            return cal.getTime();
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Move to next occurrence
    public void moveToNextOccurrence() {
        Date nextDate = calculateNextOccurrence();
        if (nextDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.targetDate = sdf.format(nextDate);
            this.completed = false;
        }
    }

    private int getDayOfWeekIndex(String day) {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task{")
          .append("id=").append(id)
          .append(", name='").append(name).append('\'')
          .append(", targetDate='").append(targetDate).append('\'')
          .append(", createdDateTime='").append(createdDateTime).append('\'')
          .append(", priority='").append(priority).append('\'')
          .append(", mode='").append(mode).append('\'')
          .append(", completed=").append(completed);
        
        if (isRepeating) {
            sb.append(", repeating=").append(isRepeating)
              .append(", type='").append(repeatType).append('\'');
            
            if ("weekly".equals(repeatType)) {
                sb.append(", day='").append(repeatDay).append('\'');
            } else if ("monthly".equals(repeatType)) {
                sb.append(", date=").append(repeatDate);
            }
        }
        
        sb.append(", deleted=").append(isDeleted);
        sb.append('}');
        return sb.toString();
    }
}