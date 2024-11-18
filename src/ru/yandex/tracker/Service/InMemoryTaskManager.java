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
        history  = new InMemoryHistoryManager();
    }

    @Override
    public Task getTask(int id) {
        if (tasks.containsKey(id)) {
            history.add(tasks.get(id));
            return tasks.get(id);
        } else return null;
    }

    @Override
    public Epic getEpicTask(int id) {
        if (epicTasks.containsKey(id)) {
            history.add(epicTasks.get(id));
            return epicTasks.get(id);
        } else return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        if (subtasks.containsKey(id)) {
            history.add(subtasks.get(id));
            return subtasks.get(id);
        } else return null;
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

    }

    @Override
    public void deleteEpicTask(Epic epicTask) {

    }

    @Override
    public void deleteTask(Task task) {

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
        int DeleteId = tasks.get(id).getId();
        tasks.remove(DeleteId);
    }

    @Override
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

    @Override
    public void deleteSubtask(int id) {
        int DeleteId = subtasks.get(id).getMainId();
        if (subtasks.containsKey(DeleteId)) {
            Epic epicTask = epicTasks.get(id);
            epicTask.removeSubtask(subtasks.get(id).getMainId());
            subtasks.remove(DeleteId);
            autoSetEpicStatus(epicTask.getId());
        }
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
        ArrayList<Task> allTasks = new ArrayList<>();
        for (Task task : tasks.values()) {
            allTasks.add(task);
            history.add(task);
        }
        return allTasks;
    }

    @Override
    public List<Epic> getAllEpicTasks() {
        ArrayList<Epic> allEpicTasks = new ArrayList<>();
        for (Epic epicTask : epicTasks.values()) {
            allEpicTasks.add(epicTask);
            history.add(epicTask);
        }
        return allEpicTasks;
    }

    @Override
    public List<Subtask> getAllSubTasks() {
        ArrayList<Subtask> allSubTasks = new ArrayList<>();
        for (Subtask subTask : subtasks.values()) {
            allSubTasks.add(subTask);
            history.add(subTask);
        }
        return allSubTasks;
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