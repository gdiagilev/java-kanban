package ru.yandex.tracker.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status, null, null);
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        updateStatus();
        updateEpicTime();
    }

    public void removeSubtaskById(int subtaskId) {
        subtasks.removeIf(s -> s.getId() == subtaskId);
        updateStatus();
        updateEpicTime();
    }

    public void clearSubtasks() {
        subtasks.clear();
        updateStatus();
        updateEpicTime();
    }

    public void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask s : subtasks) {
            if (s.getStatus() != Status.NEW) allNew = false;
            if (s.getStatus() != Status.DONE) allDone = false;
        }

        if (allNew) setStatus(Status.NEW);
        else if (allDone) setStatus(Status.DONE);
        else setStatus(Status.IN_PROGRESS);
    }

    public void updateEpicTime() {
        if (subtasks.isEmpty()) {
            setStartTime(null);
            setEndTime(null);
            setDuration(Duration.ZERO);
            return;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        Duration totalDuration = Duration.ZERO;

        for (Subtask sub : subtasks) {
            if (sub.getStartTime() != null && sub.getDuration() != null) {
                LocalDateTime subStart = sub.getStartTime();
                LocalDateTime subEnd = subStart.plus(sub.getDuration());

                if (start == null || subStart.isBefore(start)) start = subStart;
                if (end == null || subEnd.isAfter(end)) end = subEnd;

                totalDuration = totalDuration.plus(sub.getDuration());
            }
        }

        setStartTime(start);
        setEndTime(end);
        setDuration(totalDuration);
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
                ", subtasks=" + subtasks +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                ", endTime=" + getEndTime() +
                '}';
    }
}