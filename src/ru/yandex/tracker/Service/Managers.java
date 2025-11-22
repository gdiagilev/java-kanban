package ru.yandex.tracker.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Managers {

    public static FileBackedTasksManager getFileBackedManager(Path file) {
        return new FileBackedTasksManager(file);
    }

    public static FileBackedTasksManager getDefault() {
        Path defaultPath = Paths.get("tasks.csv");
        return getFileBackedManager(defaultPath);
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }


    public static InMemoryTaskManager getInMemoryManager() {
        return new InMemoryTaskManager();
    }
}