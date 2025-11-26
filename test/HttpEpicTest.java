package ru.yandex.tracker;

import org.junit.jupiter.api.*;
import ru.yandex.tracker.Model.*;
import ru.yandex.tracker.Service.InMemoryTaskManager;
import ru.yandex.tracker.Service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpEpicTest {

    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateEpicCorrectly() {
        Epic epic = new Epic(
                "Epic1",
                "Desc",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(0)
        );

        manager.createEpicTask(epic);

        Epic saved = manager.getEpicTask(epic.getId());
        assertNotNull(saved);
        assertEquals("Epic1", saved.getName());
        assertEquals(Status.NEW, saved.getStatus());
    }

    @Test
    void shouldCreateSubtaskForEpic() {
        Epic epic = new Epic(
                "Epic2",
                "Desc2",
                Status.NEW,
                LocalDateTime.now(),
                Duration.ofMinutes(0)
        );

        manager.createEpicTask(epic);

        Subtask sub = new Subtask(
                epic.getId(),
                "Sub1",
                "D1",
                Status.NEW,
                LocalDateTime.now().plusMinutes(10),
                Duration.ofMinutes(20)
        );

        manager.createSubtask(sub);

        assertEquals(1, manager.getSubtasksOfEpic(epic.getId()).size());
        assertEquals("Sub1", manager.getSubtask(sub.getId()).getName());
    }
}