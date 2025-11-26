package ru.yandex.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TaskManagerTest {

    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        epic = new Epic(
                "Epic 1",
                "Test epic",
                Status.NEW,
                null,
                null
        );
        manager.createEpicTask(epic);
    }

    @Test
    void createAndRetrieveEpic() {
        Epic retrieved = manager.getEpicTask(epic.getId());
        assertNotNull(retrieved);
        assertEquals("Epic 1", retrieved.getName());
    }
}