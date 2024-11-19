package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> history;
    private static int MAX_VALUE = 9;

    public InMemoryHistoryManager(){
        history = new LinkedList<>();
    }

    @Override
    public void add(Task task) {
        if (history.size() > MAX_VALUE){
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory()  {
        return new ArrayList<>(history);
    }
}