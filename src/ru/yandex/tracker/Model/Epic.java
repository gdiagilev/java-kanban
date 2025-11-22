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

    public void removeSubtask(int subtaskId) {
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
            setDuration(null);
            return;
        }

        LocalDateTime start = null;
        LocalDateTime end = null;
        Duration totalDuration = Duration.ZERO;

        for (Subtask s : subtasks) {
            if (s.getStartTime() != null && s.getDuration() != null) {
                LocalDateTime subStart = s.getStartTime();
                LocalDateTime subEnd = subStart.plus(s.getDuration());

                if (start == null || subStart.isBefore(start)) start = subStart;
                if (end == null || subEnd.isAfter(end)) end = subEnd;

                totalDuration = totalDuration.plus(s.getDuration());
            }
        }

        setStartTime(start);
        setDuration(totalDuration);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }
}