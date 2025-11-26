package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskCSVConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static String escape(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String unescape(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }
        return value;
    }

    public static String toString(Task task) {
        String type = task instanceof Epic ? "EPIC" :
                task instanceof Subtask ? "SUBTASK" : "TASK";

        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";

        return String.join(",",
                String.valueOf(task.getId()),
                type,
                escape(task.getName()),
                escape(task.getDescription()),
                task.getStatus().name(),
                startTime,
                duration,
                epicId
        );
    }

    public static Task fromString(String line, FileBackedTasksManager manager) {
        if (line == null || line.isEmpty()) return null;

        List<String> parts = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        parts.add(sb.toString());

        int id = Integer.parseInt(parts.get(0));
        String type = parts.get(1);
        String name = unescape(parts.get(2));
        String desc = unescape(parts.get(3));
        Status status = Status.valueOf(parts.get(4));
        LocalDateTime startTime = parts.get(5).isEmpty() ? null : LocalDateTime.parse(parts.get(5), DATE_TIME_FORMATTER);
        Duration duration = parts.get(6).isEmpty() ? null : Duration.ofMinutes(Long.parseLong(parts.get(6)));

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
                int epicId = Integer.parseInt(parts.get(7));
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
        return history.stream()
                .map(task -> String.valueOf(task.getId()))
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