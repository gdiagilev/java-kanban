package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(int epicId, String name, String description, Status status,
                   LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() { return epicId; }

    @Override
    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }
}