package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> history;

    public InMemoryHistoryManager(){
        history = new ArrayList<>();
    }
    @Override
    public void add(Task task) {
        if (history.size() > 9){
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory()  {
        List<Task> history = this.history;
        return history;
    }
}