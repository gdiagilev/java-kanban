package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;

public class TaskCSVConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String toString(Task task) {
        String type = task instanceof Epic ? "EPIC" :
                task instanceof Subtask ? "SUBTASK" : "TASK";

        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";

        String startTime = task.getStartTime() != null ? task.getStartTime().format(FORMATTER) : "";
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

    public static String historyToString(List<Task> history) {
        List<String> ids = new ArrayList<>();
        for (Task task : history) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    public static void fromString(String line, FileBackedTasksManager manager) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String desc = parts[3];
        Status status = Status.valueOf(parts[4]);

        LocalDateTime startTime = parts[5].isEmpty() ? null : LocalDateTime.parse(parts[5], FORMATTER);
        Duration duration = parts[6].isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts[6]));

        switch (type) {
            case "TASK":
                Task task = new Task(name, desc, status, startTime, duration);
                task.setId(id);
                manager.addTaskFromFile(task);
                break;
            case "EPIC":
                Epic epic = new Epic(name, desc, status, startTime, duration);
                epic.setId(id);
                manager.addEpicFromFile(epic);
                break;
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[7]);
                Subtask subtask = new Subtask(epicId, name, desc, status, startTime, duration);
                subtask.setId(id);
                manager.addSubtaskFromFile(subtask);
                break;
        }
    }

    public static List<Integer> historyFromString(String line) {
        List<Integer> ids = new ArrayList<>();
        if (!line.isEmpty()) {
            String[] parts = line.split(",");
            for (String s : parts) ids.add(Integer.parseInt(s));
        }
        return ids;
    }
}