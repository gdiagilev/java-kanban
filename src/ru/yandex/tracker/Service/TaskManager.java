package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.util.List;

public interface TaskManager {

    Task getTask(int id) throws NotFoundException;

    Epic getEpicTask(int id) throws NotFoundException;

    Subtask getSubtask(int id) throws NotFoundException;

    List<Task> getAllTasks();

    List<Epic> getAllEpicTasks();

    List<Subtask> getAllSubtasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    void createTask(Task task);

    void createEpicTask(Epic epicTask);

    void createSubtask(Subtask subtask);

    void updateTask(Task task) throws NotFoundException;

    void updateEpicTask(Epic epicTask) throws NotFoundException;

    void updateSubtask(Subtask subtask) throws NotFoundException;

    void deleteTask(Task task) throws NotFoundException;

    void deleteEpicTask(Epic epicTask) throws NotFoundException;

    void deleteSubtask(Subtask subtask) throws NotFoundException;

    void deleteTask(int id) throws NotFoundException;

    void deleteEpicTask(int id) throws NotFoundException;

    void deleteSubtask(int id) throws NotFoundException;

    void deleteAllTasks();

    void deleteAllEpicTasks();

    void deleteAllSubtasks();

    List<Subtask> getAllSubTasksByEpicId(int epicId) throws NotFoundException;
}