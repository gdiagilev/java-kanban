package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId;

    public Subtask(int epicId, String name, String description, Status status,
                   LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(int id, int epicId, String name, String description, Status status,
                   LocalDateTime startTime, Duration duration) {
        super(name, description, id, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", epicId=" + epicId +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                ", endTime=" + getEndTime() +
                '}';
    }
}