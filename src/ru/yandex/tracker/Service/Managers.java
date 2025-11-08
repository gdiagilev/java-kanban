package ru.yandex.tracker.Service;
import java.io.File;

public class Managers {

    private static final HistoryManager historyManager = new InMemoryHistoryManager();

    public TaskManager getDefault() {
        return new FileBackedTaskManager(new File("./resources/kanban.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return historyManager;
    }
}