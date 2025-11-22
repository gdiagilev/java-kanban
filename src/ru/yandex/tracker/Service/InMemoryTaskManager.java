package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.time.Duration;
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
        return new ArrayList<>(epic.getSubtasks());
    }

    @Override
    public void createTask(Task task) {
        checkTimeIntersection(task);
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
        checkTimeIntersection(subtask);
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic == null) throw new NotFoundException("Эпик не найден");
        subtask.setId(generatorId++);
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatusAndTime(epic);
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
        if (subtask == null || !subtasks.containsKey(subtask.getId())) throw new NotFoundException("Подзадача не найдена");
        subtasks.remove(subtask.getId());
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().remove(subtask);
            updateEpicStatusAndTime(epic);
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
        for (Task t : tasks.values()) history.remove(t.getId());
        tasks.clear();
    }

    @Override
    public void deleteAllEpicTasks() {
        for (Epic e : epicTasks.values()) {
            for (Subtask s : e.getSubtasks()) history.remove(s.getId());
            history.remove(e.getId());
        }
        epicTasks.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask s : subtasks.values()) history.remove(s.getId());
        subtasks.clear();
        for (Epic e : epicTasks.values()) updateEpicStatusAndTime(e);
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
        updateEpicStatusAndTime(epicTask);
    }

    @Override
    public void updateSubtask(Subtask subtask) throws NotFoundException {
        if (!subtasks.containsKey(subtask.getId())) throw new NotFoundException("Подзадача не найдена");
        checkTimeIntersection(subtask);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epicTasks.get(subtask.getEpicId());
        if (epic != null) updateEpicStatusAndTime(epic);
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
        allTasks.sort(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return allTasks;
    }

    void updateEpicStatusAndTime(Epic epic) {
        List<Subtask> subs = epic.getSubtasks();
        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setStartTime(null);
            epic.setDuration(null);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;
        LocalDateTime start = null;
        LocalDateTime end = null;

        for (Subtask sub : subs) {
            if (sub.getStatus() != Status.NEW) allNew = false;
            if (sub.getStatus() != Status.DONE) allDone = false;

            if (sub.getStartTime() != null && sub.getDuration() != null) {
                LocalDateTime subStart = sub.getStartTime();
                LocalDateTime subEnd = subStart.plus(sub.getDuration());

                if (start == null || subStart.isBefore(start)) start = subStart;
                if (end == null || subEnd.isAfter(end)) end = subEnd;
            }
        }

        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);

        if (start != null && end != null) {
            epic.setStartTime(start);
            epic.setDuration(Duration.between(start, end));
        } else {
            epic.setStartTime(null);
            epic.setDuration(null);
        }
    }

    private void checkTimeIntersection(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) return;

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newStart.plus(newTask.getDuration());

        List<Task> tasksToCheck = new ArrayList<>();
        tasksToCheck.addAll(tasks.values());
        tasksToCheck.addAll(subtasks.values());

        for (Task task : tasksToCheck) {
            if (task.getId() == newTask.getId()) continue;
            if (task.getStartTime() == null || task.getDuration() == null) continue;

            LocalDateTime start = task.getStartTime();
            LocalDateTime end = start.plus(task.getDuration());

            if (!newEnd.isBefore(start) && !newStart.isAfter(end)) {
                throw new IllegalArgumentException(
                        "Задачи пересекаются по времени: " + newTask.getName() + " и " + task.getName()
                );
            }
        }
    }
}