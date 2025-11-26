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

public class SubtasksHttpTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testCreateAndGetSubtask() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                null,
                null
        );
        manager.createEpicTask(epic);

        Subtask subtask = new Subtask(
                epic.getId(),
                "Subtask1",
                "Desc1",
                Status.NEW,
                null,
                null
        );
        manager.createSubtask(subtask);

        Subtask retrieved = manager.getSubtask(subtask.getId());
        assertNotNull(retrieved);
        assertEquals(subtask.getName(), retrieved.getName());
        assertEquals(epic.getId(), retrieved.getEpicId());
    }

    @Test
    void testGetSubtasksOfEpic() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                null,
                null
        );
        manager.createEpicTask(epic);

        Subtask sub1 = new Subtask(
                epic.getId(),
                "Sub1",
                "Desc1",
                Status.NEW,
                null,
                null
        );
        Subtask sub2 = new Subtask(
                epic.getId(),
                "Sub2",
                "Desc2",
                Status.NEW,
                null,
                null
        );

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(2, manager.getSubtasksOfEpic(epic.getId()).size());
    }
}