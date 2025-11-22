package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.time.LocalDateTime;
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
        if (task == null) throw new NotFoundException("Задача не найдена");
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicTask(int id) throws NotFoundException {
        Epic epic = epicTasks.get(id);
        if (epic == null) throw new NotFoundException("Эпик не найден");
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) throw new NotFoundException("Подзадача не найдена");
        history.add(subtask);
        return subtask;
    }

    @Override
    public List<Subtask> getAllSubTasksByEpicId(int epicId) throws NotFoundException {
        Epic epic = epicTasks.get(epicId);
        if (epic == null) throw new NotFoundException("Эпик не найден");
        return epic.getSubtasks();
    }

    @Override
    public void createTask(Task task) {
        checkTimeIntersection(task);
        task.setId(generatorId++);
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpicTask(Epic epic) {
        epic.setId(generatorId++);
        epicTasks.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        checkTimeIntersection(subtask);
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic == null) throw new NotFoundException("Эпик не найден");
        subtask.setId(generatorId++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
    }

    @Override
    public void deleteTask(Task task) throws NotFoundException {
        if (task == null || !tasks.containsKey(task.getId())) throw new NotFoundException("Задача не найдена");
        tasks.remove(task.getId());
        history.remove(task.getId());
    }

    @Override
    public void deleteEpicTask(Epic epicTask) throws NotFoundException {
        if (epicTask == null || !epicTasks.containsKey(epicTask.getId())) throw new NotFoundException("Эпик не найден");
        for (Subtask sub : epicTask.getSubtasks()) {
            subtasks.remove(sub.getId());
            history.remove(sub.getId());
        }
        epicTasks.remove(epicTask.getId());
        history.remove(epicTask.getId());
    }

    @Override
    public void deleteSubtask(Subtask subtask) throws NotFoundException {
        if (subtask == null || !subtasks.containsKey(subtask.getId()))
            throw new NotFoundException("Подзадача не найдена");
        subtasks.remove(subtask.getId());
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtask(subtask.getId());
        }
        history.remove(subtask.getId());
    }

    @Override
    public void deleteTask(int id) throws NotFoundException {
        deleteTask(tasks.get(id));
    }

    @Override
    public void deleteEpicTask(int id) throws NotFoundException {
        deleteEpicTask(epicTasks.get(id));
    }

    @Override
    public void deleteSubtask(int id) throws NotFoundException {
        deleteSubtask(subtasks.get(id));
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(t -> history.remove(t.getId()));
        tasks.clear();
    }

    @Override
    public void deleteAllEpicTasks() {
        epicTasks.values().forEach(e -> {
            e.getSubtasks().forEach(s -> history.remove(s.getId()));
            history.remove(e.getId());
        });
        epicTasks.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(s -> history.remove(s.getId()));
        subtasks.clear();
        epicTasks.values().forEach(Epic::updateEpicTime);
    }

    @Override
    public void updateTask(Task task) throws NotFoundException {
        if (!tasks.containsKey(task.getId())) throw new NotFoundException("Задача не найдена");
        checkTimeIntersection(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpicTask(Epic epicTask) throws NotFoundException {
        if (!epicTasks.containsKey(epicTask.getId())) throw new NotFoundException("Эпик не найден");
        epicTasks.put(epicTask.getId(), epicTask);
        epicTask.updateStatus();
        epicTask.updateEpicTime();
    }

    @Override
    public void updateSubtask(Subtask subtask) throws NotFoundException {
        if (!subtasks.containsKey(subtask.getId())) throw new NotFoundException("Подзадача не найдена");
        checkTimeIntersection(subtask);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic != null) {
            epic.updateStatus();
            epic.updateEpicTime();
        }
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
        List<Task> all = new ArrayList<>();
        all.addAll(tasks.values());
        all.addAll(subtasks.values());
        all.addAll(epicTasks.values());
        all.sort(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return all;
    }

    private void checkTimeIntersection(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) return;
        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newStart.plus(newTask.getDuration());

        for (Task task : getAllTasks()) {
            if (task.getId() == newTask.getId()) continue;
            if (task.getStartTime() == null || task.getDuration() == null) continue;
            LocalDateTime start = task.getStartTime();
            LocalDateTime end = start.plus(task.getDuration());

            if (!newEnd.isBefore(start) && !newStart.isAfter(end)) {
                throw new IllegalArgumentException(
                        "Задачи пересекаются по времени: " + newTask.getName() + " и " + task.getName());
            }
        }
    }
}