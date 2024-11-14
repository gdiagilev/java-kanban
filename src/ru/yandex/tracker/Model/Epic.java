package ru.yandex.tracker.Model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask task) {
        if (!subtasks.contains(task)) {
            subtasks.add(task);
        }
    }

    public void removeSubtask(int task) {
        if (subtasks != null) {
            if (subtasks.contains(task)) {
                subtasks.remove(task);
            }
        }
    }

    public void removeAllSubtasks() {
        if (subtasks != null) {
            subtasks.clear();
        }
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public Subtask getSubtask(int id) {
        Subtask subtask = null;
        for (int i = 0; i < subtasks.size(); i++) {
            if (subtasks.get(i).getId() == id) {
                subtask = subtasks.get(i);
                break;
            }
        }
        return subtask;
    }

    @Override
    public String toString() {
        return "java.yandex.practicum.Model.Epic{" + "\n" +
                "id=" + getId() + "\n" +
                "status=" + getStatus() + "\n" +
                "name='" + getName() + "\n" +
                "description='" + getDescription() + "\n" +
                "subtask=" + subtasks + "\n" +
                "}";
    }
}
