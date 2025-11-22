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

        Duration totalDuration = Duration.ZERO;

        for (Subtask s : subtasks) {
            if (s.getStatus() != Status.NEW) allNew = false;
            if (s.getStatus() != Status.DONE) allDone = false;

            if (s.getStartTime() != null && s.getDuration() != null) {
                LocalDateTime sStart = s.getStartTime();
                LocalDateTime sEnd = s.getEndTime();

                if (start == null || sStart.isBefore(start)) start = sStart;
                if (end == null || sEnd.isAfter(end)) end = sEnd;

                totalDuration = totalDuration.plus(s.getDuration());
            }
        }

        if (allNew) setStatus(Status.NEW);
        else if (allDone) setStatus(Status.DONE);
        else setStatus(Status.IN_PROGRESS);

        setStartTime(start);
        if (start != null && end != null) setDuration(Duration.between(start, end));
        else setDuration(null);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return String.format("Epic{id=%d, name='%s', status=%s, start=%s, duration=%s, end=%s, subtasks=%s}",
                getId(), getName(), getStatus(), getStartTime(), getDuration(), getEndTime(), subtasks);
    }
}