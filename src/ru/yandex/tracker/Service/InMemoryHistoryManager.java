package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Model.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private Node<Task> head;
    private Node<Task> tail;
    private final Map<Integer, Node<Task>> historyMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task == null) return;

        int id = task.getId();

        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
        }

        // Добавляем в конец
        Node<Task> newNode = linkLast(task);
        historyMap.put(id, newNode);
    }

    @Override
    public void remove(int id) {
        Node<Task> node = historyMap.remove(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    private Node<Task> linkLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        return newNode;
    }

    private void removeNode(Node<Task> node) {
        if (node == null) return;

        final Node<Task> prev = node.prev;
        final Node<Task> next = node.next;

        if (prev != null) {
            prev.next = next;
        } else {
            head = next;
        }

        if (next != null) {
            next.prev = prev;
        } else {
            tail = prev;
        }
    }
}