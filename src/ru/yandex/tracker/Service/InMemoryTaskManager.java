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
    public void createSubtask(Subtask subTask) {
        int epicId = subTask.getEpicId();
        if (epicTasks.containsKey(epicId)) {
            subTask.setId(generatorId++);
            Epic epic = epicTasks.get(epicId);
            epic.addSubtask(subTask);
            subtasks.put(subTask.getId(), subTask);
            autoSetEpicStatus(epicId);
        }
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) history.add(task);
        return task;
    }

    @Override
    public Epic getEpicTask(int id) {
        Epic epic = epicTasks.get(id);
        if (epic != null) history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) history.add(subtask);
        return subtask;
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
    public ArrayList<Subtask> getAllSubTasksByEpicId(int epicId) {
        Epic epic = epicTasks.get(epicId);
        return epic != null ? new ArrayList<>(epic.getSubtasks()) : new ArrayList<>();
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpicTask(Epic epicTask) {
        if (epicTasks.containsKey(epicTask.getId())) {
            Epic existing = epicTasks.get(epicTask.getId());
            existing.setName(epicTask.getName());
            existing.setDescription(epicTask.getDescription());
        }
    }

    @Override
    public void updateSubtask(Subtask subTask) {
        int id = subTask.getId();
        int epicId = subTask.getEpicId();
        if (subtasks.containsKey(id) && epicTasks.containsKey(epicId)) {
            Subtask existing = subtasks.get(id);
            existing.setName(subTask.getName());
            existing.setDescription(subTask.getDescription());
            existing.setStatus(subTask.getStatus());
            autoSetEpicStatus(epicId);
        }
    }

    @Override
    public void deleteTask(Task task) {
        tasks.remove(task.getId());
        history.remove(task.getId());
    }

    @Override
    public void deleteEpicTask(Epic epicTask) {
        int id = epicTask.getId();
        Epic existing = epicTasks.remove(id);
        if (existing != null) {
            for (Subtask s : existing.getSubtasks()) {
                subtasks.remove(s.getId());
                history.remove(s.getId());
            }
            history.remove(id);
        }
    }

    @Override
    public void deleteSubtask(Subtask subTask) {
        deleteSubtask(subTask.getId());
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        history.remove(id);
    }

    @Override
    public void deleteEpicTask(int id) {
        Epic epic = epicTasks.remove(id);
        if (epic != null) {
            for (Subtask s : epic.getSubtasks()) {
                subtasks.remove(s.getId());
                history.remove(s.getId());
            }
            history.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epicTasks.get(epicId);
            if (epic != null) {
                epic.removeSubtask(subtask);
                autoSetEpicStatus(epicId);
            }
            history.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : new ArrayList<>(tasks.values())) { // создаём копию, чтобы не было ConcurrentModificationException
            if (task.getTaskType() == TaskType.TASK) { // удаляем только обычные задачи
                history.remove(task.getId());
                tasks.remove(task.getId());
            }
        }
    }



    @Override
    public void deleteAllEpicTasks() {
        for (Subtask subtask : subtasks.values()) {
            history.remove(subtask.getId());
        }

        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            subtasks.remove(id);
        }

        for (Integer id : new ArrayList<>(epicTasks.keySet())) {
            epicTasks.remove(id);
        }
    }


    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            history.remove(subtask.getId());
        }

        for (Epic epic : epicTasks.values()) {
            epic.removeAllSubtasks();
            autoSetEpicStatus(epic.getId());
        }

        for (Integer id : new ArrayList<>(subtasks.keySet())) {
            subtasks.remove(id);
        }
    }


    private void autoSetEpicStatus(int id) {
        Epic epic = epicTasks.get(id);
        if (epic == null) return;

        List<Subtask> subTasks = epic.getSubtasks();
        if (subTasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = subTasks.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = subTasks.stream().allMatch(s -> s.getStatus() == Status.DONE);

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}
