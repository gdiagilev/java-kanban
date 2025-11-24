package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskCSVConverter {

    public static String toString(Task task) {
        String type = task instanceof Epic ? "EPIC" :
                task instanceof Subtask ? "SUBTASK" : "TASK";

        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";

        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getName(),
                task.getDescription(),
                task.getStatus().name(),
                startTime,
                duration,
                epicId
        );
    }

    public static Task fromString(String line, FileBackedTasksManager manager) {
        if (line == null || line.isEmpty()) return null;

        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String desc = parts[3];
        Status status = Status.valueOf(parts[4]);
        LocalDateTime startTime = parts[5].isEmpty() ? null : LocalDateTime.parse(parts[5]);
        Duration duration = parts[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[6]));

        switch (type) {
            case "TASK": {
                Task task = new Task(name, desc, status, startTime, duration);
                task.setId(id);
                manager.addTaskFromFile(task);
                return task;
            }
            case "EPIC": {
                Epic epic = new Epic(name, desc);
                epic.setId(id);
                manager.addEpicFromFile(epic);
                return epic;
            }
            case "SUBTASK": {
                int epicId = Integer.parseInt(parts[7]);
                Subtask subtask = new Subtask(epicId, name, desc, status, startTime, duration);
                subtask.setId(id);
                manager.addSubtaskFromFile(subtask);
                return subtask;
            }
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public static String historyToString(List<Task> history) {
        return history.stream().map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(","));
    }

    public static List<Integer> historyFromString(String value) {
        if (value == null || value.isEmpty()) return new ArrayList<>();
        String[] parts = value.split(",");
        List<Integer> result = new ArrayList<>();
        for (String s : parts) result.add(Integer.parseInt(s));
        return result;
    }
}