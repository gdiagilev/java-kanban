package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.NotFoundException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;

    public FileBackedTaskManager(Path file) {
        this.file = file;
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("id,type,name,status,description,startTime,duration,epic\n");
            for (Task task : getAllTasks()) writer.write(taskToString(task) + "\n");
            for (Epic epic : getAllEpicTasks()) writer.write(taskToString(epic) + "\n");
            for (Subtask sub : getAllSubtasks()) writer.write(taskToString(sub) + "\n");
            writer.write("\n");
            writer.write(historyToString(getHistory()));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        if (!Files.exists(file)) return manager;

        try {
            List<String> lines = Files.readAllLines(file);
            Map<Integer, Task> tempTasks = new HashMap<>();
            Map<Integer, Epic> tempEpics = new HashMap<>();
            Map<Integer, Subtask> tempSubs = new HashMap<>();
            boolean readHistory = false;

            for (String line : lines) {
                if (line.isEmpty()) {
                    readHistory = true;
                    continue;
                }
                if (line.startsWith("id,")) continue;

                if (!readHistory) {
                    Task task = fromString(line);
                    if (task instanceof Epic) tempEpics.put(task.getId(), (Epic) task);
                    else if (task instanceof Subtask) tempSubs.put(task.getId(), (Subtask) task);
                    else tempTasks.put(task.getId(), task);
                } else {
                    List<Integer> historyIds = historyFromString(line);
                    for (int id : historyIds) {
                        if (tempTasks.containsKey(id)) manager.history.add(tempTasks.get(id));
                        else if (tempEpics.containsKey(id)) manager.history.add(tempEpics.get(id));
                        else if (tempSubs.containsKey(id)) manager.history.add(tempSubs.get(id));
                    }
                }
            }

            manager.tasks.putAll(tempTasks);
            manager.epicTasks.putAll(tempEpics);
            manager.subtasks.putAll(tempSubs);

            // Привязываем подзадачи к эпикам
            for (Subtask sub : tempSubs.values()) {
                Epic epic = manager.epicTasks.get(sub.getEpicId());
                if (epic != null) epic.addSubtask(sub);
            }

            // Восстанавливаем генератор ID
            int maxId = Stream.concat(
                    Stream.concat(manager.tasks.keySet().stream(), manager.epicTasks.keySet().stream()),
                    manager.subtasks.keySet().stream()
            ).max(Integer::compareTo).orElse(0);
            manager.generatorId = maxId + 1;

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке из файла", e);
        }

        return manager;
    }

    private String taskToString(Task task) {
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String epic = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task instanceof Epic ? "EPIC" : task instanceof Subtask ? "SUBTASK" : "TASK",
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                startTime,
                duration,
                epic
        );
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        LocalDateTime startTime = parts[5].isEmpty() ? null : LocalDateTime.parse(parts[5]);
        Duration duration = parts[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[6]));

        switch (type) {
            case "TASK": {
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                return task;
            }
            case "EPIC": {
                Epic epic = new Epic(name, description, 0, status);
                epic.setId(id);
                return epic;
            }
            case "SUBTASK": {
                int epicId = Integer.parseInt(parts[7]);
                Subtask sub = new Subtask(epicId, name, description, status, startTime, duration);
                sub.setId(id);
                return sub;
            }
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private String historyToString(List<Task> history) {
        List<String> ids = new ArrayList<>();
        for (Task t : history) ids.add(String.valueOf(t.getId()));
        return String.join(",", ids);
    }

    private static List<Integer> historyFromString(String value) {
        List<Integer> ids = new ArrayList<>();
        if (!value.isEmpty()) {
            String[] parts = value.split(",");
            for (String s : parts) ids.add(Integer.parseInt(s));
        }
        return ids;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpicTask(Epic epicTask) {
        super.createEpicTask(epicTask);
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
    public void updateEpicTask(Epic epicTask) throws NotFoundException {
        super.updateEpicTask(epicTask);
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
    public void deleteEpicTask(Epic epicTask) throws NotFoundException {
        super.deleteEpicTask(epicTask);
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