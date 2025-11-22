package ru.yandex.tracker.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Managers {

    private Managers() {
    }


    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }


    public static FileBackedTasksManager getFileBacked(Path file) {
        return new FileBackedTasksManager(file);
    }


    public static FileBackedTasksManager getFileBacked() {
        Path defaultPath = Paths.get("tasks.csv");
        return new FileBackedTasksManager(defaultPath);
    }


    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}