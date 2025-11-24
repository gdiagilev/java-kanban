package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;

import java.util.List;

public abstract class TaskManager {

    public abstract Task createTask(Task task);
    public abstract Task updateTask(Task task);
    public abstract Task getTask(int id);
    public abstract List<Task> getAllTasks();
    public abstract void deleteTask(int id);
    public abstract void deleteAllTasks();

    public abstract Epic createEpicTask(Epic epic);
    public abstract Epic updateEpicTask(Epic epic);
    public abstract Epic getEpicTask(int id);
    public abstract List<Epic> getAllEpicTasks();
    public abstract void deleteEpicTask(int id);
    public abstract void deleteAllEpicTasks();

    public abstract Subtask createSubtask(Subtask subtask);
    public abstract Subtask updateSubtask(Subtask subtask);
    public abstract Subtask getSubtask(int id);
    public abstract List<Subtask> getAllSubtasks();
    public abstract List<Subtask> getSubtasksOfEpic(int epicId);
    public abstract void deleteSubtask(int id);
    public abstract void deleteAllSubtasks();

    public abstract List<Task> getPrioritizedTasks();

    public abstract List<Task> getHistory();
}