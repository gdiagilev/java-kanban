package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    private int id;
    private String name;
    private String description;
    private Status status;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(String name, String description, Status status) {
        this(name, description, 0, status, null, null);
    }

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this(name, description, 0, status, startTime, duration);
    }

    public Task(String name, String description, int id, Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null && duration != null) return startTime.plus(duration);
        return null;
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, name='%s', status=%s, start=%s, duration=%s, end=%s}",
                id, name, status, startTime, duration, getEndTime());
    }
}