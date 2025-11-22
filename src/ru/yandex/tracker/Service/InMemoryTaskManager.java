package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epicTasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager history = new InMemoryHistoryManager();
    protected int generatorId = 1;

    @Override
    public Task getTask(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) throw new NotFoundException("Task not found");
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicTask(int id) throws NotFoundException {
        Epic epic = epicTasks.get(id);
        if (epic == null) throw new NotFoundException("Epic not found");
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) throws NotFoundException {
        Subtask sub = subtasks.get(id);
        if (sub == null) throw new NotFoundException("Subtask not found");
        history.add(sub);
        return sub;
    }

    @Override
    public void createTask(Task task) {
        task.setId(generatorId++);
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpicTask(Epic epicTask) {
        epicTask.setId(generatorId++);
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        subtask.setId(generatorId++);
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
        }
    }

    @Override
    public List<Subtask> getAllSubTasksByEpicId(int epicId) throws NotFoundException {
        Epic epic = epicTasks.get(epicId);
        if (epic == null) throw new NotFoundException("Epic not found");
        return epic.getSubtasks();
    }

    @Override
    public void deleteTask(Task task) throws NotFoundException {
        tasks.remove(task.getId());
        history.remove(task.getId());
    }

    @Override
    public void deleteEpicTask(Epic epicTask) throws NotFoundException {
        epicTasks.remove(epicTask.getId());
        history.remove(epicTask.getId());
        for (Subtask sub : epicTask.getSubtasks()) {
            subtasks.remove(sub.getId());
            history.remove(sub.getId());
        }
    }

    @Override
    public void deleteSubtask(Subtask subTask) throws NotFoundException {
        subtasks.remove(subTask.getId());
        history.remove(subTask.getId());
        Epic epic = epicTasks.get(subTask.getEpicId());
        if (epic != null) epic.removeSubtask(subTask.getId());
    }

    @Override
    public void deleteTask(int id) throws NotFoundException {
        Task task = tasks.get(id);
        if (task == null) throw new NotFoundException("Task not found");
        deleteTask(task);
    }

    @Override
    public void deleteEpicTask(int id) throws NotFoundException {
        Epic epic = epicTasks.get(id);
        if (epic == null) throw new NotFoundException("Epic not found");
        deleteEpicTask(epic);
    }

    @Override
    public void deleteSubtask(int id) throws NotFoundException {
        Subtask sub = subtasks.get(id);
        if (sub == null) throw new NotFoundException("Subtask not found");
        deleteSubtask(sub);
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpicTasks() {
        epicTasks.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epicTasks.values()) {
            epic.clearSubtasks();
        }
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpicTask(Epic epicTask) {
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void updateSubtask(Subtask subTask) {
        subtasks.put(subTask.getId(), subTask);
        Epic epic = epicTasks.get(subTask.getEpicId());
        if (epic != null) epic.updateStatus();
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpicTasks() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(subtasks.values());
        allTasks.addAll(epicTasks.values());
        allTasks.sort(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return allTasks;
    }
}