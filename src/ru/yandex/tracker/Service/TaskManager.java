package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epicTasks;
    private HashMap<Integer, Subtask> subtasks;
    private int id = 1;

    public TaskManager() {
        tasks = new HashMap<>();
        epicTasks = new HashMap<>();
        subtasks = new HashMap<>();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpicTask(int id) {
        return epicTasks.get(id);
    }

    public Subtask getSubTask(int id) {
        return subtasks.get(id);
    }

    public void createTask(Task task) {
        task.setId(id);
        tasks.put(id, task);
        id++;
    }

    public void createEpicTask(Epic epicTask) {
        epicTask.setId(id);
        epicTasks.put(id, epicTask);
        id++;
    }

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

    public ArrayList<Subtask> getAllSubTasksByEpicId(int epicId) {
        ArrayList<Subtask> subTask = new ArrayList<>();
        if (epicTasks.containsKey(epicId)) {
            subTask = epicTasks.get(epicId).getSubtasks();
        }
        return subTask;
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

    public void deleteTask(int id) {
        int DeleteId = tasks.get(id).getId();
        tasks.remove(DeleteId);
    }

    public void deleteEpicTask(int id) {
        int DeleteId = epicTasks.get(id).getId();
        if (epicTasks.containsKey(DeleteId)) {
            Epic epicTaskToDelete = epicTasks.get(DeleteId);
            ArrayList<Subtask> subTasksToDelete = epicTaskToDelete.getSubtasks();
            for (Subtask subTask : subTasksToDelete) {
                subtasks.remove(subTask.getId());
            }
            epicTasks.remove(DeleteId);
        }
    }

    public void deleteSubtask(int id) {
        int DeleteId = subtasks.get(id).getMainId();
        if (subtasks.containsKey(DeleteId)) {
            Epic epicTask = epicTasks.get(id);
            epicTask.removeSubtask(subtasks.get(id).getMainId());
            subtasks.remove(DeleteId);
            autoSetEpicStatus(epicTask.getId());
        }
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpicTasks() {
        epicTasks.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {
        for (Epic epicTask : epicTasks.values()) {
            epicTask.removeAllSubtasks();
            autoSetEpicStatus(epicTask.getId());
        }
        subtasks.clear();
    }

    public void updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, task);
        }
    }

    public void updateEpicTask(Epic epicTask) {
        int id = epicTask.getId();
        if (epicTasks.containsKey(id)) {
            Epic epicTask1 = epicTasks.get(id);
            epicTask1.setName(epicTask.getName());
            epicTask1.setDescription(epicTask.getDescription());
        }
    }

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

    public List<Task> getAllTasks() {
            return new ArrayList<>(this.tasks.values());
        }

    public List<Epic> getAllEpicTasks() {
        return new ArrayList<>(this.epicTasks.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(this.subtasks.values());
    }
}