package ru.yandex.tracker.Model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.Managers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class EpicTaskTest {
    Managers managers = new Managers();
    InMemoryTaskManager manager = (InMemoryTaskManager) managers.getDefault();

    @BeforeEach
    void init() {
        Managers managers = new Managers();
        InMemoryTaskManager manager = (InMemoryTaskManager) managers.getDefault();
    }

    @Test
    void addSubtask() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        Assertions.assertNotNull(subtask1, "Подзадача не добавлена");
    }

    @Test
    void removeSubtask() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        manager.deleteSubtask(subtask1);
        Assertions.assertNull(manager.getSubtask(2),  "Подзадача не удалена");
    }

    @Test
    void removeAllSubtasks() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(1, "Подзадача 2",  "Подзадача эпика 2", Status.NEW);
        manager.createSubtask(subtask2);
        manager.deleteAllSubtasks();
        Assertions.assertNull(manager.getSubtask(2),   "Подзадача не удалена");
        Assertions.assertNull(manager.getSubtask(3),    "Подзадача не удалена");
    }

    @Test
    void getSubTasks() {
        Epic epicTask1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpicTask(epicTask1);
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача эпика 1", Status.NEW);
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask(1, "Подзадача 2",  "Подзадача эпика 2", Status.NEW);
        manager.createSubtask(subtask2);
        List<Subtask> subTasks  = new ArrayList<>();
        subTasks.add(subtask1);
        subTasks.add(subtask2);
        List<Subtask> actualSubTasks = manager.getAllSubTasks();
        assertArrayEquals(actualSubTasks.toArray(), subTasks.toArray());
    }
}