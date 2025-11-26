package ru.yandex.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.tracker.Model.Epic;
import ru.yandex.tracker.Model.Status;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicsHttpTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testCreateAndGetEpic() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                null,
                null
        );
        manager.createEpicTask(epic);
        Epic retrieved = manager.getEpicTask(epic.getId());
        assertEquals(epic.getName(), retrieved.getName());
    }
}