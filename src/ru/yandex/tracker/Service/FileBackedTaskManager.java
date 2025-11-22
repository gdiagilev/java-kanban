package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    private static final String HEADER = "id,type,name,status,description,epicId,startTime,duration";

    public FileBackedTaskManager() {
        this(new File(System.getProperty("java.io.tmpdir"), "kanban.csv"));
    }

    public FileBackedTaskManager(File file) {
        this.file = file;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при создании файла: " + file.getAbsolutePath());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Map<Integer, Task> allTasks = new HashMap<>();
        List<Integer> historyIds = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) return manager;

            int emptyIndex = lines.indexOf("");
            List<String> taskLines;
            if (emptyIndex != -1) {
                taskLines = lines.subList(1, emptyIndex);
                if (emptyIndex + 1 < lines.size()) historyIds = historyFromString(lines.get(emptyIndex + 1));
            } else {
                taskLines = lines.subList(1, lines.size());
            }

            for (String line : taskLines) {
                Task task = fromString(line.split(","));
                if (task == null) continue;
                allTasks.put(task.getId(), task);

                switch (task.getTaskType()) {
                    case TASK -> manager.tasks.put(task.getId(), task);
                    case EPIC -> manager.epicTasks.put(task.getId(), (Epic) task);
                    case SUBTASK -> {
                        Subtask sub = (Subtask) task;
                        manager.subtasks.put(sub.getId(), sub);
                        Epic epic = manager.epicTasks.get(sub.getEpicId());
                        if (epic != null) epic.addSubtask(sub);
                    }
                }
                manager.generatorId = Math.max(manager.generatorId, task.getId() + 1);
            }

            for (Integer id : historyIds) {
                if (allTasks.containsKey(id)) manager.history.add(allTasks.get(id));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return manager;
    }

    private static List<Integer> historyFromString(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    private static Task fromString(String[] fields) {
        try {
            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String name = fields[2];
            Status status = Status.valueOf(fields[3]);
            String description = fields[4];
            LocalDateTime startTime = fields.length > 6 && !fields[6].isEmpty() ? LocalDateTime.parse(fields[6]) : null;
            Duration duration = fields.length > 7 && !fields[7].isEmpty() ? Duration.ofMinutes(Long.parseLong(fields[7])) : null;

            switch (type) {
                case TASK -> {
                    Task task = new Task(name, description, status, startTime, duration);
                    task.setId(id);
                    return task;
                }
                case EPIC -> {
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    return epic;
                }
                case SUBTASK -> {
                    int epicId = Integer.parseInt(fields[5]);
                    Subtask sub = new Subtask(epicId, name, description, status, startTime, duration);
                    sub.setId(id);
                    return sub;
                }
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (Task t : getAllTasks()) writer.write(toString(t) + "\n");
            for (Epic e : getAllEpicTasks()) writer.write(toString(e) + "\n");
            for (Subtask s : getAllSubtasks()) writer.write(toString(s) + "\n");

            if (!getHistory().isEmpty()) {
                writer.newLine();
                String historyLine = getHistory().stream().map(t -> String.valueOf(t.getId())).collect(Collectors.joining(","));
                writer.write(historyLine);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    private String toString(Task task) {
        String epicId = "";
        String startTime = "";
        String duration = "";

        if (task instanceof Subtask) epicId = String.valueOf(((Subtask) task).getEpicId());
        if (task.getStartTime() != null) startTime = task.getStartTime().toString();
        if (task.getDuration() != null) duration = String.valueOf(task.getDuration().toMinutes());

        return String.join(",",
                String.valueOf(task.getId()),
                task.getTaskType().name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicId,
                startTime,
                duration
        );
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
    public void createSubtask(Subtask subTask) {
        super.createSubtask(subTask);
        save();
    }

    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void updateEpicTask(Epic updatedTask) {
        super.updateEpicTask(updatedTask);
        save();
    }

    @Override
    public void updateSubtask(Subtask updatedTask) {
        super.updateSubtask(updatedTask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpicTask(int id) {
        super.deleteEpicTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
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