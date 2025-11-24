package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW, null, null);
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        updateTime();
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        updateTime();
    }

    public void clearSubtasks() {
        subtasks.clear();
        setStatus(Status.NEW);
        setStartTime(null);
        setDuration(null);
    }

    public void updateTime() {
        if (subtasks.isEmpty()) {
            setStartTime(null);
            setDuration(null);
            return;
        }

        LocalDateTime earliest = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(t -> t != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latest = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (earliest != null && latest != null) {
            setStartTime(earliest);
            setDuration(Duration.between(earliest, latest));
        }
    }
}