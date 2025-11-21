package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.duration = Duration.ZERO;
    }

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status);
        this.duration = Duration.ZERO;
    }

    public void addSubtask(Subtask subtask) {
        if (subtask != null && !subtasks.contains(subtask)) {
            subtasks.add(subtask);
            updateEpicStatus();
            updateEpicTime();
        }
    }

    public void removeSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasks.remove(subtask);
            updateEpicStatus();
            updateEpicTime();
        }
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        updateEpicStatus();
        updateEpicTime();
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void updateEpicTime() {
        if (subtasks.isEmpty()) {
            this.startTime = null;
            this.endTime = null;
            this.duration = Duration.ZERO;
            return;
        }

        this.startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(t -> t != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        this.endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(t -> t != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        this.duration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(d -> d != null)
                .reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateEpicStatus() {
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            return;
        }

        boolean allNew = subtasks.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = subtasks.stream().allMatch(s -> s.getStatus() == Status.DONE);

        if (allNew) {
            setStatus(Status.NEW);
        } else if (allDone) {
            setStatus(Status.DONE);
        } else {
            setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                ", description='" + getDescription() + '\'' +
                ", subtasksCount=" + subtasks.size() +
                ", startTime=" + startTime +
                ", duration=" + duration +
                ", endTime=" + endTime +
                '}';
    }
}