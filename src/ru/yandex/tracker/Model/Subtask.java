package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, Status status, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() { return epicId; }
    public void setEpicId(int epicId) { this.epicId = epicId; }
}