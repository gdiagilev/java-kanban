package ru.yandex.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Model.Subtask;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpSubtaskTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                null,
                null
        );
        manager.createEpicTask(epic);
    }

    @Test
    void testCreateSubtask() {
        Subtask subtask = new Subtask(
                epic.getId(),          // epicId
                "Sub1",               // name
                "SubDesc",            // description
                Status.NEW,           // status
                null,                 // startTime
                null                  // duration
        );

        manager.createSubtask(subtask);
        Subtask retrieved = manager.getSubtask(subtask.getId());
        assertNotNull(retrieved);
        assertEquals("Sub1", retrieved.getName());
        assertEquals(epic.getId(), retrieved.getEpicId());
    }
}