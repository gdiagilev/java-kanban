package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public abstract class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;

    public FileBackedTaskManager(Path file) {
        super();
        this.file = file;
        if (java.nio.file.Files.exists(file)) {
            loadFromFile();
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("id,type,name,status,description,startTime,duration,epic\n");
            for (Task t : getAllTasks()) {
                writer.write(toString(t) + "\n");
            }
            for (Epic e : getAllEpicTasks()) {
                writer.write(toString(e) + "\n");
            }
            for (Subtask s : getAllSubtasks()) {
                writer.write(toString(s) + "\n");
            }

            // сохранение истории
            writer.write("\n");
            List<Task> historyList = getHistory();
            StringBuilder historyLine = new StringBuilder();
            for (int i = 0; i < historyList.size(); i++) {
                historyLine.append(historyList.get(i).getId());
                if (i != historyList.size() - 1) historyLine.append(",");
            }
            writer.write(historyLine.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toString(Task task) {
        String start = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getTaskType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                start,
                duration,
                epicId
        );
    }

    private void loadFromFile() {
        try {
            List<String> lines = Files.readAllLines(file);
            Map<Integer, Task> tempTasks = new HashMap<>();
            Map<Integer, Epic> tempEpics = new HashMap<>();
            Map<Integer, Subtask> tempSubtasks = new HashMap<>();

            boolean readHistory = false;
            for (String line : lines) {
                if (line.isEmpty()) {
                    readHistory = true;
                    continue;
                }
                if (line.startsWith("id")) continue;

                if (!readHistory) {
                    Task task = fromString(line);
                    if (task instanceof Epic) tempEpics.put(task.getId(), (Epic) task);
                    else if (task instanceof Subtask) tempSubtasks.put(task.getId(), (Subtask) task);
                    else tempTasks.put(task.getId(), task);
                    generatorId = Math.max(generatorId, task.getId() + 1);
                } else {
                    String[] ids = line.split(",");
                    for (String sId : ids) {
                        int id = Integer.parseInt(sId);
                        if (tempTasks.containsKey(id)) history.add(tempTasks.get(id));
                        else if (tempEpics.containsKey(id)) history.add(tempEpics.get(id));
                        else if (tempSubtasks.containsKey(id)) history.add(tempSubtasks.get(id));
                    }
                }
            }

            tasks.putAll(tempTasks);
            epicTasks.putAll(tempEpics);
            subtasks.putAll(tempSubtasks);

            // привязка подзадач к эпикам
            for (Subtask s : subtasks.values()) {
                Epic epic = epicTasks.get(s.getEpicId());
                if (epic != null) epic.addSubtask(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Task fromString(String value) {
        String[] parts = value.split(",", -1); // -1 чтобы учитывать пустые
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String desc = parts[4];
        LocalDateTime start = parts[5].isEmpty() ? null : LocalDateTime.parse(parts[5]);
        Duration dur = parts[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[6]));

        switch (type) {
            case TASK:
                Task task = new Task(name, desc, status, start, dur);
                task.setId(id);
                return task;
            case EPIC:
                return new Epic(name, desc, id, status);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[7]);
                Subtask subtask = new Subtask(epicId, name, desc, status, start, dur);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpicTask(Epic epic) {
        super.createEpicTask(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) throws NotFoundException {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpicTask(Epic epic) throws NotFoundException {
        super.updateEpicTask(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) throws NotFoundException {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(Task task) throws NotFoundException {
        super.deleteTask(task);
        save();
    }

    @Override
    public void deleteEpicTask(Epic epic) throws NotFoundException {
        super.deleteEpicTask(epic);
        save();
    }

    @Override
    public void deleteSubtask(Subtask subtask) throws NotFoundException {
        super.deleteSubtask(subtask);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpicTasks() {
        super.deleteAllEpicTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}