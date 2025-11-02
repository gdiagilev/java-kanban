package ru.yandex.tracker.Model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Subtask> subtasks;

    // Для создания нового эпика
    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtasks = new ArrayList<>();
    }

    // Для восстановления из файла
    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask task) {
        if (task != null && !subtasks.contains(task)) {
            subtasks.add(task);
        }
    }

    public void removeSubtask(Subtask task) {
        subtasks.remove(task);
    }

    public void removeSubtaskById(int id) {
        subtasks.removeIf(s -> s.getId() == id);
    }

    public void removeAllSubtasks() {
        subtasks.clear();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public Subtask getSubtask(int id) {
        for (Subtask subtask : subtasks) {
            if (subtask.getId() == id) {
                return subtask;
            }
        }
        return null;
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
                '}';
    }
}
