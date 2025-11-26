package ru.yandex.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpEpicTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testCreateEpicWithSubtasks() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                null,
                null
        );
        manager.createEpicTask(epic);

        Subtask s1 = new Subtask(
                epic.getId(),
                "S1",
                "D1",
                Status.NEW,
                null,
                null
        );

        Subtask s2 = new Subtask(
                epic.getId(),
                "S2",
                "D2",
                Status.NEW,
                null,
                null
        );

        manager.createSubtask(s1);
        manager.createSubtask(s2);

        assertEquals(2, manager.getSubtasksOfEpic(epic.getId()).size());
        assertTrue(manager.getSubtasksOfEpic(epic.getId()).stream()
                .anyMatch(sub -> sub.getName().equals("S1")));
        assertTrue(manager.getSubtasksOfEpic(epic.getId()).stream()
                .anyMatch(sub -> sub.getName().equals("S2")));
    }
}