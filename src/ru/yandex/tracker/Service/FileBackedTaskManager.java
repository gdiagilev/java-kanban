package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    private static final String HEADER = "id,type,name,status,description, epic";

    public FileBackedTaskManager() {
        this(new File("./resources/kanban.csv"));
    }

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    protected static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Map<Integer, Task> allTasks = new HashMap<>();
        List<Integer> historyIds = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) return manager;

            int emptyIndex = lines.indexOf("");
            List<String> taskLines;
            if (emptyIndex != -1) {
                taskLines = lines.subList(1, emptyIndex); // пропускаем заголовок
                if (emptyIndex + 1 < lines.size()) {
                    historyIds = historyFromString(lines.get(emptyIndex + 1));
                }
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
                        manager.subtasks.put(task.getId(), (Subtask) task);
                        int epicId = ((Subtask) task).getEpicId();
                        Epic epic = manager.epicTasks.get(epicId);
                        if (epic != null) {
                            epic.addSubtask((Subtask) task);
                        }
                    }
                }
                manager.generatorId = Math.max(manager.generatorId, task.getId());
            }

            for (Integer id : historyIds) {
                if (allTasks.containsKey(id)) {
                    manager.history.add(allTasks.get(id));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return manager;
    }

    private static List<Integer> historyFromString(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private static Task fromString(String[] fields) {
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];

        return switch (type) {
            case TASK -> new Task(name, description, id, status);
            case EPIC -> new Epic(name, description, id, status);
            case SUBTASK -> {
                int epicId = Integer.parseInt(fields[8]);
                yield new Subtask(epicId, name, description, status, id);
            }
        };
    }

    protected void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            addTasksToFile(writer);

            if (!getHistory().isEmpty()) {
                writer.newLine();
                List<String> historyIds = getHistory().stream()
                        .map(t -> String.valueOf(t.getId()))
                        .collect(Collectors.toList());
                writer.write(String.join(",", historyIds));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    private void addTasksToFile(BufferedWriter writer) throws IOException {
        for (Task task : getAllTasks()) {
            writer.write(toString(task));
            writer.newLine();
        }
        for (Epic epic : getAllEpicTasks()) {
            writer.write(toString(epic));
            writer.newLine();
        }
        for (Subtask subtask : getAllSubtasks()) {
            writer.write(toString(subtask));
            writer.newLine();
        }
    }

    private String toString(Task task) {
        String epicId = "";

        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        return String.join(",",
                String.valueOf(task.getId()),
                task.getTaskType().name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                "", "", "", epicId
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
