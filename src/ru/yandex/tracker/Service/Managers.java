package ru.yandex.tracker.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Managers {

    public static TaskManager getDefault() {
        Path path = Paths.get("./resources/kanban.csv");
        return new FileBackedTaskManager(path);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}