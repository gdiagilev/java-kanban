package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epicTasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager history;
    protected int generatorId = 1;

    public InMemoryTaskManager() {
        history = Managers.getDefaultHistory();
    }

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    private boolean isTasksOverlap(Task t1, Task t2) {
        if (t1.getStartTime() == null || t1.getEndTime() == null ||
                t2.getStartTime() == null || t2.getEndTime() == null) {
            return false;
        }

        return t1.getStartTime().isBefore(t2.getEndTime()) &&
                t2.getStartTime().isBefore(t1.getEndTime());
    }

    private boolean isOverlapping(Task newTask) {
        return prioritizedTasks.stream()
                .anyMatch(existing -> isTasksOverlap(existing, newTask));
    }

    @Override
    public void createTask(Task task) {
        if (isOverlapping(task)) {
            throw new IllegalArgumentException("Ошибка: новая задача пересекается по времени с другой задачей!");
        }
        task.setId(generatorId++);
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
    }

    @Override
    public void createEpicTask(Epic epicTask) {
        epicTask.setId(generatorId++);
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void createSubtask(Subtask subTask) {
        if (isOverlapping(subTask)) {
            throw new IllegalArgumentException("Ошибка: подзадача пересекается по времени с другой задачей!");
        }
        Optional.ofNullable(epicTasks.get(subTask.getEpicId()))
                .ifPresent(epic -> {
                    subTask.setId(generatorId++);
                    epic.addSubtask(subTask);
                    subtasks.put(subTask.getId(), subTask);
                    autoSetEpicStatus(epic.getId());
                    addToPrioritizedTasks(subTask);
                });
    }

    @Override
    public Task getTask(int id) {
        return Optional.ofNullable(tasks.get(id))
                .map(task -> {
                    history.add(task);
                    return task;
                })
                .orElse(null);
    }

    @Override
    public Epic getEpicTask(int id) {
        return Optional.ofNullable(epicTasks.get(id))
                .map(epic -> {
                    history.add(epic);
                    return epic;
                })
                .orElse(null);
    }

    @Override
    public Subtask getSubtask(int id) {
        return Optional.ofNullable(subtasks.get(id))
                .map(sub -> {
                    history.add(sub);
                    return sub;
                })
                .orElse(null);
    }

    @Override
    public List<Task> getAllTasks() {
        return tasks.values().stream().toList();
    }

    @Override
    public List<Epic> getAllEpicTasks() {
        return epicTasks.values().stream().toList();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return subtasks.values().stream().toList();
    }

    @Override
    public List<Subtask> getAllSubTasksByEpicId(int epicId) {
        return Optional.ofNullable(epicTasks.get(epicId))
                .map(Epic::getSubtasks)
                .orElseGet(Collections::emptyList)
                .stream()
                .toList();
    }

    @Override
    public void updateTask(Task task) {
        if (isOverlapping(task)) {
            throw new IllegalArgumentException("Ошибка: обновлённая задача пересекается по времени!");
        }
        Optional.ofNullable(tasks.get(task.getId()))
                .ifPresent(t -> tasks.put(task.getId(), task));

        addToPrioritizedTasks(task);
    }

    @Override
    public void updateEpicTask(Epic epicTask) {
        Optional.ofNullable(epicTasks.get(epicTask.getId()))
                .ifPresent(existing -> {
                    existing.setName(epicTask.getName());
                    existing.setDescription(epicTask.getDescription());
                });
    }

    @Override
    public void updateSubtask(Subtask subTask) {
        if (isOverlapping(subTask)) {
            throw new IllegalArgumentException("Ошибка: обновлённая подзадача пересекается по времени!");
        }
        Optional.ofNullable(subtasks.get(subTask.getId()))
                .ifPresent(existing -> {
                    existing.setName(subTask.getName());
                    existing.setDescription(subTask.getDescription());
                    existing.setStatus(subTask.getStatus());
                    autoSetEpicStatus(subTask.getEpicId());
                    addToPrioritizedTasks(subTask);
                });
    }

    @Override
    public void deleteTask(Task task) {
        Optional.ofNullable(task).ifPresent(t -> {
            tasks.remove(t.getId());
            history.remove(t.getId());
            removeFromPrioritizedTasks(t);
        });
    }

    @Override
    public void deleteEpicTask(Epic epicTask) {
        Optional.ofNullable(epicTasks.remove(epicTask.getId()))
                .ifPresent(epic -> {
                    epic.getSubtasks().forEach(sub -> {
                        subtasks.remove(sub.getId());
                        history.remove(sub.getId());
                    });
                    history.remove(epic.getId());
                });
    }

    @Override
    public void deleteSubtask(Subtask subTask) {
        Optional.ofNullable(subTask).ifPresent(s -> {
            deleteSubtask(s.getId());
            removeFromPrioritizedTasks(s);
        });
    }

    @Override
    public void deleteTask(int id) {
        Optional.ofNullable(tasks.remove(id)).ifPresent(t -> history.remove(id));
    }

    @Override
    public void deleteEpicTask(int id) {
        Optional.ofNullable(epicTasks.remove(id)).ifPresent(epic -> {
            epic.getSubtasks().forEach(s -> {
                subtasks.remove(s.getId());
                history.remove(s.getId());
            });
            history.remove(id);
        });
    }

    @Override
    public void deleteSubtask(int id) {
        Optional.ofNullable(subtasks.remove(id)).ifPresent(subtask -> {
            Optional.ofNullable(epicTasks.get(subtask.getEpicId()))
                    .ifPresent(epic -> {
                        epic.removeSubtask(subtask);
                        autoSetEpicStatus(epic.getId());
                    });
            history.remove(id);
        });
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().stream()
                .filter(task -> task.getTaskType() == TaskType.TASK)
                .map(Task::getId)
                .toList()
                .forEach(id -> {
                    history.remove(id);
                    tasks.remove(id);
                });
    }



    @Override
    public void deleteAllEpicTasks() {
        subtasks.values().stream()
                .map(Subtask::getId)
                .forEach(history::remove);

        new ArrayList<>(subtasks.keySet()).forEach(subtasks::remove);
        new ArrayList<>(epicTasks.keySet()).forEach(epicTasks::remove);
    }


    @Override
    public void deleteAllSubtasks() {
        subtasks.values().stream()
                .map(Subtask::getId)
                .forEach(history::remove);

        epicTasks.values().forEach(epic -> {
            epic.removeAllSubtasks();
            autoSetEpicStatus(epic.getId());
        });

        new ArrayList<>(subtasks.keySet()).forEach(subtasks::remove);
    }


    private void autoSetEpicStatus(int id) {
        Optional.ofNullable(epicTasks.get(id)).ifPresent(epic -> {
            List<Subtask> subs = epic.getSubtasks();

            if (subs.isEmpty()) {
                epic.setStatus(Status.NEW);
                return;
            }

            boolean allNew = subs.stream().allMatch(s -> s.getStatus() == Status.NEW);
            boolean allDone = subs.stream().allMatch(s -> s.getStatus() == Status.DONE);

            epic.setStatus(allNew ? Status.NEW :
                    allDone ? Status.DONE :
                            Status.IN_PROGRESS);
        });
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}