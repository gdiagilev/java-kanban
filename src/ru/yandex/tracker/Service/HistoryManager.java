package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}