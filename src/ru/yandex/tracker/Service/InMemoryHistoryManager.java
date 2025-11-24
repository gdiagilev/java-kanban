package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;
        remove(task.getId());

        Node node = new Node(tail, task, null);
        if (tail != null) tail.next = node;
        tail = node;
        if (head == null) head = node;

        nodeMap.put(task.getId(), node);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node == null) return;

        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;

        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }
}