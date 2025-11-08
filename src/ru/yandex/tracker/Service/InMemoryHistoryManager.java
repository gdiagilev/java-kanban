package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Model.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> head;
    private Node<Task> tail;
    private final Map<Integer, Node<Task>> historyMap = new HashMap<>();
    private int size = 0;

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        int id = task.getId();
        Node<Task> existingNode = historyMap.get(id);
        if (existingNode != null) {
            removeNode(existingNode);
        }

        linkedLast(task);
    }

    @Override
    public void remove(int id) {
        Node<Task> node = historyMap.remove(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkedLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }

        historyMap.put(task.getId(), newNode);
        size++;
    }

    private void removeNode(Node<Task> node) {
        if (node == null) return;

        Node<Task> prev = node.prev;
        Node<Task> next = node.next;

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

        size--;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>(size);
        Node<Task> current = head;
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }
        return tasks;
    }
}