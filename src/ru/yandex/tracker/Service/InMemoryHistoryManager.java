package ru.yandex.tracker.Service;

import ru.yandex.tracker.Model.Task;
import ru.yandex.tracker.Model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> head;
    private Node<Task> tail;
    private int size = 0;
    private Map<Integer, Node> historyMap = new HashMap<>();

    public InMemoryHistoryManager() {
        historyMap = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task != null){
            remove(task.getId());
            linkedLast(task);
        }
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    final void linkedLast(Task task) {
        final Node<Task> newNode;
        final Node<Task> oldTail = tail;
        newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        size++;
        historyMap.put(task.getId(), newNode);
    }

    private <T extends Task> List<T> getTasks() {
        List<T> listOfTasks = new ArrayList<>();
        Node<T> node = (Node<T>) head;
        while (node != null) {
            listOfTasks.add(node.task);
            node = node.next;
        }
        return listOfTasks;
    }

    private void removeNode(Node node) {
        Node<Task> prevNode = node.prev;
        Node<Task> nextNode = node.next;
        if (size == 1) {
            head = null;
            tail = null;
        } else if (size > 1) {
            if (prevNode == null) {
                head = nextNode;
                nextNode.prev = null;
            } else if (nextNode == null) {
                tail = prevNode;
                prevNode.next = null;
            } else {
                prevNode.next = nextNode;
                nextNode.prev = prevNode;
            }
        }
        if (size != 0) {
            size--;
        }
    }
}