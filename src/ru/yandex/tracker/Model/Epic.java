package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(name, description, status, startTime, duration);
    }

    public List<Subtask> getSubtasks() { return subtasks; }

    public void addSubtask(Subtask subtask) { subtasks.add(subtask); }
    public void removeSubtask(Subtask subtask) { subtasks.remove(subtask); }
}