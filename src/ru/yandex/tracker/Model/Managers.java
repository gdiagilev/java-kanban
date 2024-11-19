package ru.yandex.tracker.Model;

import ru.yandex.tracker.Service.HistoryManager;
import ru.yandex.tracker.Service.InMemoryHistoryManager;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

public class Managers {
    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
    public static HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }
}
