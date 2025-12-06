package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager extends TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private int nextId = 1;

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    @Override
    public Task createTask(Task task) {
        task.setId(nextId++);
        checkTimeOverlap(task);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic createEpicTask(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(nextId++);
        checkTimeOverlap(subtask);
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
            updateEpicTimeAndStatus(epic);
        }
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpicTask(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpicTasks() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return Collections.emptyList();
        return epic.getSubtasks();
    }


    @Override
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return null;
        checkTimeOverlap(task);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic updateEpicTask(Epic epic) {
        if (!epics.containsKey(epic.getId())) return null;
        epics.put(epic.getId(), epic);
        updateEpicTimeAndStatus(epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return null;
        checkTimeOverlap(subtask);
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) updateEpicTimeAndStatus(epic);
        return subtask;
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) prioritizedTasks.remove(task);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpicTask(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Subtask sub : new ArrayList<>(epic.getSubtasks())) {
                deleteSubtask(sub.getId());
            }
        }
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(subtask);
                updateEpicTimeAndStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        for (int id : new ArrayList<>(tasks.keySet())) deleteTask(id);
    }

    @Override
    public void deleteAllEpicTasks() {
        for (int id : new ArrayList<>(epics.keySet())) deleteEpicTask(id);
    }

    @Override
    public void deleteAllSubtasks() {
        for (int id : new ArrayList<>(subtasks.keySet())) deleteSubtask(id);
    }


    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }


    private void updateEpicTimeAndStatus(Epic epic) {
        List<Subtask> subs = epic.getSubtasks();
        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setStartTime(null);
            epic.setDuration(null);
            return;
        }

        boolean allNew = subs.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = subs.stream().allMatch(s -> s.getStatus() == Status.DONE);

        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);

        LocalDateTime start = subs.stream()
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = subs.stream()
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Duration duration = subs.stream()
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);

        epic.setStartTime(start);
        epic.setEndTime(end);
        epic.setDuration(duration);
    }

    private void checkTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) return;
        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        for (Task task : prioritizedTasks) {
            if (task.getStartTime() == null || task.getId() == newTask.getId()) continue;
            LocalDateTime start = task.getStartTime();
            LocalDateTime end = task.getEndTime();

            boolean overlap = !newEnd.isBefore(start) && !newStart.isAfter(end);
            if (overlap) throw new IllegalArgumentException("Tasks time overlap");
        }
    }
}