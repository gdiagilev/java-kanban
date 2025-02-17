package ru.yandex.tracker.Model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Service.InMemoryHistoryManager;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.Managers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TaskTest {

    Managers managers = new Managers();
    InMemoryTaskManager manager = (InMemoryTaskManager) managers.getDefault();

    @BeforeEach
    void init() {
        Managers managers = new Managers();
        InMemoryTaskManager manager = (InMemoryTaskManager) managers.getDefault();
    }

    @Test
    void checkTaskById() {
        Task task = new Task("task1","task1", Status.NEW);
        Task task2  = new Task("task1","task1", Status.NEW);
        manager.createTask(task);
        manager.createTask(task2);
        Assertions.assertNotEquals(manager.getTask(1), manager.getTask(2));
    }

    @Test
    void checkEqualsTaskbyId() {
        Task task = new Task("task1","task1", Status.NEW);
        manager.createTask(task);
        Assertions.assertEquals(task, manager.getTask(1));
    }

    @Test
    void validCreationfOfObjects() {
        Task task = new Task("task1","task1", Status.NEW);
        Epic epicTask = new Epic("epicTask1","epicTask1");
        Subtask subTask = new Subtask(2,"subTask1","subTask1", Status.NEW);
        manager.createTask(task);
        manager.createEpicTask(epicTask);
        manager.createSubtask(subTask);
        Task task2  = new Task("task1","task1", Status.NEW);
        assertInstanceOf(Task.class, manager.getTask(1), "Объекты не совпадают");
        assertInstanceOf(Epic.class, manager.getEpicTask(2), "Объекты не совпадают");
        assertInstanceOf(Subtask.class, manager.getSubtask(3), "Объекты не совпадают");
    }

    @Test
    void inMemoryTaskManagerSearchTestByID() {
        Task task = new Task("task1","task1", Status.NEW);
        Epic epicTask = new Epic("epicTask1","epicTask1");
        Subtask subTask = new Subtask(2,"subTask1","subTask1", Status.NEW);
        manager.createTask(task);
        manager.createEpicTask(epicTask);
        manager.createSubtask(subTask);
        Assertions.assertEquals(manager.getTask(1), task);
        Assertions.assertEquals(manager.getEpicTask(2), epicTask);
        Assertions.assertEquals(manager.getSubtask(3), subTask);
    }

    @Test
    void historyManagerTestOfObjects() {
        InMemoryHistoryManager historyManager  = (InMemoryHistoryManager) managers.getDefaultHistory();
        Task task  = new Task("task1","task1", Status.NEW);
        Epic epicTask   = new Epic("epicTask1","epicTask1");
        Subtask subTask   = new Subtask(2,"subtask1","subtask1", Status.NEW);
        manager.createTask(task);
        manager.createEpicTask(epicTask);
        manager.createSubtask(subTask);
        historyManager.add(task);
        historyManager.add(epicTask);
        List<Task> history  = new ArrayList<>();
        history.add(task);
        history.add(epicTask);
        List<Task> historyToCompare = historyManager.getHistory();
        assertEquals(history, historyToCompare);
        history.add(subTask);
        historyManager.add(subTask);
        historyToCompare = historyManager.getHistory();
        assertEquals(history, historyToCompare);
    }

    @Test
    void addAndRemoveInMemoryHistoryManager() {
        InMemoryHistoryManager historyManager = (InMemoryHistoryManager) managers.getDefaultHistory();
        Task task1 = new Task("Task1", "Task1", Status.NEW);
        Task task2 = new Task("Task2", "Task2", Status.NEW);
        Task task3 = new Task("Task3", "Task3", Status.NEW);
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        List<Task> history = new ArrayList<>();
        history.add(task1);
        history.add(task2);
        history.add(task3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> historyToCompare = historyManager.getHistory();
        Assertions.assertEquals(history, historyToCompare);

        historyManager.remove(1);
        history.remove(task1);
        historyToCompare = historyManager.getHistory();
        Assertions.assertEquals(history, historyToCompare);
    }
}

