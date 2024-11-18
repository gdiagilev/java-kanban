package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;

import java.util.List;

public interface TaskManager {
    Task getTask(int id);

    Epic getEpicTask(int id);

    Subtask getSubtask(int id);

    void createTask(Task task);

    void createEpicTask(Epic epicTask);

    void createSubtask(Subtask subtask);

    List<Subtask> getAllSubTasksByEpicId(int epicId);

    void deleteTask(Task task);

    void deleteEpicTask(Epic epicTask);

    void deleteSubtask(Subtask subTask);

    void deleteTask(int id);

    void deleteEpicTask(int id);

    void deleteSubtask(int id);

    void deleteAllTasks();

    void deleteAllEpicTasks();

    void deleteAllSubtasks();

    void updateTask(Task task);

    void updateEpicTask(Epic epicTask);

    void updateSubtask(Subtask subTask);

    List<Task> getAllTasks();

    List<Epic> getAllEpicTasks();

    List<Subtask> getAllSubTasks();

    List<Subtask> getAllSubtasks();

    List<Task> getHistory();
}