package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.util.List;

public interface TaskManager {

    Task getTask(int id) throws NotFoundException;

    Epic getEpicTask(int id) throws NotFoundException;

    Subtask getSubtask(int id) throws NotFoundException;

    void createTask(Task task);

    void createEpicTask(Epic epicTask);

    void createSubtask(Subtask subtask);

    List<Subtask> getAllSubTasksByEpicId(int epicId) throws NotFoundException;

    void deleteTask(Task task) throws NotFoundException;

    void deleteEpicTask(Epic epicTask) throws NotFoundException;

    void deleteSubtask(Subtask subTask) throws NotFoundException;

    void deleteTask(int id) throws NotFoundException;

    void deleteEpicTask(int id) throws NotFoundException;

    void deleteSubtask(int id) throws NotFoundException;

    void deleteAllTasks();

    void deleteAllEpicTasks();

    void deleteAllSubtasks();

    void updateTask(Task task) throws NotFoundException;

    void updateEpicTask(Epic epicTask) throws NotFoundException;

    void updateSubtask(Subtask subTask) throws NotFoundException;

    List<Task> getAllTasks();

    List<Epic> getAllEpicTasks();

    List<Subtask> getAllSubtasks();

    List<Task> getHistory();

    Object getPrioritizedTasks();
}