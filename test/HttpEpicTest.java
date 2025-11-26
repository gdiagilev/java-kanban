package ru.yandex.tracker;

import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpEpicTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateAndRetrieveEpic() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ZERO
        );

        manager.createEpicTask(epic);

        Epic retrieved = manager.getEpicTask(epic.getId());
        assertNotNull(retrieved);
        assertEquals(epic.getName(), retrieved.getName());
    }

    @Test
    void shouldAddSubtasksToEpic() {
        Epic epic = new Epic(
                "Epic2",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ZERO
        );

        manager.createEpicTask(epic);

        Subtask s1 = new Subtask(
                epic.getId(),
                "S1",
                "D1",
                Status.NEW,
                LocalDateTime.now().plusMinutes(1),
                Duration.ofMinutes(15)
        );

        Subtask s2 = new Subtask(
                epic.getId(),
                "S2",
                "D2",
                Status.NEW,
                LocalDateTime.now().plusMinutes(20),
                Duration.ofMinutes(10)
        );

        manager.createSubtask(s1);
        manager.createSubtask(s2);

        assertEquals(2, manager.getSubtasksOfEpic(epic.getId()).size());
    }
}
