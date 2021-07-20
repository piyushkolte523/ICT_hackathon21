package com.example.chronology;

public class Tasks {
    private String taskName, taskDescriptionName, taskTimeName;

    public Tasks()
    {}

    public Tasks (String taskName, String taskDescriptionName, String taskTimeName) {
        this.taskName = taskName;
        this.taskDescriptionName = taskDescriptionName;
        this.taskTimeName = taskTimeName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName() {
        this.taskName = taskName;
    }

    public String getTaskDescriptionName() {
        return taskDescriptionName;
    }

    public void setTaskDescriptionName() {
        this.taskDescriptionName = taskDescriptionName;
    }

    public String getTaskTimeName() {
        return taskTimeName;
    }

    public void setTaskTimeName() {
        this.taskTimeName = taskTimeName;
    }


}
