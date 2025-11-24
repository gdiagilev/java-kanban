package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private final Path file;

    public FileBackedTasksManager(Path file) {
        this.file = file;
        loadFromFile();
    }

    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpicTask(Epic epic) {
        super.createEpicTask(epic);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask;
    }


    @Override
    public Task updateTask(Task task) {
        Task updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public Epic updateEpicTask(Epic epic) {
        Epic updated = super.updateEpicTask(epic);
        save();
        return updated;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updated = super.updateSubtask(subtask);
        save();
        return updated;
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


    public void addTaskFromFile(Task task) {
        super.tasks.put(task.getId(), task);
    }

    public void addEpicFromFile(Epic epic) {
        super.epics.put(epic.getId(), epic);
    }

    public void addSubtaskFromFile(Subtask subtask) {
        super.subtasks.put(subtask.getId(), subtask);
        Epic epic = super.epics.get(subtask.getEpicId());
        if (epic != null) epic.addSubtask(subtask);
    }


    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("id,type,name,description,status,startTime,duration,epic\n");

            for (Task task : super.getAllTasks()) {
                writer.write(TaskCSVConverter.toString(task));
                writer.newLine();
            }
            for (Epic epic : super.getAllEpicTasks()) {
                writer.write(TaskCSVConverter.toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : super.getAllSubtasks()) {
                writer.write(TaskCSVConverter.toString(subtask));
                writer.newLine();
            }

            writer.newLine();
            writer.write(TaskCSVConverter.historyToString(super.getHistory()));

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении задач в файл", e);
        }
    }


    private void loadFromFile() {
        if (!Files.exists(file)) return;

        try {
            List<String> lines = Files.readAllLines(file);
            boolean historySection = false;

            for (String line : lines) {
                if (line.isEmpty()) {
                    historySection = true;
                    continue;
                }
                if (line.startsWith("id,type")) continue;

                if (!historySection) {
                    TaskCSVConverter.fromString(line, this);
                } else {
                    List<Integer> historyIds = TaskCSVConverter.historyFromString(line);
                    for (Integer id : historyIds) {
                        Task task = super.tasks.get(id);
                        if (task == null) task = super.epics.get(id);
                        if (task == null) task = super.subtasks.get(id);
                        if (task != null) super.historyManager.add(task);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке задач из файла", e);
        }
    }
}