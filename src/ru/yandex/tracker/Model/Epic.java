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
        updateStatusAndTime();
    }

    public void removeSubtask(int subtaskId) {
        subtasks.removeIf(s -> s.getId() == subtaskId);
        updateStatusAndTime();
    }

    public void clearSubtasks() {
        subtasks.clear();
        updateStatusAndTime();
    }

    public void updateStatusAndTime() {
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            setStartTime(null);
            setDuration(null);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;
        LocalDateTime start = null;
        LocalDateTime end = null;

        for (Subtask sub : subtasks) {
            if (sub.getStatus() != Status.NEW) allNew = false;
            if (sub.getStatus() != Status.DONE) allDone = false;

            if (sub.getStartTime() != null && sub.getDuration() != null) {
                LocalDateTime subStart = sub.getStartTime();
                LocalDateTime subEnd = subStart.plus(sub.getDuration());

                if (start == null || subStart.isBefore(start)) start = subStart;
                if (end == null || subEnd.isAfter(end)) end = subEnd;
            }
        }

        if (allNew) setStatus(Status.NEW);
        else if (allDone) setStatus(Status.DONE);
        else setStatus(Status.IN_PROGRESS);

        if (start != null && end != null) {
            setStartTime(start);
            setDuration(Duration.between(start, end));
        } else {
            setStartTime(null);
            setDuration(null);
        }
    }

    public void updateStatus() {
        updateStatusAndTime();
    }

    public void updateEpicTime() {
        updateStatusAndTime();
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }
}