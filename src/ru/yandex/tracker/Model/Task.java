package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Task {

    private static int idGenerator = 1;

    private int id;
    private String name;
    private String description;
    private Status status;

    protected LocalDateTime startTime;
    protected Duration duration;
    protected LocalDateTime endTime;

    public Task(String name, String description, Status status) {
        this.id = idGenerator++;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description, Status status,
                LocalDateTime startTime, Duration duration) {

        this(name, description, status);
        this.startTime = startTime;
        this.duration = duration;

        updateEndTime();
    }

    public Task(String name, String description, int id, Status status,
                LocalDateTime startTime, Duration duration) {

        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;

        if (id >= idGenerator) {
            idGenerator = id + 1;
        }

        updateEndTime();
    }

    private void updateEndTime() {
        if (startTime == null || duration == null) {
            endTime = null;
        } else {
            endTime = startTime.plus(duration);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id >= idGenerator) {
            idGenerator = id + 1;
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        updateEndTime();
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
        updateEndTime();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    protected void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", endTime=" + endTime +
                '}';
    }
}