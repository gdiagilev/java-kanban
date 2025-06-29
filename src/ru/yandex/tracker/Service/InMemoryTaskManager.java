package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epicTasks;
    private HashMap<Integer, Subtask> subtasks;
    private int id = 1;
    private HistoryManager history;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subtasks = new HashMap<>();
        history  = Managers.getDefaultHistory();
    }

    @Override
    public Task getTask(int id) {
        final Task task = tasks.get(id);
            if (task == null) {
                return task;
            }
        history.add(task);
        return task;
    }

    @Override
    public Epic getEpicTask(int id) {
        final Epic epic = epicTasks.get(id);
        if (epic == null) {
            return epic;
        }
        history.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return subtask;
        }
        history.add(subtask);
        return subtask;
    }

    @Override
    public void createTask(Task task) {
        task.setId(id);
        tasks.put(id, task);
        id++;
    }

    @Override
    public void createEpicTask(Epic epicTask) {
        epicTask.setId(id);
        epicTasks.put(id, epicTask);
        id++;
    }

    @Override
    public void createSubtask(Subtask subTask) {
        int mainId = subTask.getMainId();
        if (epicTasks.containsKey(mainId)) {
            subTask.setId(id);
            Epic epicTask = epicTasks.get(mainId);
            epicTask.addSubtask(subTask);
            subtasks.put(id, subTask);
            autoSetEpicStatus(mainId);
            id++;
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubTasksByEpicId(int epicId) {
        ArrayList<Subtask> subTask = new ArrayList<>();
        if (epicTasks.containsKey(epicId)) {
            subTask = epicTasks.get(epicId).getSubtasks();
        }
        return subTask;
    }

    @Override
    public void deleteSubtask(Subtask subTask) {
        int idDelete = subTask.getId();
        tasks.remove(idDelete);
        history.remove(idDelete);
    }

    @Override
    public void deleteEpicTask(Epic epicTask) {
        int idDelete = epicTask.getId();
        if (epicTasks.containsKey(idDelete)) {
            Epic epicTaskToDelete = epicTasks.get(idDelete);
            ArrayList<Subtask> subTasksToDelete = epicTaskToDelete.getSubtasks();
            for (Subtask subTask : subTasksToDelete) {
                subtasks.remove(subTask.getId());
            }
            epicTasks.remove(idDelete);
            history.remove(idDelete);
        }
    }

    @Override
    public void deleteTask(Task task) {
        int idDelete = task.getId();
        tasks.remove(idDelete);
        history.remove(idDelete);
    }

    private void autoSetEpicStatus(int id) {
        if (!epicTasks.isEmpty()) {
            boolean allNew = true;
            boolean anyInProgress = false;
            Epic epicTask = epicTasks.get(id);
            ArrayList<Subtask> subTasks = epicTask.getSubtasks();
            if (!subTasks.isEmpty()) {
                for (Subtask subTask : subTasks) {
                    if (subTask.getStatus() != Status.NEW) {
                        allNew = false;
                    }
                    if (subTask.getStatus() == Status.IN_PROGRESS) {
                        anyInProgress = true;
                    }
                    if (!allNew && anyInProgress) {
                        break;
                    }
                }
            }
            if (allNew) {
                epicTask.setStatus(Status.NEW);
            } else if (anyInProgress) {
                epicTask.setStatus(Status.IN_PROGRESS);
            } else {
                epicTask.setStatus(Status.DONE);
            }
        }
    }

    @Override
    public void deleteTask(int id) {
        int deleteId = tasks.get(id).getId();
        tasks.remove(deleteId);
    }

    @Override
    public void deleteEpicTask(int id) {
        int deleteId = epicTasks.get(id).getId();
        if (epicTasks.containsKey(deleteId)) {
            Epic epicTaskToDelete = epicTasks.get(deleteId);
            ArrayList<Subtask> subTasksToDelete = epicTaskToDelete.getSubtasks();
            for (Subtask subTask : subTasksToDelete) {
                subtasks.remove(subTask.getId());
            }
            epicTasks.remove(deleteId);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }

        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            return;
        }

        int mainId = subtask.getMainId();
        Epic epicTask = epicTasks.get(mainId);

        if (epicTask != null) {
            epicTask.removeSubtask(id);
            autoSetEpicStatus(mainId);
        }
        subtasks.remove(id);
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
        for (Epic epicTask : epicTasks.values()) {
            epicTask.removeAllSubtasks();
            autoSetEpicStatus(epicTask.getId());
        }
        subtasks.clear();
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, task);
        }
    }

    @Override
    public void updateEpicTask(Epic epicTask) {
        int id = epicTask.getId();
        if (epicTasks.containsKey(id)) {
            Epic epicTask1 = epicTasks.get(id);
            epicTask1.setName(epicTask.getName());
            epicTask1.setDescription(epicTask.getDescription());
        }
    }

    @Override
    public void updateSubtask(Subtask subTask) {
        int id = subTask.getId();
        int mainId = subTask.getMainId();
        if ((subtasks.containsKey(id)) && (epicTasks.containsKey(mainId)) && (subtasks.get(id).getMainId() == subTask.getMainId())) {
            Subtask subTaskToUpdate = subtasks.get(id);
            subTaskToUpdate.update(subTask.getName(), subTask.getDescription(), subTask.getStatus());
            Epic epicTask = epicTasks.get(mainId);
            Subtask subTask1 = epicTask.getSubtask(id);
            subTask1.update(subTask.getName(), subTask.getDescription(), subTask.getStatus());
            autoSetEpicStatus(mainId);

        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    @Override
    public List<Epic> getAllEpicTasks() {
        return new ArrayList<>(this.epicTasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(this.subtasks.values());
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }
}